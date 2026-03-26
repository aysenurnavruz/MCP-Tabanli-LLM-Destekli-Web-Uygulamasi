package com.elif.mcpproject.mcp.tools;

import com.elif.mcpproject.chat.ChatService;
import com.elif.mcpproject.chat.dto.MessageRequest;
import com.elif.mcpproject.mcp.McpTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SendMessageTool implements McpTool {
    private final ChatService chatService;

    @Override
    public String name() {
        return "send_message";
    }

    @Override
    public String title() {
        return "Send Message";
    }

    @Override
    public String description() {
        return "Sends a user message to a chat and stores it.";
    }

    @Override
    public Map<String, Object> inputSchema(){
        return Map.of(
                "type","object",
                "properties", Map.of(
                        "chatId", Map.of("type","integer"),
                        "content", Map.of("type","string"),
                        "clientMessageId", Map.of("type", "string")
                ),
                "required", List.of("chatId","content")
        );
    }

    @Override
    public Object call(Map<String, Object> arguments, Principal principal){
        Long chatId = ((Number) arguments.get("chatId")).longValue();
        String content = (String) arguments.get("content");
        String clientMessageId = (String) arguments.get("clientMessageId");

        MessageRequest req = new MessageRequest(content, clientMessageId);
        return chatService.sendUserMessage(chatId, req, principal);
    }
}
