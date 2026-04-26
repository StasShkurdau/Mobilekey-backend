CREATE TABLE update_avatar_request (
    id           UUID         NOT NULL PRIMARY KEY,
    file_id      UUID         NOT NULL,
    target_path  VARCHAR(512) NOT NULL,
    new_status   VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    processed_at TIMESTAMP    NULL
);
