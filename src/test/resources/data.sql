--CREATE TABLE IF NOT EXISTS product (
--    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    product_name VARCHAR(255),
--    price DOUBLE
--);
--
--INSERT INTO product (product_name, price) VALUES ('Test Product 1', 100.0);
--INSERT INTO product (product_name, price) VALUES ('Test Product 2', 200.0);
-- Step 1: Drop the existing table (if it already exists)
DROP TABLE IF EXISTS product;

-- Step 2: Create the table without AUTO_INCREMENT
CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT PRIMARY KEY,  -- Removed AUTO_INCREMENT
    product_name VARCHAR(255),
    price DOUBLE
);

INSERT INTO product (product_id, product_name, price) VALUES (1, 'Test Product 1', 100.0);
INSERT INTO product (product_id, product_name, price) VALUES (2, 'Test Product 2', 200.0);
--
