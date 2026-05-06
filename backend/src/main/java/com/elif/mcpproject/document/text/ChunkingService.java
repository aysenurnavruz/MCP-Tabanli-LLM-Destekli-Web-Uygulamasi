package com.elif.mcpproject.document.text;

import com.elif.mcpproject.document.Document;
import com.elif.mcpproject.document.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkingService {

    private final DocumentRepository documentRepository;
    private final DocumentTextRepository documentTextRepository;
    private final DocumentChunkRepository documentChunkRepository;

    @Transactional
    public int chunkDocument(Long documentId, Long userId){

        Document doc = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or not owned by user"));

        String text = resolveBestTextSource(documentId, doc);

        if (text == null || text.isBlank()){
            throw new IllegalStateException("No extracted text available to chunk");
        }

        documentChunkRepository.deleteAllByDocumentId(documentId);

        int chunkSize = 1600;
        int overlap = 200;

        List<DocumentChunk> chunks = buildChunks(doc, text, chunkSize, overlap);

        documentChunkRepository.saveAll(chunks);

        return chunks.size();
    }

    private String resolveBestTextSource(Long documentId, Document doc){
        var dtOpt = documentTextRepository.findByDocumentId(documentId);

        if (dtOpt.isPresent() && dtOpt.get().getStatus() == DocumentTextStatus.DONE){
            String t = dtOpt.get().getExtractedText();
            if (t != null && !t.isBlank()) return t;
        }
        return doc.getExtractedText();
    }

    private List<DocumentChunk> buildChunks(Document document, String text, int chunkSize, int overlap){

        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be smaller than chunkSize");
        }

        List<DocumentChunk> out = new ArrayList<>();
        int i = 0;
        int idx = 0;

        while (i < text.length()){
            int end = Math.min(i + chunkSize, text.length());
            String part = text.substring(i, end);

            out.add(DocumentChunk.builder()
                    .document(document)
                    .chunkIndex(idx++)
                    .content(part)
                    .startOffset(i)
                    .endOffset(end)
                    .build());

            if (end == text.length()) break;

            i = Math.max(0, end - overlap);
        }

        return out;
    }
}