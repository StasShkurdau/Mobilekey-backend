CREATE TABLE file
(
    id           UUID         NOT NULL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    path         VARCHAR(512) NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL
);

ALTER TABLE user_profile
    ADD COLUMN avatar_id UUID REFERENCES file (id);
