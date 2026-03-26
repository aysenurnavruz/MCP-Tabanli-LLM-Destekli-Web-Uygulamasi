package com.elif.mcpproject.document.text;

import com.elif.mcpproject.document.Document;
import com.elif.mcpproject.document.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DocumentExtractionService {
    private final DocumentRepository documentRepository;
    private final DocumentTextRepository documentTextRepository;

    @Transactional
    public DocumentText extractAndStore(Long documentId, Long userId){
        Document doc = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or not owned by user"));
        DocumentText dt = documentTextRepository.findByDocumentId(documentId)
                .orElseGet(() -> DocumentText.builder()
                        .status(DocumentTextStatus.PENDING)
                        .build());
        dt.setStatus(DocumentTextStatus.PENDING);
        dt.setErrorMessage(null);
        dt.setExtractedText(null);
        documentTextRepository.save(dt);

        try{
            String text = extractText(doc);
            dt.setExtractedText(text);
            dt.setStatus(DocumentTextStatus.DONE);
            return documentTextRepository.save(dt);
        }catch (Exception e){
            dt.setStatus(DocumentTextStatus.FAILED);
            dt.setErrorMessage(safeMessage(e));
            return documentTextRepository.save(dt);
        }
    }

    private String extractText(Document doc) throws Exception{
        Path path = Path.of(doc.getStoragePath());
        String ct = doc.getContentType() == null ? " ": doc.getContentType().toLowerCase();

        if (ct.contains("pdf") || doc.getOriginalFilename().toLowerCase().endsWith(".pdf")){
            try (PDDocument pdf = PDDocument.load(Files.readAllBytes(path))){
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(pdf);
            }
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private String safeMessage(Exception e){
        String msg = e.getMessage();
        if (msg == null) return e.getClass().getSimpleName();
        return msg.length() > 500 ? msg.substring(0,500) : msg;
    }
    }
