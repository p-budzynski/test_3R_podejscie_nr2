--liquibase formatted sql
--changeset test_3R:1

CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255)
);


CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category_fk BIGINT NOT NULL,
    page_count INT,
    author_fk BIGINT NOT NULL,
    created_at DATE NOT NULL
);


CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    city VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255)
);


CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    client_fk BIGINT NOT NULL,
    author_fk BIGINT,
    category_fk BIGINT,
    CONSTRAINT fk_sub_client FOREIGN KEY (client_fk) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_author FOREIGN KEY (author_fk) REFERENCES authors(id),
    CONSTRAINT fk_sub_category FOREIGN KEY (category_fk) REFERENCES categories(id)
);

CREATE INDEX idx_sub_author ON subscriptions(author_fk);
CREATE INDEX idx_sub_category ON subscriptions(category_fk);
CREATE INDEX idx_sub_client_author ON subscriptions(client_fk, author_fk);
CREATE INDEX idx_sub_client_category ON subscriptions(client_fk, category_fk);


CREATE TABLE message_config (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    body VARCHAR(255) NOT NULL
);

