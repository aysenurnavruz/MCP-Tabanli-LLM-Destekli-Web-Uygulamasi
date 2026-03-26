package com.elif.mcpproject.chat;

import com.elif.mcpproject.chat.dto.ChatCreateRequest;
import com.elif.mcpproject.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;


@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse create(@RequestBody ChatCreateRequest req, Principal principal){
        return chatService.createChat(req.documentId(), req.title(), principal);
    }

    @GetMapping
    public Page<ChatResponse> myChats(Pageable pageable, Principal principal){
        return chatService.listMyChatsPaged(pageable, principal);
    }

}
