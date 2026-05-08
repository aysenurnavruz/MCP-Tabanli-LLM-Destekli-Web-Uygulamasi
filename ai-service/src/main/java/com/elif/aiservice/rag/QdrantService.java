package com.elif.aiservice.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QdrantService {
    private final RestClient restClient = RestClient.create();

    @Value("${qdrant.url:http://localhost:6333}")
    private String qdrantUrl;

    @Value("${qdrant.collection:document_chunks}")
    private String collection;

    private volatile boolean collectionReady = false;

    public void upsertChunk(
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
        ensureCollection(embedding.size());

        Map<String, Object> payload = new HashMap<>();
        payload.put("documentId", documentId);
        payload.put("chunkId", chunkId);
        payload.put("chunkIndex", chunkIndex);
        payload.put("content", content);
        payload.put("startOffset", startOffset);
        payload.put("endOffset", endOffset);
        payload.put("pageStart", pageStart);
        payload.put("pageEnd", pageEnd);

        Map<String, Object> point = Map.of(
                "id", chunkId,
                "vector", embedding,
                "payload", payload
        );

        restClient.put()
                .uri(qdrantUrl + "/collections/" + collection + "/points?wait=true")
                .body(Map.of("points", List.of(point)))
                .retrieve()
                .toBodilessEntity();
    }

    public List<RetrievalService.ScoredChunk> search(Long documentId, List<Double> queryEmbedding, int limit) {
        ensureCollection(queryEmbedding.size());

        Map<String, Object> filter = Map.of(
                "must", List.of(Map.of(
                        "key", "documentId",
                        "match", Map.of("value", documentId)
                ))
        );

        Map<String, Object> body = Map.of(
                "vector", queryEmbedding,
                "filter", filter,
                "limit", limit,
                "with_payload", true
        );

        Map<String, Object> response = restClient.post()
                .uri(qdrantUrl + "/collections/" + collection + "/points/search")
                .body(body)
                .retrieve()
                .body(Map.class);

        Object rawResult = response == null ? null : response.get("result");
        if (!(rawResult instanceof List<?> result)) {
            return List.of();
        }

        List<RetrievalService.ScoredChunk> chunks = new ArrayList<>();
        for (Object item : result) {
            if (!(item instanceof Map<?, ?> point)) {
                continue;
            }

            Object rawPayload = point.get("payload");
            if (!(rawPayload instanceof Map<?, ?> payload)) {
                continue;
            }

            chunks.add(new RetrievalService.ScoredChunk(
                    asLong(payload.get("chunkId")),
                    asInteger(payload.get("chunkIndex")),
                    asString(payload.get("content")),
                    asInteger(payload.get("startOffset")),
                    asInteger(payload.get("endOffset")),
                    asInteger(payload.get("pageStart")),
                    asInteger(payload.get("pageEnd")),
                    asDouble(point.get("score"))
            ));
        }

        return chunks;
    }

    public void deleteDocument(Long documentId) {
        if (!collectionExists()) {
            return;
        }

        Map<String, Object> filter = Map.of(
                "must", List.of(Map.of(
                        "key", "documentId",
                        "match", Map.of("value", documentId)
                ))
        );

        restClient.post()
                .uri(qdrantUrl + "/collections/" + collection + "/points/delete?wait=true")
                .body(Map.of("filter", filter))
                .retrieve()
                .toBodilessEntity();
    }

    private synchronized void ensureCollection(int vectorSize) {
        if (collectionReady) {
            return;
        }

        if (collectionExists()) {
            collectionReady = true;
            return;
        }

        restClient.put()
                .uri(qdrantUrl + "/collections/" + collection)
                .body(Map.of(
                        "vectors", Map.of(
                                "size", vectorSize,
                                "distance", "Cosine"
                        )
                ))
                .retrieve()
                .toBodilessEntity();

        collectionReady = true;
    }

    private boolean collectionExists() {
        try {
            restClient.get()
                    .uri(qdrantUrl + "/collections/" + collection)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ignored) {
            return false;
        }
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.parseLong(value.toString());
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? null : Integer.parseInt(value.toString());
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? 0.0 : Double.parseDouble(value.toString());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
