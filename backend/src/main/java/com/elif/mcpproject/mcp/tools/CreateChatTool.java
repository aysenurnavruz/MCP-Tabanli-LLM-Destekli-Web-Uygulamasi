package com.elif.mcpproject.mcp.tools;

import com.elif.mcpproject.chat.ChatService;
import com.elif.mcpproject.mcp.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateChatTool implements McpTool {
    private final ChatService chatService;

    @Override public String name() { return "create_chat";}
    @Override public String title() { return "Create Chat";}
    @Override public String description() { return "Creates a chat for a document";}

    @Override
    public Map<String, Object> inputSchema(){
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "documentId", Map.of("type","integer"),
                        "title", Map.of("type", "string")
                )
        );
    }

    @Override
    public Object call(Map<String, Object> arguments, Principal principal){
        Long documentId = null;
        if (arguments != null && arguments.get("documentId") != null) {
            documentId = ((Number) arguments.get("documentId")).longValue();
        }
        String title = arguments == null ? null : (String) arguments.get("title");
        return chatService.createChat(documentId,title, principal);
    }
}
