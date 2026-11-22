-- Run ONCE by a MySQL administrator (for example: mysql -u root -p < createDB.sql),
-- after replacing <SENHA> with the password chosen for the application user and
-- <HOST> with the host from which the application connects:
--   * MySQL installed on the same machine as the application: localhost
--   * MySQL in a Docker container, application on the host: the address the MySQL
--     container sees as the client, NOT localhost (for example 172.17.0.1). A wrong
--     host makes the connection fail with "Access denied for user 'petclinic'@'<address>'";
--     that message shows the address to use. Do not use '%' outside a throwaway test.
-- The same password must be given to the application in PETCLINIC_DB_PASSWORD.
-- Do not commit this file with a real password. Requires MySQL 5.7.6 or later.

CREATE DATABASE IF NOT EXISTS petclinic CHARACTER SET utf8;

CREATE USER IF NOT EXISTS 'petclinic'@'<HOST>' IDENTIFIED BY '<SENHA>';

-- Least privilege: the application only creates its tables (initDB.sql) and manipulates data.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, REFERENCES ON petclinic.* TO 'petclinic'@'<HOST>';
