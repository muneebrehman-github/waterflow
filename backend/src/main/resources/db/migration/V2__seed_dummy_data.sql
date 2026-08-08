-- Dummy data for local testing/dev only — applied automatically by Flyway on
-- startup, same as V1. Not seeded: Super Admin — that's auto-created at
-- runtime by SuperAdminBootstrap (see application.yml waterflow.bootstrap.*).
--
-- All seeded accounts share the password: Password123!

-- Shop 1: already approved, owner can log in immediately.
INSERT INTO users (full_name, email, phone, password_hash, role, status)
VALUES ('Ali Khan', 'owner1@example.com', '03001234567',
        '$2a$12$HVMzNW9i1t.JrxkCYvg1se3Lyl3qUrl0TSdPO/GuIlH/.hiSTGZj.', 'SHOP_OWNER', 'ACTIVE');
SET @owner1_id = LAST_INSERT_ID();

INSERT INTO shops (owner_user_id, name, description, owner_name, email, phone, address, status)
VALUES (@owner1_id, 'Clear Springs Water', 'Purified water delivery', 'Ali Khan',
        'owner1@example.com', '03001234567', 'Model Town, Lahore', 'ACTIVE');
SET @shop1_id = LAST_INSERT_ID();

UPDATE users SET tenant_id = @shop1_id WHERE id = @owner1_id;

-- Shop 2: still pending Super Admin approval — log in as Super Admin to
-- approve/reject it and see the owner's login gate flip accordingly.
INSERT INTO users (full_name, email, phone, password_hash, role, status)
VALUES ('Sara Malik', 'owner2@example.com', '03009876543',
        '$2a$12$HVMzNW9i1t.JrxkCYvg1se3Lyl3qUrl0TSdPO/GuIlH/.hiSTGZj.', 'SHOP_OWNER', 'PENDING');
SET @owner2_id = LAST_INSERT_ID();

INSERT INTO shops (owner_user_id, name, description, owner_name, email, phone, address, status)
VALUES (@owner2_id, 'Fresh Drop Water Co', 'On-demand pure water delivery', 'Sara Malik',
        'owner2@example.com', '03009876543', 'DHA Phase 5, Lahore', 'PENDING_APPROVAL');
SET @shop2_id = LAST_INSERT_ID();

UPDATE users SET tenant_id = @shop2_id WHERE id = @owner2_id;
