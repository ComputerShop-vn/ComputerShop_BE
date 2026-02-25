-- ===============================
-- SBA301 Computer Shop - Database Initialization
-- SQL Server - matches Hibernate entity model (ddl-auto: validate)
-- ===============================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'ComputerShopDB')
BEGIN
    CREATE DATABASE ComputerShopDB;
END
GO

USE ComputerShopDB;
GO

-- ===============================
-- DROP TABLES (reverse FK order)
-- ===============================
IF OBJECT_ID('warranty_claim',             'U') IS NOT NULL DROP TABLE warranty_claim;
IF OBJECT_ID('reviews',                    'U') IS NOT NULL DROP TABLE reviews;
IF OBJECT_ID('order_payment_schedule',     'U') IS NOT NULL DROP TABLE order_payment_schedule;
IF OBJECT_ID('order_items',                'U') IS NOT NULL DROP TABLE order_items;
IF OBJECT_ID('orders',                     'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('cart_items',                 'U') IS NOT NULL DROP TABLE cart_items;
IF OBJECT_ID('carts',                      'U') IS NOT NULL DROP TABLE carts;
IF OBJECT_ID('pc_build_item',              'U') IS NOT NULL DROP TABLE pc_build_item;
IF OBJECT_ID('pc_build',                   'U') IS NOT NULL DROP TABLE pc_build;
IF OBJECT_ID('promotion_product',          'U') IS NOT NULL DROP TABLE promotion_product;
IF OBJECT_ID('promotions',                 'U') IS NOT NULL DROP TABLE promotions;
IF OBJECT_ID('product_images',             'U') IS NOT NULL DROP TABLE product_images;
IF OBJECT_ID('product_variant_attributes', 'U') IS NOT NULL DROP TABLE product_variant_attributes;
IF OBJECT_ID('product_items',              'U') IS NOT NULL DROP TABLE product_items;
IF OBJECT_ID('product_variants',           'U') IS NOT NULL DROP TABLE product_variants;
IF OBJECT_ID('products',                   'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('attributes',                 'U') IS NOT NULL DROP TABLE attributes;
IF OBJECT_ID('blogs',                      'U') IS NOT NULL DROP TABLE blogs;
IF OBJECT_ID('brands',                     'U') IS NOT NULL DROP TABLE brands;
IF OBJECT_ID('categories',                 'U') IS NOT NULL DROP TABLE categories;
IF OBJECT_ID('users',                      'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('roles',                      'U') IS NOT NULL DROP TABLE roles;
IF OBJECT_ID('invalidated_token',          'U') IS NOT NULL DROP TABLE invalidated_token;
GO

-- ===============================
-- CREATE TABLES
-- ===============================

-- roles
CREATE TABLE roles (
    role_id  INT           IDENTITY(1,1) PRIMARY KEY,
    name     NVARCHAR(50)  NOT NULL UNIQUE
);

-- users
CREATE TABLE users (
    user_id       INT           IDENTITY(1,1) PRIMARY KEY,
    username      NVARCHAR(100) NOT NULL UNIQUE,
    email         NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    phone_number  NVARCHAR(20),
    created_at    DATETIME2,
    status        NVARCHAR(50),
    role_id       INT           NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- invalidated_token
CREATE TABLE invalidated_token (
    id          NVARCHAR(255) PRIMARY KEY,
    expiry_time DATETIME2     NOT NULL
);

-- categories  (self-referencing)
CREATE TABLE categories (
    category_id        INT           IDENTITY(1,1) PRIMARY KEY,
    category_name      NVARCHAR(100) NOT NULL,
    parent_category_id INT           NULL,
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);

-- brands
CREATE TABLE brands (
    brand_id   INT           IDENTITY(1,1) PRIMARY KEY,
    brand_name NVARCHAR(100) NOT NULL,
    logo_url   NVARCHAR(500)
);

-- products  (NO price/stock_quantity -- those live on product_variants)
CREATE TABLE products (
    product_id  INT           IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    base_price  FLOAT,
    category_id INT           NOT NULL,
    brand_id    INT           NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id)    REFERENCES brands(brand_id)
);

-- product_variants  (SKU-level stock & price)
CREATE TABLE product_variants (
    variant_id     INT           IDENTITY(1,1) PRIMARY KEY,
    product_id     INT           NOT NULL,
    sku            NVARCHAR(100) NOT NULL UNIQUE,
    price          FLOAT         NOT NULL,
    stock_quantity INT           DEFAULT 0,
    variant_name   NVARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- attributes
CREATE TABLE attributes (
    attribute_id   INT           IDENTITY(1,1) PRIMARY KEY,
    attribute_name NVARCHAR(100) NOT NULL UNIQUE
);

-- product_variant_attributes  (EAV for variant specs)
CREATE TABLE product_variant_attributes (
    variant_attr_id INT           IDENTITY(1,1) PRIMARY KEY,
    variant_id      INT           NOT NULL,
    attribute_id    INT           NOT NULL,
    value           NVARCHAR(500) NOT NULL,
    FOREIGN KEY (variant_id)   REFERENCES product_variants(variant_id),
    FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id)
);

-- product_items  (physical units / serial numbers)
--   product_id is legacy/deprecated (nullable), variant_id is current
CREATE TABLE product_items (
    item_id       INT           IDENTITY(1,1) PRIMARY KEY,
    product_id    INT           NULL,
    variant_id    INT           NULL,
    serial_number NVARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);

-- product_images
CREATE TABLE product_images (
    image_id     INT           IDENTITY(1,1) PRIMARY KEY,
    product_id   INT           NOT NULL,
    image_url    NVARCHAR(500) NOT NULL,
    is_thumbnail BIT           DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- blogs
CREATE TABLE blogs (
    blog_id      INT           IDENTITY(1,1) PRIMARY KEY,
    user_id      INT           NOT NULL,
    title        NVARCHAR(500) NOT NULL,
    content      NVARCHAR(MAX),
    published_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- promotions
CREATE TABLE promotions (
    promotion_id     INT          IDENTITY(1,1) PRIMARY KEY,
    promo_code       NVARCHAR(50) NOT NULL UNIQUE,
    discount_percent INT          NOT NULL,
    start_date       DATE,
    end_date         DATE
);

-- promotion_product
CREATE TABLE promotion_product (
    promo_prod_id INT IDENTITY(1,1) PRIMARY KEY,
    promotion_id  INT NOT NULL,
    product_id    INT NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id),
    FOREIGN KEY (product_id)   REFERENCES products(product_id)
);

-- carts  (1-to-1 with user)
CREATE TABLE carts (
    cart_id    INT       IDENTITY(1,1) PRIMARY KEY,
    user_id    INT       NOT NULL UNIQUE,
    created_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- cart_items  (reference variant, not raw product)
CREATE TABLE cart_items (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id      INT NOT NULL,
    variant_id   INT NOT NULL,
    quantity     INT NOT NULL DEFAULT 1,
    FOREIGN KEY (cart_id)    REFERENCES carts(cart_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);

-- orders
CREATE TABLE orders (
    order_id     INT           IDENTITY(1,1) PRIMARY KEY,
    user_id      INT           NOT NULL,
    total_amount FLOAT,
    status       NVARCHAR(50),
    order_date   DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- order_items
CREATE TABLE order_items (
    order_item_id    INT           IDENTITY(1,1) PRIMARY KEY,
    order_id         INT           NOT NULL,
    item_id          INT           NOT NULL,
    quantity         INT           NOT NULL,
    unit_price       FLOAT         NOT NULL,
    recipient_name   NVARCHAR(200),
    recipient_phone  NVARCHAR(20),
    shipping_address NVARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (item_id)  REFERENCES product_items(item_id)
);

-- order_payment_schedule
CREATE TABLE order_payment_schedule (
    payment_schedule_id INT          IDENTITY(1,1) PRIMARY KEY,
    order_id            INT          NOT NULL,
    provider_name       NVARCHAR(255),
    duration_months     INT,
    interest_rate       FLOAT,
    payment_type        NVARCHAR(20),
    total_amount        FLOAT,
    installment_no      INT,
    amount              FLOAT,
    due_date            DATE,
    paid_date           DATE,
    status              NVARCHAR(20),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- pc_build
CREATE TABLE pc_build (
    build_id    INT           IDENTITY(1,1) PRIMARY KEY,
    user_id     INT           NOT NULL,
    build_name  NVARCHAR(255),
    total_price FLOAT,
    created_at  DATETIME2,
    status      NVARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- pc_build_item
CREATE TABLE pc_build_item (
    build_item_id INT IDENTITY(1,1) PRIMARY KEY,
    build_id      INT NOT NULL,
    product_id    INT NOT NULL,
    quantity      INT NOT NULL,
    FOREIGN KEY (build_id)   REFERENCES pc_build(build_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- reviews
CREATE TABLE reviews (
    review_id  INT           IDENTITY(1,1) PRIMARY KEY,
    user_id    INT           NOT NULL,
    product_id INT           NOT NULL,
    rating     INT           NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment    NVARCHAR(MAX),
    created_at DATETIME2,
    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- warranty_claim
CREATE TABLE warranty_claim (
    claim_id      INT  IDENTITY(1,1) PRIMARY KEY,
    order_item_id INT  NOT NULL,
    reason        NVARCHAR(MAX),
    status        NVARCHAR(50),
    claim_date    DATE,
    FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
);

GO

-- ===============================
-- SEED DATA
-- ===============================

-- Roles  (must match @PreAuthorize: ADMIN, STAFF, MEMBER)
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (role_id, name) VALUES
(1, 'ADMIN'),
(2, 'STAFF'),
(3, 'MEMBER');
SET IDENTITY_INSERT roles OFF;

-- Users  (password = Admin@123 for all)
SET IDENTITY_INSERT users ON;
INSERT INTO users (user_id, username, email, password_hash, phone_number, created_at, status, role_id) VALUES
(1, 'admin',   'admin@computershop.com',  '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0901234567', GETDATE(), 'ACTIVE', 1),
(2, 'staff1',  'staff1@computershop.com', '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0902345678', GETDATE(), 'ACTIVE', 2),
(3, 'member1', 'member1@example.com',     '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0903456789', GETDATE(), 'ACTIVE', 3),
(4, 'member2', 'member2@example.com',     '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0904567890', GETDATE(), 'ACTIVE', 3);
SET IDENTITY_INSERT users OFF;

-- Categories
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (category_id, category_name, parent_category_id) VALUES
(1,  'CPU',        NULL),
(2,  'GPU',        NULL),
(3,  'Mainboard',  NULL),
(4,  'RAM',        NULL),
(5,  'SSD',        NULL),
(6,  'HDD',        NULL),
(7,  'PSU',        NULL),
(8,  'Case',       NULL),
(9,  'Cooling',    NULL),
(10, 'Monitor',    NULL),
(11, 'Keyboard',   NULL),
(12, 'Mouse',      NULL),
(13, 'Intel CPU',  1),
(14, 'AMD CPU',    1),
(15, 'NVIDIA GPU', 2),
(16, 'AMD GPU',    2);
SET IDENTITY_INSERT categories OFF;

-- Brands
SET IDENTITY_INSERT brands ON;
INSERT INTO brands (brand_id, brand_name) VALUES
(1,  'Intel'),
(2,  'AMD'),
(3,  'NVIDIA'),
(4,  'ASUS'),
(5,  'MSI'),
(6,  'Corsair'),
(7,  'Kingston'),
(8,  'Samsung'),
(9,  'Logitech'),
(10, 'NZXT');
SET IDENTITY_INSERT brands OFF;

-- Attributes
SET IDENTITY_INSERT attributes ON;
INSERT INTO attributes (attribute_id, attribute_name) VALUES
(1,  'Core Count'),
(2,  'Thread Count'),
(3,  'Base Clock'),
(4,  'Boost Clock'),
(5,  'TDP'),
(6,  'Socket'),
(7,  'Memory Size'),
(8,  'Memory Type'),
(9,  'Memory Bus'),
(10, 'Memory Speed'),
(11, 'Form Factor'),
(12, 'Interface'),
(13, 'Read Speed'),
(14, 'Write Speed'),
(15, 'Wattage'),
(16, 'Efficiency');
SET IDENTITY_INSERT attributes OFF;

-- Products
SET IDENTITY_INSERT products ON;
INSERT INTO products (product_id, name, description, base_price, category_id, brand_id) VALUES
(1,  'Intel Core i9-14900K',          'Top-end Intel desktop CPU, 24 cores (8P+16E), LGA1700.',         559.99,  13, 1),
(2,  'AMD Ryzen 9 7950X',             'Flagship Ryzen CPU, 16 cores / 32 threads, AM5 socket.',          699.99,  14, 2),
(3,  'NVIDIA RTX 4090',               'Flagship Ada Lovelace GPU, 24 GB GDDR6X.',                       1599.99,  15, 3),
(4,  'AMD Radeon RX 7900 XTX',        'High-end RDNA3 GPU, 24 GB GDDR6.',                                999.99,  16, 2),
(5,  'ASUS ROG Strix Z790-E Gaming',  'Premium Z790 ATX motherboard for LGA1700.',                       499.99,   3, 4),
(6,  'Corsair Vengeance DDR5',        'High-speed DDR5 memory kit.',                                      89.99,   4, 6),
(7,  'Samsung 990 PRO NVMe SSD',      'PCIe 4.0 NVMe SSD, up to 7450 MB/s read.',                       119.99,   5, 8),
(8,  'Corsair RM1000x',               '1000W 80+ Gold fully-modular ATX PSU.',                           189.99,   7, 6),
(9,  'NZXT H7 Flow',                  'Mid-tower ATX case, mesh front panel.',                           109.99,   8, 10),
(10, 'Logitech G Pro X Superlight 2', 'Ultra-light wireless gaming mouse, 60g.',                         159.99,  12, 9);
SET IDENTITY_INSERT products OFF;

-- Product Variants
SET IDENTITY_INSERT product_variants ON;
INSERT INTO product_variants (variant_id, product_id, sku, price, stock_quantity, variant_name) VALUES
(1,  1,  'CPU-I9-14900K-BOX',    589.99, 20, 'i9-14900K Box (with cooler)'),
(2,  1,  'CPU-I9-14900K-TRAY',   559.99, 15, 'i9-14900K Tray (no cooler)'),
(3,  2,  'CPU-R9-7950X-BOX',     699.99, 18, 'Ryzen 9 7950X Box'),
(4,  3,  'GPU-RTX4090-ASUS',    1799.99,  8, 'ASUS ROG STRIX RTX 4090 24GB OC'),
(5,  3,  'GPU-RTX4090-MSI',     1699.99,  6, 'MSI GAMING TRIO RTX 4090 24GB'),
(6,  3,  'GPU-RTX4090-FE',      1599.99,  5, 'NVIDIA Founders Edition RTX 4090'),
(7,  4,  'GPU-RX7900XTX-REF',    999.99, 12, 'AMD Reference RX 7900 XTX 24GB'),
(8,  5,  'MB-ROGZ790E-ATX',      499.99, 25, 'ROG Strix Z790-E ATX'),
(9,  6,  'RAM-DDR5-16G-6000',     89.99, 50, 'Corsair Vengeance DDR5 16GB 6000MHz'),
(10, 6,  'RAM-DDR5-32G-6000',    159.99, 40, 'Corsair Vengeance DDR5 32GB 6000MHz'),
(11, 6,  'RAM-DDR5-64G-6000',    299.99, 20, 'Corsair Vengeance DDR5 64GB 6000MHz'),
(12, 7,  'SSD-990PRO-1TB',       119.99, 35, 'Samsung 990 PRO 1TB NVMe'),
(13, 7,  'SSD-990PRO-2TB',       199.99, 30, 'Samsung 990 PRO 2TB NVMe'),
(14, 7,  'SSD-990PRO-4TB',       349.99, 15, 'Samsung 990 PRO 4TB NVMe'),
(15, 8,  'PSU-RM1000X-2024',     189.99, 30, 'Corsair RM1000x 1000W 80+ Gold'),
(16, 9,  'CASE-H7FLOW-BLACK',    109.99, 40, 'NZXT H7 Flow Black'),
(17, 9,  'CASE-H7FLOW-WHITE',    119.99, 25, 'NZXT H7 Flow White'),
(18, 10, 'MOUSE-GPXSL2-BLACK',   159.99, 50, 'G Pro X Superlight 2 Black'),
(19, 10, 'MOUSE-GPXSL2-WHITE',   159.99, 45, 'G Pro X Superlight 2 White');
SET IDENTITY_INSERT product_variants OFF;

-- Product Variant Attributes
SET IDENTITY_INSERT product_variant_attributes ON;
INSERT INTO product_variant_attributes (variant_attr_id, variant_id, attribute_id, value) VALUES
(1,  1,  1,  '24 (8P+16E)'),  (2,  1,  2,  '32'),
(3,  1,  3,  '3.2 GHz'),      (4,  1,  4,  '6.0 GHz'),
(5,  1,  5,  '125W'),          (6,  1,  6,  'LGA1700'),
(7,  2,  1,  '24 (8P+16E)'),  (8,  2,  2,  '32'),
(9,  2,  3,  '3.2 GHz'),      (10, 2,  4,  '6.0 GHz'),
(11, 2,  5,  '125W'),          (12, 2,  6,  'LGA1700'),
(13, 3,  1,  '16'),            (14, 3,  2,  '32'),
(15, 3,  3,  '4.5 GHz'),      (16, 3,  4,  '5.7 GHz'),
(17, 3,  5,  '170W'),          (18, 3,  6,  'AM5'),
(19, 6,  7,  '24 GB'),         (20, 6,  8,  'GDDR6X'),
(21, 6,  9,  '384-bit'),       (22, 6,  5,  '450W'),
(23, 7,  7,  '24 GB'),         (24, 7,  8,  'GDDR6'),
(25, 7,  9,  '384-bit'),       (26, 7,  5,  '355W'),
(27, 10, 7,  '32 GB'),         (28, 10, 8,  'DDR5'),
(29, 10, 10, '6000 MHz'),
(30, 13, 7,  '2 TB'),          (31, 13, 12, 'PCIe 4.0 NVMe M.2'),
(32, 13, 13, '7450 MB/s'),     (33, 13, 14, '6900 MB/s'),
(34, 15, 15, '1000W'),         (35, 15, 16, '80+ Gold');
SET IDENTITY_INSERT product_variant_attributes OFF;

-- Product Images
SET IDENTITY_INSERT product_images ON;
INSERT INTO product_images (image_id, product_id, image_url, is_thumbnail) VALUES
(1,  1,  'https://placehold.co/600x400?text=i9-14900K',      1),
(2,  2,  'https://placehold.co/600x400?text=R9-7950X',       1),
(3,  3,  'https://placehold.co/600x400?text=RTX-4090',       1),
(4,  4,  'https://placehold.co/600x400?text=RX-7900-XTX',    1),
(5,  5,  'https://placehold.co/600x400?text=Z790-E',         1),
(6,  6,  'https://placehold.co/600x400?text=DDR5-RAM',       1),
(7,  7,  'https://placehold.co/600x400?text=990-PRO-SSD',    1),
(8,  8,  'https://placehold.co/600x400?text=RM1000x-PSU',    1),
(9,  9,  'https://placehold.co/600x400?text=NZXT-H7-Flow',   1),
(10, 10, 'https://placehold.co/600x400?text=GPX-SL2-Mouse',  1);
SET IDENTITY_INSERT product_images OFF;

-- Blogs
SET IDENTITY_INSERT blogs ON;
INSERT INTO blogs (blog_id, user_id, title, content, published_at) VALUES
(1, 1, 'Top PC Builds for 2026',              'A roundup of the best value builds using 2026 hardware.',       GETDATE()),
(2, 2, 'DDR5 vs DDR4: Is the Upgrade Worth?', 'Detailed comparison of DDR5 and DDR4 performance in 2026.',     GETDATE()),
(3, 3, 'How to Pick the Right GPU in 2026',   'Tips for choosing a GPU based on resolution and budget.',       GETDATE());
SET IDENTITY_INSERT blogs OFF;

-- Promotions
SET IDENTITY_INSERT promotions ON;
INSERT INTO promotions (promotion_id, promo_code, discount_percent, start_date, end_date) VALUES
(1, 'SALE2026',    15, '2026-01-01', '2026-12-31'),
(2, 'NEWMEMBER10', 10, '2026-01-01', '2026-12-31'),
(3, 'SUMMER25',    25, '2026-06-01', '2026-08-31');
SET IDENTITY_INSERT promotions OFF;

-- Promotion Products
SET IDENTITY_INSERT promotion_product ON;
INSERT INTO promotion_product (promo_prod_id, promotion_id, product_id) VALUES
(1, 1, 3), (2, 1, 4),
(3, 2, 6), (4, 2, 7),
(5, 3, 1), (6, 3, 2);
SET IDENTITY_INSERT promotion_product OFF;

-- Reviews
SET IDENTITY_INSERT reviews ON;
INSERT INTO reviews (review_id, user_id, product_id, rating, comment, created_at) VALUES
(1, 3, 1, 5, 'Blazing fast CPU, handles everything at max settings.', GETDATE()),
(2, 3, 3, 5, 'Overkill for gaming but the performance is incredible.',GETDATE()),
(3, 4, 7, 4, 'Very fast SSD, great value for the 2TB capacity.',      GETDATE()),
(4, 4,10, 5, 'Best wireless mouse ever. Zero noticeable latency.',    GETDATE()),
(5, 3, 6, 4, 'Solid DDR5 kit, easy XMP/EXPO setup in BIOS.',         GETDATE());
SET IDENTITY_INSERT reviews OFF;

GO

PRINT '========================================';
PRINT 'Database initialized successfully!';
PRINT 'All passwords: Admin@123';
PRINT '  admin@computershop.com  -> ADMIN';
PRINT '  staff1@computershop.com -> STAFF';
PRINT '  member1@example.com     -> MEMBER';
PRINT '  member2@example.com     -> MEMBER';
PRINT '========================================';
GO