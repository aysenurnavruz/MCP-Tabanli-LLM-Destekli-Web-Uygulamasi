package com.elif.mcpproject.document;

import com.elif.mcpproject.ai.AiMcpClient;
import com.elif.mcpproject.chat.Chat;
import com.elif.mcpproject.chat.ChatRepository;
import com.elif.mcpproject.chat.MessageRepository;
import com.elif.mcpproject.document.dto.DocumentReprocessResponse;
import com.elif.mcpproject.document.dto.DocumentResponse;
import com.elif.mcpproject.document.text.DocumentTextRepository;
import com.elif.mcpproject.security.CurrentUserService;
import com.elif.mcpproject.user.AppUser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import com.elif.mcpproject.document.text.DocumentChunk;
import com.elif.mcpproject.document.text.DocumentChunkRepository;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CurrentUserService currentUserService;
    private final AiMcpClient aiMcpClient;
    private final ObjectMapper objectMapper;
    private final DocumentChunkRepository chunkRepository;
    private final DocumentTextRepository documentTextRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Value("${app.storage.path:storage}")
    private String storageRoot;

    public DocumentResponse upload(MultipartFile file, Principal principal){
        if (file == null || file.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"File is required");
        }

        AppUser user = currentUserService.requireCurrentUser(principal);

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";

        int dot = original.lastIndexOf('.');
        if (dot>-1 && dot< original.length()-1){
            ext = original.substring(dot);
        }

        String stored = UUID.randomUUID() + ext;

        String storagePath = Paths.get("users", user.getId().toString(), stored).toString();
        Path target = Paths.get(storageRoot).resolve(storagePath);

        try{
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File save failed: " + e.getMessage());
        }

        String contentType = file.getContentType();
        boolean isPdf = "application/pdf".equalsIgnoreCase(contentType) || original.toLowerCase().endsWith(".pdf");
        ExtractionResult extraction = extractText(target, isPdf);

        Instant now = Instant.now();

        Document doc = Document.builder()
                .user(user)
                .originalFilename(original)
                .storedFilename(stored)
                .contentType((file.getContentType()))
                .sizeBytes((file.getSize()))
                .storagePath(storagePath)
                .extractedText(extraction.text())
                .createdAt((now))
                .build();

        Document saved = documentRepository.save(doc);

        processAndIndex(saved, extraction.pageRanges());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> listMinePaged(Pageable pageable, Principal principal){
        AppUser user = currentUserService.requireCurrentUser(principal);
        return documentRepository
                .findAllByUserIdOrderByCreatedAtDesc(user.getId(),pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getMine(Long id,Principal principal){
        AppUser user = currentUserService.requireCurrentUser(principal);
        Document doc = documentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Document not found"));
        return toResponse(doc);
    }

    @Transactional
    public void deleteMine(Long id, Principal principal) {
        AppUser user = currentUserService.requireCurrentUser(principal);
        Document doc = documentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        List<Chat> chats = chatRepository.findAllByDocumentIdAndUserId(doc.getId(), user.getId());
        for (Chat chat : chats) {
            messageRepository.deleteAllByChatId(chat.getId());
        }

        chatRepository.deleteAll(chats);
        chunkRepository.deleteAllByDocumentId(doc.getId());
        documentTextRepository.deleteByDocumentId(doc.getId());
        aiMcpClient.deleteDocumentIndex(doc.getId());
        documentRepository.delete(doc);

        deleteStoredFile(doc);
    }

    @Transactional
    public DocumentReprocessResponse reprocessMine(Long id, Principal principal) {
        AppUser user = currentUserService.requireCurrentUser(principal);
        Document doc = documentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        Path path = Paths.get(storageRoot).resolve(doc.getStoragePath()).normalize();
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stored file not found");
        }

        boolean isPdf = "application/pdf".equalsIgnoreCase(doc.getContentType())
                || doc.getOriginalFilename().toLowerCase().endsWith(".pdf");
        ExtractionResult extraction = extractText(path, isPdf);
        doc.setExtractedText(extraction.text());
        documentRepository.save(doc);

        chunkRepository.deleteAllByDocumentId(doc.getId());
        aiMcpClient.deleteDocumentIndex(doc.getId());
        int chunkCount = processAndIndex(doc, extraction.pageRanges());

        return new DocumentReprocessResponse(doc.getId(), chunkCount, true);
    }

    private DocumentResponse toResponse(Document d){
        return new DocumentResponse(
                d.getId(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getCreatedAt()
        );
    }

    private int processAndIndex(Document document, List<PageRange> pageRanges) {
        if (document.getExtractedText() == null || document.getExtractedText().isBlank()) {
            return 0;
        }

        List<DocumentChunk> chunks = splitWithOffsets(document.getExtractedText(), document, pageRanges);

        for (DocumentChunk chunk : chunks) {
            List<Double> embeddingVector = aiMcpClient.createEmbedding(chunk.getContent());

            String embeddingJson;
            try {
                embeddingJson = objectMapper.writeValueAsString(embeddingVector);
            } catch (Exception e) {
                throw new RuntimeException("Embedding JSON convert error", e);
            }
            chunk.setEmbedding(embeddingJson);
            DocumentChunk savedChunk = chunkRepository.save(chunk);
            aiMcpClient.indexChunk(
                    document.getId(),
                    savedChunk.getId(),
                    savedChunk.getChunkIndex(),
                    savedChunk.getContent(),
                    savedChunk.getStartOffset(),
                    savedChunk.getEndOffset(),
                    savedChunk.getPageStart(),
                    savedChunk.getPageEnd(),
                    embeddingVector
            );
        }

        return chunks.size();
    }

    private List<DocumentChunk> splitWithOffsets(String text, Document document, List<PageRange> pageRanges) {

        int chunkSize = 500;
        List<DocumentChunk> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize) {

            int end = Math.min(text.length(), i + chunkSize);

            String chunkText = text.substring(i, end);
            PageSpan pageSpan = pageSpanFor(i, end, pageRanges);

            DocumentChunk chunk = DocumentChunk.builder()
                .document(document)
                .content(chunkText)
                .chunkIndex(chunks.size())
                .startOffset(i)
                .endOffset(end)
                .pageStart(pageSpan.start())
                .pageEnd(pageSpan.end())
                .build();

            chunks.add(chunk);
        }

        return chunks;
    }

    private ExtractionResult extractText(Path path, boolean isPdf) {
        if (!isPdf) {
            return new ExtractionResult(null, List.of());
        }

        try (PDDocument pdf = PDDocument.load(path.toFile())) {
            StringBuilder text = new StringBuilder();
            List<PageRange> pageRanges = new ArrayList<>();

            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(pdf);
                if (pageText == null || pageText.isBlank()) {
                    continue;
                }

                if (!text.isEmpty()) {
                    text.append("\n\n");
                }

                int start = text.length();
                text.append(pageText);
                int end = text.length();
                pageRanges.add(new PageRange(page, start, end));
            }

            String extractedText = text.toString();
            if (extractedText.isBlank()) {
                return new ExtractionResult(null, List.of());
            }

            return new ExtractionResult(extractedText, pageRanges);
        } catch (Exception e) {
            return new ExtractionResult(null, List.of());
        }
    }

    private PageSpan pageSpanFor(int chunkStart, int chunkEnd, List<PageRange> pageRanges) {
        if (pageRanges == null || pageRanges.isEmpty()) {
            return new PageSpan(null, null);
        }

        Integer pageStart = null;
        Integer pageEnd = null;

        for (PageRange pageRange : pageRanges) {
            boolean overlaps = chunkStart < pageRange.endOffset() && chunkEnd > pageRange.startOffset();
            if (!overlaps) {
                continue;
            }

            if (pageStart == null) {
                pageStart = pageRange.pageNumber();
            }
            pageEnd = pageRange.pageNumber();
        }

        return new PageSpan(pageStart, pageEnd);
    }

    private void deleteStoredFile(Document document) {
        if (document.getStoragePath() == null || document.getStoragePath().isBlank()) {
            return;
        }

        Path path = Paths.get(storageRoot).resolve(document.getStoragePath()).normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File delete failed: " + e.getMessage());
        }
    }

    private record ExtractionResult(String text, List<PageRange> pageRanges) {
    }

    private record PageRange(Integer pageNumber, Integer startOffset, Integer endOffset) {
    }

    private record PageSpan(Integer start, Integer end) {
    }
}
