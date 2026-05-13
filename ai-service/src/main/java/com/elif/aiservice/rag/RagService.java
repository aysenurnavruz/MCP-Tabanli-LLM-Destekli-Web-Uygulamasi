package com.elif.aiservice.rag;

import com.elif.aiservice.document.DocumentChunk;
import com.elif.aiservice.document.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {
    private final RetrievalService retrievalService;
    private final DocumentChunkRepository documentChunkRepository;
    private final LlmService llmService;

    public RagAnswer answer(Long documentId, String question, int topK) {
        boolean overviewQuestion = isOverviewQuestion(question);
        int effectiveTopK = Math.max(topK, overviewQuestion ? 8 : 5);
        List<RetrievalService.ScoredChunk> chunks = new ArrayList<>(
                retrievalService.retrieveTopK(documentId, question, effectiveTopK)
        );

        if (overviewQuestion) {
            chunks = withDocumentOpeningChunks(documentId, chunks);
        }

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
                Kullanıcı genel özet veya dokümanın konusu hakkında soruyorsa, context'teki parçaları birlikte değerlendirerek kısa bir özet üret.
                Cevapta kullandığın bilgilerin yanına ilgili source etiketini köşeli parantezle ekle. Örnek: [source:2 page:5]

                Context:
                %s

                Soru:
                %s
                """.formatted(context, question);

        return new RagAnswer(llmService.ask(prompt), chunks);
    }

    private List<RetrievalService.ScoredChunk> withDocumentOpeningChunks(
            Long documentId,
            List<RetrievalService.ScoredChunk> retrievedChunks
    ) {
        Map<Long, RetrievalService.ScoredChunk> merged = new LinkedHashMap<>();

        documentChunkRepository.findAllByDocumentIdOrderByChunkIndexAsc(documentId).stream()
                .limit(6)
                .map(this::toScoredChunk)
                .forEach(chunk -> merged.putIfAbsent(chunk.id(), chunk));

        retrievedChunks.forEach(chunk -> merged.putIfAbsent(chunk.id(), chunk));

        return merged.values().stream()
                .limit(10)
                .toList();
    }

    private RetrievalService.ScoredChunk toScoredChunk(DocumentChunk chunk) {
        return new RetrievalService.ScoredChunk(
                chunk.getId(),
                chunk.getChunkIndex(),
                chunk.getContent(),
                chunk.getStartOffset(),
                chunk.getEndOffset(),
                chunk.getPageStart(),
                chunk.getPageEnd(),
                1.0
        );
    }

    private boolean isOverviewQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }

        String normalized = question.toLowerCase(Locale.forLanguageTag("tr-TR"));
        return (normalized.contains("özet")
                || normalized.contains("ozet")
                || normalized.contains("ne hakkında")
                || normalized.contains("neden bahsed")
                || normalized.contains("konusu ne")
                || normalized.contains("kısaca açıkla")
                || normalized.contains("kisaca acikla")
                || normalized.contains("genel olarak"))
                && (normalized.contains("pdf")
                || normalized.contains("doküman")
                || normalized.contains("dokuman")
                || normalized.contains("belge")
                || normalized.contains("metin")
                || normalized.contains("bu"));
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
