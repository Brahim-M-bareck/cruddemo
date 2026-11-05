-- TP 03 - Hibernate & JPA : CRUD avec l'EntityManager
-- Script de reference (non execute automatiquement par Spring Boot).
-- A executer manuellement avec : mysql -u root -p < 02-student-tracker.sql

CREATE DATABASE IF NOT EXISTS student_tracker;
USE student_tracker;

CREATE USER IF NOT EXISTS 'springstudent'@'localhost' IDENTIFIED BY 'springstudent';
GRANT ALL PRIVILEGES ON student_tracker.* TO 'springstudent'@'localhost';

DROP TABLE IF EXISTS student;

CREATE TABLE student (
    id         INT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(45) DEFAULT NULL,
    last_name  VARCHAR(45) DEFAULT NULL,
    email      VARCHAR(45) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
