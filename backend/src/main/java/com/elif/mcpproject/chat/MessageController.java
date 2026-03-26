package com.elif.mcpproject.chat;

import com.elif.mcpproject.chat.dto.MessageRequest;
import com.elif.mcpproject.chat.dto.MessageResponse;
import com.elif.mcpproject.chat.dto.SendMessageResponse;
import lombok.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Getter @Setter
@RequestMapping("/api/chats/{chatId}/messages")
public class MessageController {

    private final ChatService chatService;

    @PostMapping
    public SendMessageResponse send(@PathVariable Long chatId,
                                                   @RequestBody MessageRequest req,
                                                   Principal principal){
        return chatService.sendUserMessage(chatId,req,principal);
    }

    @GetMapping
    public Page<MessageResponse> list(@PathVariable Long chatId, Pageable pageable, Principal principal) {
        return chatService.listMessagesPaged(chatId, pageable, principal);
    }

}
