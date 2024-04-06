CREATE DATABASE IF NOT EXISTS users_credentials;

DELETE FROM mysql.user WHERE User='';

CREATE USER 'ocr'@'%' IDENTIFIED BY 'password';

GRANT SELECT, INSERT ON users_credentials.* TO 'ocr'@'%';

CREATE USER 'ocr'@localhost IDENTIFIED BY 'password';

GRANT SELECT, INSERT ON users_credentials.* TO 'ocr'@localhost;

FLUSH PRIVILEGES;

-- Create table

USE users_credentials;

CREATE TABLE IF NOT EXISTS application_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    authority VARCHAR(255) NOT NULL
);

-- Create sequence for id

CREATE SEQUENCE IF NOT EXISTS application_user_seq START WITH 1 INCREMENT BY 1;