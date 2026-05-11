package com.elif.aiservice.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {
    private final RetrievalService retrievalService;
    private final LlmService llmService;

    public RagAnswer answer(Long documentId, String question, int topK) {
        List<RetrievalService.ScoredChunk> chunks = retrievalService.retrieveTopK(documentId, question, topK);

        String context = chunks.stream()
                .map(RetrievalService.ScoredChunk::content)
                .filter(content -> content != null && !content.isBlank())
                .collect(Collectors.joining("\n\n"));

        if (context.isBlank()) {
            context = "Dokumanda ilgili icerik bulunamadi.";
        }

        String prompt = """
                Sen bir yardımcı asistansın.

                Sadece aşağıdaki context'e göre cevap ver.
                Cevap context'te yoksa "Bilmiyorum" de.

                Context:
                %s

                Soru:
                %s
                """.formatted(context, question);

        return new RagAnswer(llmService.ask(prompt), chunks);
    }

    public record RagAnswer(
            String answer,
            List<RetrievalService.ScoredChunk> chunks
    ) {}
}
