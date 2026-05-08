package com.elif.mcpproject.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiMcpClient {
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${ai.service.mcp-url:http://localhost:8081/mcp}")
    private String mcpUrl;

    @Value("${ai.service.token:dev-service-token}")
    private String serviceToken;

    public List<Double> createEmbedding(String content) {
        Map<String, Object> result = callTool("document.embed", Map.of("content", content == null ? "" : content));
        Object embedding = result.get("embedding");
        return objectMapper.convertValue(embedding, objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
    }

    public AnswerResult answer(Long documentId, String question, int topK) {
        Map<String, Object> result = callTool("rag.answer", Map.of(
                "documentId", documentId,
                "question", question,
                "topK", topK
        ));
        Object answer = result.get("answer");
        List<Citation> citations = objectMapper.convertValue(
                result.get("chunks"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Citation.class)
        );
        return new AnswerResult(answer == null ? "" : answer.toString(), citations == null ? List.of() : citations);
    }

    public void indexChunk(
            Long documentId,
            Long chunkId,
            Integer chunkIndex,
            String content,
            Integer startOffset,
            Integer endOffset,
            Integer pageStart,
            Integer pageEnd,
            List<Double> embedding
    ) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("documentId", documentId);
        arguments.put("chunkId", chunkId);
        arguments.put("chunkIndex", chunkIndex);
        arguments.put("content", content == null ? "" : content);
        arguments.put("startOffset", startOffset);
        arguments.put("endOffset", endOffset);
        arguments.put("pageStart", pageStart);
        arguments.put("pageEnd", pageEnd);
        arguments.put("embedding", embedding);
        callTool("document.index", arguments);
    }

    public void deleteDocumentIndex(Long documentId) {
        callTool("document.delete", Map.of("documentId", documentId));
    }

    private Map<String, Object> callTool(String name, Map<String, Object> arguments) {
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", UUID.randomUUID().toString(),
                "method", "tools/call",
                "params", Map.of(
                        "name", name,
                        "arguments", arguments
                )
        );

        Map<String, Object> response = restClient.post()
                .uri(mcpUrl)
                .header("X-Service-Token", serviceToken)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ResponseStatusException(res.getStatusCode(), "AI service MCP request failed");
                })
                .body(Map.class);

        if (response == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "AI service returned empty response");
        }

        if (response.get("error") != null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "AI service error: " + response.get("error"));
        }

        Map<String, Object> result = objectMapper.convertValue(response.get("result"), Map.class);
        if (Boolean.TRUE.equals(result.get("isError"))) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "AI tool error: " + result.get("content"));
        }

        List<Map<String, Object>> content = objectMapper.convertValue(result.get("content"), List.class);
        if (content == null || content.isEmpty()) {
            return Map.of();
        }

        Object json = content.get(0).get("json");
        return objectMapper.convertValue(json, Map.class);
    }

    public record AnswerResult(
            String answer,
            List<Citation> citations
    ) {
    }

    public record Citation(
            Long id,
            Integer chunkIndex,
            String content,
            Integer startOffset,
            Integer endOffset,
            Integer pageStart,
            Integer pageEnd,
            double score
    ) {
    }
}
