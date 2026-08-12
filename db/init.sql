CREATE DATABASE IF NOT EXISTS common_db;

USE common_db;

-- CREATE TABLE users
-- (
--     id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
--     username   VARCHAR(100) NOT NULL,
--     email      VARCHAR(255) NOT NULL,
--     created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
--
--     PRIMARY KEY (id),
--     UNIQUE KEY uq_users_email (email)
-- );

CREATE TABLE products
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name             VARCHAR(100) NOT NULL,
    description      TEXT NOT NULL DEFAULT '',
    price            INT     NOT NULL DEFAULT 0,
    price_unit        VARCHAR(8) NOT NULL DEFAULT '$',

    PRIMARY KEY (id),
    UNIQUE KEY p_name (name)
);
