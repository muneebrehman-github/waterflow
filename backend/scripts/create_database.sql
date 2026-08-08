-- Run this once against your local MariaDB server before starting the
-- backend jar. It only creates the empty database — Flyway creates all the
-- actual tables (and seeds dummy data) automatically the first time the
-- backend boots against it.
--
-- Example: mysql -u root -p < create_database.sql
--      or: mariadb -u root -p < create_database.sql

CREATE DATABASE IF NOT EXISTS waterflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
