-- Run ONCE by a MySQL administrator (for example: mysql -u root -p < createDB.sql),
-- after replacing <SENHA> with the password chosen for the application user.
-- The same password must be given to the application in PETCLINIC_DB_PASSWORD.
-- Do not commit this file with a real password. Requires MySQL 5.7.6 or later.

CREATE DATABASE IF NOT EXISTS petclinic CHARACTER SET utf8;

CREATE USER IF NOT EXISTS 'petclinic'@'localhost' IDENTIFIED BY '<SENHA>';

-- Least privilege: the application only creates its tables (initDB.sql) and manipulates data.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, REFERENCES ON petclinic.* TO 'petclinic'@'localhost';
