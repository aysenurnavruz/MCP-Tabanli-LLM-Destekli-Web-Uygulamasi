package com.elif.mcpproject.document;

import com.elif.mcpproject.document.dto.DocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse upload(@RequestPart("file") MultipartFile file,
                                   Principal principal) {
        return documentService.upload(file, principal);
    }

    @GetMapping
    public Page<DocumentResponse> listMinePaged(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal){
        return documentService.listMinePaged(PageRequest.of(page,size),principal);
    }

    @GetMapping("/{id}")
    public DocumentResponse getMine(@PathVariable Long id, Principal principal){
        return documentService.getMine(id,principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMine(@PathVariable Long id, Principal principal) {
        documentService.deleteMine(id, principal);
    }
}
