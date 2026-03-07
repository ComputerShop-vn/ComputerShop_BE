-- Turn off IDENTITY_INSERT for safety at the end, but explicitly allow it when inserting.
-- Run these inserts against the ComputerShopDB
-- 1. Insert Roles
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (role_id, name)
VALUES (1, 'MEMBER'),
       (2, 'STAFF'),
       (3, 'ADMIN');
SET IDENTITY_INSERT roles OFF;
-- 2. Insert Brands
SET IDENTITY_INSERT brands ON;
INSERT INTO brands (brand_id, brand_name, logo_url)
VALUES (1, 'ASUS', 'https://example.com/asus.png'),
       (2, 'Dell', 'https://example.com/dell.png'),
       (3, 'Apple', 'https://example.com/apple.png'),
       (4, 'HP', 'https://example.com/hp.png');
SET IDENTITY_INSERT brands OFF;
-- 3. Insert Categories
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (category_id, category_name, parent_category_id)
VALUES (1, 'Laptops', NULL),
       (2, 'Gaming Laptops', 1),
       (3, 'Office Laptops', 1),
       (4, 'Accessories', NULL);
SET IDENTITY_INSERT categories OFF;
-- 4. Insert Attributes
SET IDENTITY_INSERT attributes ON;
INSERT INTO attributes (attribute_id, attribute_name)
VALUES (1, 'Color'),
       (2, 'RAM'),
       (3, 'Storage'),
       (4, 'CPU');
SET IDENTITY_INSERT attributes OFF;
-- 5. Insert Users (Password is '123456')
-- Password below is the existing bcrypt hash as requested.
SET IDENTITY_INSERT users ON;
INSERT INTO users (
    user_id,
    username,
    email,
    password_hash,
    phone_number,
    created_at,
    status,
    role_id
)
VALUES (
           1,
           'admin_user',
           'admin@gmail.com',
           '$2a$10$26x1YTOi/QxS3UxN7tzKQuI/B/7jv0jbjoHDll7OtxCoPmcFUAZZK',
           '0123456789',
           CURRENT_TIMESTAMP,
           'ACTIVE',
           3
       ),
       (
           2,
           'regular_user',
           'user@gmail.com',
           '$2a$10$26x1YTOi/QxS3UxN7tzKQuI/B/7jv0jbjoHDll7OtxCoPmcFUAZZK',
           '0987654321',
           CURRENT_TIMESTAMP,
           'ACTIVE',
           1
       );
SET IDENTITY_INSERT users OFF;
-- 6. Insert Products
SET IDENTITY_INSERT products ON;
INSERT INTO products (
    product_id,
    brand_id,
    category_id,
    name,
    base_price,
    description
)
VALUES (
           1,
           1,
           2,
           'ASUS ROG Strix G15',
           12000000.00,
           'High performance gaming laptop with excellent cooling.'
       ),
       (
           2,
           3,
           3,
           'MacBook Air M2',
           1100000.00,
           'Thin, light, and powerful with M2 chip.'
       );
SET IDENTITY_INSERT products OFF;
-- 7. Insert Product Variants
SET IDENTITY_INSERT product_variants ON;
INSERT INTO product_variants (
    variant_id,
    product_id,
    sku,
    price,
    stock_quantity,
    variant_name
)
VALUES (
           1,
           1,
           'ROG-G15-16GB-512GB',
           120000.00,
           50,
           'ROG Strix G15 16GB RAM 512GB SSD'
       ),
       (
           2,
           1,
           'ROG-G15-32GB-1TB',
           150000.00,
           20,
           'ROG Strix G15 32GB RAM 1TB SSD'
       ),
       (
           3,
           2,
           'MBA-M2-8GB-256GB',
           110000.00,
           100,
           'MacBook Air M2 8GB 256GB Midnight'
       );
SET IDENTITY_INSERT product_variants OFF;
-- 8. Insert Product Variant Attributes
SET IDENTITY_INSERT product_variant_attributes ON;
INSERT INTO product_variant_attributes (variant_attr_id, variant_id, attribute_id, value)
VALUES (1, 1, 2, '16GB'),
       -- RAM
       (2, 1, 3, '512GB NVMe'),
       -- Storage
       (3, 2, 2, '32GB'),
       -- RAM
       (4, 2, 3, '1TB NVMe'),
       -- Storage
       (5, 3, 2, '8GB unified'),
       -- RAM
       (6, 3, 3, '256GB SSD'),
       -- Storage
       (7, 3, 1, 'Midnight');
-- Color
SET IDENTITY_INSERT product_variant_attributes OFF;
-- 9. Insert Product Images
SET IDENTITY_INSERT product_images ON;
INSERT INTO product_images (image_id, product_id, image_url, is_thumbnail)
VALUES (1, 1, 'https://example.com/rog-strix.jpg', 1),
       (2, 2, 'https://example.com/macbook-air.jpg', 1);
SET IDENTITY_INSERT product_images OFF;
-- 10. Insert Product Items (Serial Numbers)
-- Added product_id here to match entity even if it's deprecated
SET IDENTITY_INSERT product_items ON;
INSERT INTO product_items (item_id, product_id, variant_id, serial_number)
VALUES (1, 1, 1, 'SN-ROG001'),
       (2, 1, 1, 'SN-ROG002'),
       (3, 1, 2, 'SN-ROG003'),
       (4, 2, 3, 'SN-MBA001');
SET IDENTITY_INSERT product_items OFF;
-- 11. Insert Installment Packages
SET IDENTITY_INSERT installment_package ON;
INSERT INTO installment_package (
    package_id,
    name,
    duration_months,
    interest_rate,
    min_order_amount,
    is_active
)
VALUES (
           1,
           N'Trả góp 3 tháng - Lãi suất 0%',
           3,
           0.0,
           3000000.00,
           1
       ),
       (
           2,
           N'Trả góp 6 tháng - Lãi suất 0%',
           6,
           1.0,
           5000000.00,
           1
       ),
       (
           3,
           N'Trả góp 12 tháng - Lãi suất 1.5%',
           12,
           1.5,
           10000000.00,
           1
       ),
       (
           4,
           N'Trả góp 9 tháng - Không hoạt động',
           9,
           1.5,
           5000000.00,
           0
       );
SET IDENTITY_INSERT installment_package OFF;