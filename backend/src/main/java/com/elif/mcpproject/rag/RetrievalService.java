package com.elif.mcpproject.rag;

import com.elif.mcpproject.document.text.DocumentChunk;
import com.elif.mcpproject.document.text.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RetrievalService {
    private final DocumentChunkRepository documentChunkRepository;

    public List<DocumentChunk> retrieveTopK(Long documentId, String query,int k){
        List<DocumentChunk> chunks = documentChunkRepository.findAllByDocumentIdOrderByChunkIndexAsc(documentId);
        if (chunks.isEmpty()) return List.of();

        Set<String> qTokens = tokenize(query);
        if (qTokens.isEmpty()) return chunks.stream().limit(k).toList();

        return chunks.stream()
                .map(c -> new ScoredChunk(c,score(c.getContent(),qTokens)))
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(k)
                .map(ScoredChunk::chunk)
                .collect(Collectors.toList());
    }

    private int score(String content, Set<String> qTokens){
        if (content == null || content.isBlank()) return 0;
        String lower = content.toLowerCase(Locale.ROOT);
        int s = 0;
        for (String t : qTokens){
            if (lower.contains(t)) s++;
        }
        return s;
    }

    private Set<String> tokenize(String text){
        if (text == null) return Set.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+"))
                .filter(t->t.length() >=3)
                .collect(Collectors.toSet());
    }

    private record ScoredChunk(DocumentChunk chunk, int score){}
}
