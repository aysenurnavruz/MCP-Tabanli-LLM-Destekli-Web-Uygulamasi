package com.elif.mcpproject.mcp.tools;

import com.elif.mcpproject.chat.ChatService;
import com.elif.mcpproject.chat.dto.MessageResponse;
import com.elif.mcpproject.mcp.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ListMessagesTool implements McpTool {
    private final ChatService chatService;

    @Override public String name(){ return "list_messages";}
    @Override public String title(){ return "List Messages";}
    @Override public String description(){ return "Lists messages of a chat (paged).";}

    @Override
    public Map<String, Object> inputSchema(){
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "chatId", Map.of("type","integer"),
                        "page", Map.of("type", "integer", "default", 0),
                        "size", Map.of("type","integer","default",50)
                ),
                "required", List.of("chatId")
        );

    }

    @Override
    public Object call(Map<String, Object> arguments, Principal principal){
        Long chatId = ((Number) arguments.get("chatId")).longValue();
        int page = ((Number) arguments.getOrDefault("page", 0)).intValue();
        int size = ((Number) arguments.getOrDefault("size", 20)).intValue();
        Page<MessageResponse> result =
                chatService.listMessagesPaged(chatId, PageRequest.of(page, size), principal);

        return Map.of(
                "items", result.getContent(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }


}
