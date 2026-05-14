package com.elif.aiservice.mcp;

import com.elif.aiservice.rag.EmbeddingService;
import com.elif.aiservice.rag.QdrantService;
import com.elif.aiservice.rag.RagService;
import com.elif.aiservice.rag.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiMcpTools {
    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;
    private final RagService ragService;
    private final QdrantService qdrantService;

    @Tool(name = "document.embed", description = "Creates an embedding vector for a document chunk.")
    public Map<String, Object> embedDocument(
            @ToolParam(description = "Chunk text content to embed", required = true) String content
    ) {
        return Map.of("embedding", embeddingService.createEmbedding(content));
    }

    @Tool(name = "document.index", description = "Indexes a document chunk vector and payload in Qdrant.")
    public Map<String, Object> indexDocument(
            @ToolParam(description = "Document id", required = true) Long documentId,
            @ToolParam(description = "Chunk id", required = true) Long chunkId,
            @ToolParam(description = "Chunk index", required = true) Integer chunkIndex,
            @ToolParam(description = "Chunk content", required = true) String content,
            @ToolParam(description = "Start character offset", required = false) Integer startOffset,
            @ToolParam(description = "End character offset", required = false) Integer endOffset,
            @ToolParam(description = "First PDF page covered by this chunk", required = false) Integer pageStart,
            @ToolParam(description = "Last PDF page covered by this chunk", required = false) Integer pageEnd,
            @ToolParam(description = "Embedding vector", required = true) List<Double> embedding
    ) {
        qdrantService.upsertChunk(
                documentId,
                chunkId,
                chunkIndex == null ? 0 : chunkIndex,
                content,
                startOffset,
                endOffset,
                pageStart,
                pageEnd,
                embedding
        );
        return Map.of("indexed", true);
    }

    @Tool(name = "document.delete", description = "Deletes all Qdrant vectors for a document.")
    public Map<String, Object> deleteDocument(
            @ToolParam(description = "Document id", required = true) Long documentId
    ) {
        qdrantService.deleteDocument(documentId);
        return Map.of("deleted", true);
    }

    @Tool(name = "retrieval.search", description = "Finds relevant chunks for a document and query.")
    public Map<String, Object> searchRetrieval(
            @ToolParam(description = "Document id", required = true) Long documentId,
            @ToolParam(description = "User query", required = true) String query,
            @ToolParam(description = "Number of chunks to retrieve", required = false) Integer topK
    ) {
        return Map.of("chunks", retrievalService.retrieveTopK(documentId, query, topK == null ? 3 : topK));
    }

    @Tool(name = "rag.answer", description = "Retrieves document chunks and generates an answer only from that context.")
    public RagService.RagAnswer answerWithRag(
            @ToolParam(description = "Document id", required = true) Long documentId,
            @ToolParam(description = "User question", required = true) String question,
            @ToolParam(description = "Number of chunks to retrieve", required = false) Integer topK
    ) {
        return ragService.answer(documentId, question, topK == null ? 3 : topK);
    }
}
