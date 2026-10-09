CREATE DATABASE IF NOT EXISTS student_marketplace;
CREATE USER IF NOT EXISTS 'peninsula_app'@'localhost' IDENTIFIED BY 'peninsula123';
GRANT ALL PRIVILEGES ON student_marketplace.* TO 'peninsula_app'@'localhost';
FLUSH PRIVILEGES;