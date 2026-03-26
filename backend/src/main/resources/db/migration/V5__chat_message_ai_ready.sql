ALTER TABLE chats
    ADD COLUMN title VARCHAR(120) NULL AFTER document_id,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
    AFTER created_at;

CREATE INDEX idx_chats_user_updated
    ON chats(user_id, updated_at);

ALTER TABLE messages
    ADD COLUMN client_message_id VARCHAR(36) NULL AFTER chat_id,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'CREATED' AFTER role;


CREATE UNIQUE INDEX uq_messages_client_message_id
    ON messages(client_message_id);


CREATE INDEX idx_messages_chat_status_created
    ON messages(chat_id, status, created_at);
