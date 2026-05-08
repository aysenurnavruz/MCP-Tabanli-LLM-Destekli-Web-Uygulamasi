package com.elif.aiservice.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalService {
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;

    public List<ScoredChunk> retrieveTopK(Long documentId, String query, int k) {
        List<Double> queryEmbedding = embeddingService.createEmbedding(query);
        return qdrantService.search(documentId, queryEmbedding, k);
    }

    public record ScoredChunk(
            Long id,
            Integer chunkIndex,
            String content,
            Integer startOffset,
            Integer endOffset,
            Integer pageStart,
            Integer pageEnd,
            double score
    ) {}
}
