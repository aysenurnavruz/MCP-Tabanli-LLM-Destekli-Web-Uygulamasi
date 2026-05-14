package com.elif.mcpproject.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiMcpClient {
    private final ObjectMapper objectMapper;
    private final List<McpSyncClient> mcpSyncClients;

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
        McpSchema.CallToolResult result = mcpClient()
                .callTool(new McpSchema.CallToolRequest(name, arguments));

        if (Boolean.TRUE.equals(result.isError())) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "AI tool error: " + result.content()
            );
        }

        if (result.structuredContent() != null) {
            return objectMapper.convertValue(result.structuredContent(), Map.class);
        }

        List<McpSchema.Content> content = result.content();
        if (content == null || content.isEmpty()) {
            return Map.of();
        }

        McpSchema.Content firstContent = content.get(0);
        if (firstContent instanceof McpSchema.TextContent textContent && !textContent.text().isBlank()) {
            try {
                return objectMapper.readValue(textContent.text(), Map.class);
            } catch (Exception e) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "AI tool returned non-json text content"
                );
            }
        }

        return Map.of();
    }

    private McpSyncClient mcpClient() {
        if (mcpSyncClients == null || mcpSyncClients.isEmpty()) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "No MCP client is configured for AI service"
            );
        }
        return mcpSyncClients.get(0);
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
