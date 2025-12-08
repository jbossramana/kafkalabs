1. copy plugins folder inside kafka/bin/window

2. copy connect-standalone.properties, file-sink.properties, mysql-source.properties in /config folder

3. create table and records in MySQL

CREATE DATABASE testdb;
USE testdb;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    city VARCHAR(50)
);

INSERT INTO users(name, city) VALUES 
('Ram', 'Hyderabad'),
('Arjun', 'Bangalore');


4. Run Both Connectors Together

connect-standalone.bat ^
..\config\connect\connect-standalone.properties ^
..\config\connect\mysql-source.properties ^
..\config\connect\file-sink.properties





Note: To Download ZIP:
👉 https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc
