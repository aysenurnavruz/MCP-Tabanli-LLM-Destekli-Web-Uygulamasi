package com.elif.mcpproject.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByChatIdOrderByCreatedAtAsc(Long chatId, Pageable pageable);
    Optional<Message> findByClientMessageId(String clientMessageId);
    Optional<Message> findFirstByChatIdAndRoleOrderByCreatedAtDesc(Long chatId, Message.Role role);
    void deleteAllByChatId(Long chatId);
}
