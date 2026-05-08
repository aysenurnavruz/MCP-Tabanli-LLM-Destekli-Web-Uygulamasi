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
                .map(chunk -> """
                        [source:%s page:%s score:%.4f]
                        %s
                        """.formatted(
                        sourceLabel(chunk),
                        pageLabel(chunk),
                        chunk.score(),
                        chunk.content()
                ))
                .filter(content -> content != null && !content.isBlank())
                .collect(Collectors.joining("\n\n"));

        if (context.isBlank()) {
            context = "Dokumanda ilgili icerik bulunamadi.";
        }

        String prompt = """
                Sen bir yardımcı asistansın.

                Sadece aşağıdaki context'e göre cevap ver.
                Cevap context'te yoksa "Bilmiyorum" de.
                Cevapta kullandığın bilgilerin yanına ilgili source etiketini köşeli parantezle ekle. Örnek: [source:2 page:5]

                Context:
                %s

                Soru:
                %s
                """.formatted(context, question);

        return new RagAnswer(llmService.ask(prompt), chunks);
    }

    private String sourceLabel(RetrievalService.ScoredChunk chunk) {
        return chunk.chunkIndex() == null ? "?" : String.valueOf(chunk.chunkIndex());
    }

    private String pageLabel(RetrievalService.ScoredChunk chunk) {
        if (chunk.pageStart() == null && chunk.pageEnd() == null) {
            return "?";
        }
        if (chunk.pageEnd() == null || chunk.pageStart().equals(chunk.pageEnd())) {
            return String.valueOf(chunk.pageStart());
        }
        return chunk.pageStart() + "-" + chunk.pageEnd();
    }

    public record RagAnswer(
            String answer,
            List<RetrievalService.ScoredChunk> chunks
    ) {}
}
