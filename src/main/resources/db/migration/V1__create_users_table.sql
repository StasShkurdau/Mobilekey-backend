CREATE TABLE users
(
    id       UUID         NOT NULL PRIMARY KEY,
    login    VARCHAR(255) NOT NULL UNIQUE,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
