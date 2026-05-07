package com.elif.mcpproject.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByIdAndUserId(Long id, Long userId);
    Page<Chat> findAllByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    List<Chat> findAllByDocumentIdAndUserId(Long documentId, Long userId);
}
