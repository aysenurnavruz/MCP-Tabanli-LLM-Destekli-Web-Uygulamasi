package com.elif.mcpproject.mcp.tools;

import com.elif.mcpproject.document.DocumentService;
import com.elif.mcpproject.document.dto.DocumentResponse;
import com.elif.mcpproject.mcp.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ListDocumentsTool implements McpTool {
    private final DocumentService documentService;

    @Override public String name() {return "list_documents";}
    @Override public String title() {return "List Documents";}
    @Override public String description(){ return "Lists your uploaded documents (paged)." ;}

    @Override
    public Map<String, Object> inputSchema(){
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "page", Map.of("type","integer","default",0),
                        "size", Map.of("type","integer", "default", 20)
                )
        );
    }

    @Override
    public Object call(Map<String, Object> arguments, Principal principal){
        int page = ((Number) arguments.getOrDefault("page",0)).intValue();
        int size = ((Number) arguments.getOrDefault("size",20)).intValue();

        Page<DocumentResponse> result =
                documentService.listMinePaged(PageRequest.of(page,size),principal);

        return Map.of(
                "items", result.getContent(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }

}
