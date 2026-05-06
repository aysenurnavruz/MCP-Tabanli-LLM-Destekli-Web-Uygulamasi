package com.elif.aiservice.rag;

import com.elif.aiservice.document.DocumentChunk;
import com.elif.aiservice.document.DocumentChunkRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalService {
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    public List<ScoredChunk> retrieveTopK(Long documentId, String query, int k) {
        List<DocumentChunk> chunks = documentChunkRepository.findAllByDocumentIdOrderByChunkIndexAsc(documentId);
        if (chunks == null || chunks.isEmpty()) {
            return List.of();
        }

        List<Double> queryEmbedding = embeddingService.createEmbedding(query);

        return chunks.stream()
                .map(chunk -> new ScoredChunk(
                        chunk.getId(),
                        chunk.getChunkIndex(),
                        chunk.getContent(),
                        chunk.getStartOffset(),
                        chunk.getEndOffset(),
                        similarity(chunk.getEmbedding(), queryEmbedding)
                ))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(k)
                .toList();
    }

    private double similarity(String embeddingJson, List<Double> queryEmbedding) {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            return 0.0;
        }

        try {
            List<Double> chunkEmbedding = objectMapper.readValue(embeddingJson, new TypeReference<>() {});
            return cosineSimilarity(chunkEmbedding, queryEmbedding);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        int dimensions = Math.min(v1.size(), v2.size());

        for (int i = 0; i < dimensions; i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public record ScoredChunk(
            Long id,
            Integer chunkIndex,
            String content,
            Integer startOffset,
            Integer endOffset,
            double score
    ) {}
}
