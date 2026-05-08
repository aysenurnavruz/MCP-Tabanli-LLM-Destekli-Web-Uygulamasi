package com.elif.aiservice.mcp;

import com.elif.aiservice.rag.EmbeddingService;
import com.elif.aiservice.rag.QdrantService;
import com.elif.aiservice.rag.RagService;
import com.elif.aiservice.rag.RetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mcp")
public class McpController {
    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;
    private final RagService ragService;
    private final QdrantService qdrantService;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.token}")
    private String serviceToken;

    @PostMapping
    public ResponseEntity<JsonRpcResponse> handle(@RequestBody JsonRpcRequest req, HttpServletRequest httpReq) {
        if (req == null || req.id() == null || req.method() == null) {
            return ResponseEntity.badRequest()
                    .body(JsonRpcResponse.err(null, -32600, "Invalid Request", null));
        }

        if (!"2.0".equals(req.jsonrpc())) {
            return ResponseEntity.badRequest()
                    .body(JsonRpcResponse.err(req.id(), -32600, "Invalid JSON-RPC version", null));
        }

        if ("initialize".equals(req.method())) {
            return ResponseEntity.ok(JsonRpcResponse.ok(req.id(), Map.of(
                    "protocolVersion", "2025-03-26",
                    "capabilities", Map.of("tools", Map.of("listChanged", false)),
                    "serverInfo", Map.of("name", "ai-service", "version", "0.1.0")
            )));
        }

        String token = httpReq.getHeader("X-Service-Token");
        if (!serviceToken.equals(token)) {
            return ResponseEntity.status(401)
                    .body(JsonRpcResponse.err(req.id(), -32001, "Unauthorized service token", null));
        }

        return switch (req.method()) {
            case "tools/list" -> ResponseEntity.ok(JsonRpcResponse.ok(req.id(), Map.of("tools", tools())));
            case "tools/call" -> handleToolCall(req);
            default -> ResponseEntity.ok(JsonRpcResponse.err(req.id(), -32601, "Method not found", Map.of("method", req.method())));
        };
    }

    private ResponseEntity<JsonRpcResponse> handleToolCall(JsonRpcRequest req) {
        Map<String, Object> params = req.params() == null ? Map.of() : req.params();
        String name = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        try {
            Object toolResult = switch (name == null ? "" : name) {
                case "document.embed" -> Map.of("embedding", embeddingService.createEmbedding(stringArg(arguments, "content")));
                case "document.index" -> {
                    qdrantService.upsertChunk(
                            longArg(arguments, "documentId"),
                            longArg(arguments, "chunkId"),
                            intArg(arguments, "chunkIndex", 0),
                            stringArg(arguments, "content"),
                            nullableIntArg(arguments, "startOffset"),
                            nullableIntArg(arguments, "endOffset"),
                            nullableIntArg(arguments, "pageStart"),
                            nullableIntArg(arguments, "pageEnd"),
                            doubleListArg(arguments, "embedding")
                    );
                    yield Map.of("indexed", true);
                }
                case "document.delete" -> {
                    qdrantService.deleteDocument(longArg(arguments, "documentId"));
                    yield Map.of("deleted", true);
                }
                case "retrieval.search" -> Map.of("chunks", retrievalService.retrieveTopK(
                        longArg(arguments, "documentId"),
                        stringArg(arguments, "query"),
                        intArg(arguments, "topK", 3)
                ));
                case "rag.answer" -> ragService.answer(
                        longArg(arguments, "documentId"),
                        stringArg(arguments, "question"),
                        intArg(arguments, "topK", 3)
                );
                default -> throw new IllegalArgumentException("Unknown tool: " + name);
            };

            return ResponseEntity.ok(JsonRpcResponse.ok(req.id(), Map.of(
                    "content", List.of(Map.of("type", "json", "json", toolResult)),
                    "isError", false
            )));
        } catch (Exception e) {
            return ResponseEntity.ok(JsonRpcResponse.ok(req.id(), Map.of(
                    "content", List.of(Map.of("type", "text", "text", "Tool error: " + e.getMessage())),
                    "isError", true
            )));
        }
    }

    private List<Map<String, Object>> tools() {
        return List.of(
                tool("document.embed", "Embed document chunk", "Creates an embedding vector for chunk text."),
                tool("document.index", "Index document chunk", "Stores a chunk vector and payload in Qdrant."),
                tool("document.delete", "Delete document vectors", "Deletes all Qdrant vectors for a document."),
                tool("retrieval.search", "Search retrieved chunks", "Finds relevant chunks for a document and query."),
                tool("rag.answer", "Answer with RAG", "Retrieves document chunks and generates an answer only from that context.")
        );
    }

    private Map<String, Object> tool(String name, String title, String description) {
        return Map.of(
                "name", name,
                "title", title,
                "description", description,
                "inputSchema", Map.of("type", "object")
        );
    }

    private String stringArg(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.toString();
    }

    private Long longArg(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            return Long.parseLong(value.toString());
        }
        throw new IllegalArgumentException(name + " is required");
    }

    private int intArg(Map<String, Object> args, String name, int defaultValue) {
        Object value = args.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Integer nullableIntArg(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private List<Double> doubleListArg(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return objectMapper.convertValue(
                value,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)
        );
    }
}
