package com.elif.mcpproject.chat;

import com.elif.mcpproject.ai.AiMcpClient;
import com.elif.mcpproject.chat.dto.ChatResponse;
import com.elif.mcpproject.chat.dto.MessageResponse;
import com.elif.mcpproject.chat.dto.MessageRequest;
import com.elif.mcpproject.chat.dto.SendMessageResponse;
import com.elif.mcpproject.document.Document;
import com.elif.mcpproject.document.DocumentRepository;
import com.elif.mcpproject.security.CurrentUserService;
import com.elif.mcpproject.user.AppUser;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Getter @Setter @Builder
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final DocumentRepository documentRepository;
    private final CurrentUserService currentUserService;

    private final AiMcpClient aiMcpClient;

    @Transactional
    public ChatResponse createChat(Long documentId,String title, Principal principal) {
        AppUser user = currentUserService.requireCurrentUser(principal);

        if (documentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "documentId is required");
        }

        Document doc = documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        String safeTitle = (title == null || title.isBlank()) ? null : title.trim();

        Chat chat = Chat.builder()
                .user(user)
                .document(doc)
                .title(safeTitle)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Chat saved = chatRepository.save(chat);
        return new ChatResponse(
                saved.getId(),
                saved.getDocument() == null ? null : saved.getDocument().getId(),
                saved.getTitle(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ChatResponse> listMyChatsPaged(Pageable pageable, Principal principal) {
        AppUser user = currentUserService.requireCurrentUser(principal);

        return chatRepository
                .findAllByUserIdOrderByUpdatedAtDesc(user.getId(), pageable)
                .map(c -> new ChatResponse(
                        c.getId(),
                        c.getDocument() == null ? null : c.getDocument().getId(),
                        c.getTitle(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()
                ));
    }

    @Transactional
    public void deleteChat(Long chatId, Principal principal) {
        AppUser user = currentUserService.requireCurrentUser(principal);
        Chat chat = chatRepository.findByIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        messageRepository.deleteAllByChatId(chat.getId());
        chatRepository.delete(chat);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessagesPaged(Long chatId, Pageable pageable, Principal principal) {
        AppUser user = currentUserService.requireCurrentUser(principal);
        Chat chat = chatRepository.findByIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        return messageRepository.findAllByChatIdOrderByCreatedAtAsc(chat.getId(), pageable)
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getRole().name(),
                        m.getStatus().name(),
                        m.getContent(),
                        m.getCreatedAt()
                ));
    }


    @Transactional
    public SendMessageResponse sendUserMessage(Long chatId, MessageRequest req, Principal principal) {
        if (req == null || req.content() == null || req.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }

        AppUser user = currentUserService.requireCurrentUser(principal);

        Chat chat = chatRepository.findByIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        if (chat.getDocument() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat must be linked to a document for RAG answer");
        }

        String content = req.content().trim();
        String clientMessageId = (req.clientMessageId() == null || req.clientMessageId().isBlank())
                ? null
                : req.clientMessageId().trim();

        if (clientMessageId != null) {
            var existing = messageRepository.findByClientMessageId(clientMessageId);
            if (existing.isPresent()) {
                Message userMsg = existing.get();
                // Güvenlik: aynı chat mi? (clientMessageId global unique olsa bile)
                if (!userMsg.getChat().getId().equals(chat.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "clientMessageId conflict");
                }

                Message assistantMSg = messageRepository
                        .findFirstByChatIdAndRoleOrderByCreatedAtDesc(chat.getId(), Message.Role.ASSISTANT)
                        .orElse(null);

                return new SendMessageResponse(
                        toResponse(userMsg),
                        assistantMSg == null ? null : toResponse(assistantMSg)
                );


            }
        }

        if (chat.getTitle() == null || chat.getTitle().isBlank()) {
            String title = content.replaceAll("\\s+", " ");
            if (title.length() > 120) title = title.substring(0, 120);
            chat.setTitle(title.isBlank() ? "New chat" : title);
            chatRepository.save(chat); // updated_at DB’de ON UPDATE ise de satır update olur
        }

        Message userMsg = Message.builder()
                .chat(chat)
                .role(Message.Role.USER)
                .status(Message.Status.CREATED) // sende farklıysa uyarlarsın
                .clientMessageId(clientMessageId)
                .content(content)
                .createdAt(Instant.now())
                .build();

        Message savedUser;

        try {
            savedUser = messageRepository.save(userMsg);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (clientMessageId != null) {
                Message m = messageRepository.findByClientMessageId(clientMessageId)
                        .orElseThrow(() -> e);

                if (!m.getChat().getId().equals(chat.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "clientMessageId conflict");
                }

                Message assistantMsg = messageRepository
                        .findFirstByChatIdAndRoleOrderByCreatedAtDesc(chat.getId(), Message.Role.ASSISTANT)
                        .orElse(null);

                return new SendMessageResponse(
                        toResponse(m),
                        assistantMsg == null ? null : toResponse(assistantMsg)
                );
            }
            throw e;
        }

        String assistantText = aiMcpClient.answer(chat.getDocument().getId(), content, 3);

        Message assistantMsg = Message.builder()
                .chat(chat)
                .role(Message.Role.ASSISTANT)
                .status(Message.Status.CREATED)
                .content(assistantText)
                .createdAt(Instant.now())
                .build();

        Message savedAssistant = messageRepository.save(assistantMsg);

        return new SendMessageResponse(
                toResponse(savedUser),
                toResponse(savedAssistant)
        );
    }

    private MessageResponse toResponse(Message m){
        return new MessageResponse(
                m.getId(),
                m.getRole().name(),
                m.getStatus().name(),
                m.getContent(),
                m.getCreatedAt()
        );
    }
}
