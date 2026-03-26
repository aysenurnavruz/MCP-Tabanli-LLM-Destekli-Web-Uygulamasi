CREATE TABLE chats (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       user_id BIGINT NOT NULL,
                       document_id BIGINT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_chats_user FOREIGN KEY (user_id) REFERENCES users(id),
                       CONSTRAINT fk_chats_document FOREIGN KEY (document_id) REFERENCES documents(id)
);

CREATE INDEX idx_chats_user_created ON chats(user_id, created_at);
CREATE INDEX idx_chats_document_created ON chats(document_id, created_at);

CREATE TABLE messages (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          chat_id BIGINT NOT NULL,
                          role VARCHAR(20) NOT NULL,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_messages_chat FOREIGN KEY (chat_id) REFERENCES chats(id)
);

CREATE INDEX idx_messages_chat_created ON messages(chat_id, created_at);