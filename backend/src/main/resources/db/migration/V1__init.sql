CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(190) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(30) NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
