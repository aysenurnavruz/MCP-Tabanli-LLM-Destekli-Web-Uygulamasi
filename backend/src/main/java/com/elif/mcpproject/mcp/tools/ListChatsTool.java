package com.elif.mcpproject.mcp.tools;

import com.elif.mcpproject.chat.ChatService;
import com.elif.mcpproject.mcp.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ListChatsTool implements McpTool {
    private final ChatService chatService;

    @Override public String name(){ return "list_chats";}
    @Override public String title(){return "List Chats"; }
    @Override public String description(){ return "Lists current user's chats (paged).";}

    @Override
    public Map<String, Object> inputSchema(){
        return Map.of(
                "type","object",
                "properties", Map.of(
                                "page",Map.of("type","integer","default",0),
                                "size",Map.of("type","integer","default",20)
                )
        );
    }

    @Override
    public Object call(Map<String, Object> arguments, Principal principal){
        int page = arguments.get("page") == null ? 0 : ((Number) arguments.get("page")).intValue();
        int size = arguments.get("size") == null ? 20 : ((Number) arguments.get("size")).intValue();
        return chatService.listMyChatsPaged(PageRequest.of(page, size), principal);
    }
}
