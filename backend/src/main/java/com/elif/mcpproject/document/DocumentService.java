package com.elif.mcpproject.document;

import com.elif.mcpproject.document.dto.DocumentResponse;
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

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CurrentUserService currentUserService;

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

        String extractedText = null;
        String contentType = file.getContentType();
        boolean isPdf = "application/pdf".equalsIgnoreCase(contentType) || original.toLowerCase().endsWith(".pdf");

        if (isPdf){
            try (PDDocument pdf = PDDocument.load(target.toFile())){
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(pdf);
                if (extractedText != null) extractedText = extractedText.trim();
                if (extractedText != null && extractedText.isBlank()) extractedText = null;
            }catch (Exception e){
                extractedText = null;
            }
        }

        Instant now = Instant.now();

        Document doc = Document.builder()
                .user(user)
                .originalFilename(original)
                .storedFilename(stored)
                .contentType((file.getContentType()))
                .sizeBytes((file.getSize()))
                .storagePath(storagePath)
                .extractedText(extractedText)
                .createdAt((now))
                .build();

        Document saved = documentRepository.save(doc);
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

    private DocumentResponse toResponse(Document d){
        return new DocumentResponse(
                d.getId(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getCreatedAt()
        );
    }
}
