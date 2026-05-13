CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT true,
                       account_non_expired BOOLEAN NOT NULL DEFAULT true,
                       account_non_locked BOOLEAN NOT NULL DEFAULT true,
                       credentials_non_expired BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE authority (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           role VARCHAR(50) NOT NULL
);

CREATE TABLE user_authority (
                                user_id BIGINT,
                                authority_id BIGINT,
                                FOREIGN KEY (user_id) REFERENCES users(id),
                                FOREIGN KEY (authority_id) REFERENCES authority(id),
                                PRIMARY KEY (user_id, authority_id)
);