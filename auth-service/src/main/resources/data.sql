-- CREATE TABLE IF NOT EXISTS "users" (
--                                        id UUID PRIMARY KEY,
--                                        email VARCHAR(255) UNIQUE NOT NULL,
--     password VARCHAR(255) NOT NULL,
--     role VARCHAR(50) NOT NULL
--     );
--
-- -- Insert the user if no existing user with the same id or email exists
-- INSERT INTO users (id, email, password, role)
-- SELECT '2', 'testuser@test.com',
--        '$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu', 'ADMIN'
--     WHERE NOT EXISTS (
--     SELECT 1
--     FROM users
--     WHERE id = '2'
--        OR email = 'testuser@test.com'
-- );


