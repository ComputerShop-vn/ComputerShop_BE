-- ===============================
-- SBA301 Computer Shop - Database Initialization
-- ===============================

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'ComputerShopDB')
BEGIN
    CREATE DATABASE ComputerShopDB;
END
GO

USE ComputerShopDB;
GO

-- ===============================
-- DROP TABLES (if exists)
-- ===============================
IF OBJECT_ID('cart_items', 'U') IS NOT NULL DROP TABLE cart_items;
IF OBJECT_ID('carts', 'U') IS NOT NULL DROP TABLE carts;
IF OBJECT_ID('order_items', 'U') IS NOT NULL DROP TABLE order_items;
IF OBJECT_ID('order_payment_schedule', 'U') IS NOT NULL DROP TABLE order_payment_schedule;
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('promotion_product', 'U') IS NOT NULL DROP TABLE promotion_product;
IF OBJECT_ID('promotions', 'U') IS NOT NULL DROP TABLE promotions;
IF OBJECT_ID('product_images', 'U') IS NOT NULL DROP TABLE product_images;
IF OBJECT_ID('product_attribute', 'U') IS NOT NULL DROP TABLE product_attribute;
IF OBJECT_ID('product_items', 'U') IS NOT NULL DROP TABLE product_items;
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('attributes', 'U') IS NOT NULL DROP TABLE attributes;
IF OBJECT_ID('reviews', 'U') IS NOT NULL DROP TABLE reviews;
IF OBJECT_ID('warranty_claim', 'U') IS NOT NULL DROP TABLE warranty_claim;
IF OBJECT_ID('blogs', 'U') IS NOT NULL DROP TABLE blogs;
IF OBJECT_ID('pc_build_item', 'U') IS NOT NULL DROP TABLE pc_build_item;
IF OBJECT_ID('pc_build', 'U') IS NOT NULL DROP TABLE pc_build;
IF OBJECT_ID('brands', 'U') IS NOT NULL DROP TABLE brands;
IF OBJECT_ID('categories', 'U') IS NOT NULL DROP TABLE categories;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('roles', 'U') IS NOT NULL DROP TABLE roles;
IF OBJECT_ID('invalidated_token', 'U') IS NOT NULL DROP TABLE invalidated_token;
GO

-- ===============================
-- CREATE TABLES
-- ===============================

-- Roles
CREATE TABLE roles (
    role_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);

-- Users
CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(100) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    phone_number NVARCHAR(20),
    created_at DATETIME2,
    status NVARCHAR(50),
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Invalidated Token
CREATE TABLE invalidated_token (
    id NVARCHAR(255) PRIMARY KEY,
    expiry_time DATETIME2 NOT NULL
);

-- Categories
CREATE TABLE categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL UNIQUE,
    parent_category_id INT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);

-- Brands
CREATE TABLE brands (
    brand_id INT IDENTITY(1,1) PRIMARY KEY,
    brand_name NVARCHAR(100) NOT NULL UNIQUE,
    logo_url NVARCHAR(500),
    website NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Products
CREATE TABLE products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    category_id INT NOT NULL,
    brand_id INT NOT NULL,
    price FLOAT NOT NULL,
    stock_quantity INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id)
);

-- Product Items
CREATE TABLE product_items (
    item_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    serial_number NVARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Product Images
CREATE TABLE product_images (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    is_thumbnail BIT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Attributes
CREATE TABLE attributes (
    attribute_id INT IDENTITY(1,1) PRIMARY KEY,
    attribute_name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Product Attributes
CREATE TABLE product_attribute (
    prod_attr_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    attribute_id INT NOT NULL,
    value NVARCHAR(500),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id)
);

-- Promotions
CREATE TABLE promotions (
    promotion_id INT IDENTITY(1,1) PRIMARY KEY,
    promo_code NVARCHAR(50) NOT NULL UNIQUE,
    discount_percent INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

-- Promotion Product
CREATE TABLE promotion_product (
    promo_prod_id INT IDENTITY(1,1) PRIMARY KEY,
    promotion_id INT NOT NULL,
    product_id INT NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Carts
CREATE TABLE carts (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Cart Items
CREATE TABLE cart_items (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Orders
CREATE TABLE orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount FLOAT,
    status NVARCHAR(50),
    order_date DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Order Items
CREATE TABLE order_items (
    order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price FLOAT NOT NULL,
    recipient_name NVARCHAR(200),
    recipient_phone NVARCHAR(20),
    shipping_address NVARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (item_id) REFERENCES product_items(item_id)
);

-- Order Payment Schedule
CREATE TABLE order_payment_schedule (
    payment_schedule_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    provider_name NVARCHAR(255),
    duration_months INT,
    interest_rate FLOAT,
    payment_type NVARCHAR(20),
    total_amount FLOAT,
    installment_no INT,
    amount FLOAT,
    due_date DATE,
    paid_date DATE,
    status NVARCHAR(20),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- PC Build
CREATE TABLE pc_build (
    build_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    build_name NVARCHAR(255),
    total_price FLOAT,
    created_at DATETIME2,
    status NVARCHAR(20),
    description NVARCHAR(MAX),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- PC Build Item
CREATE TABLE pc_build_item (
    build_item_id INT IDENTITY(1,1) PRIMARY KEY,
    build_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (build_id) REFERENCES pc_build(build_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Reviews
CREATE TABLE reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Warranty Claim
CREATE TABLE warranty_claim (
    claim_id INT IDENTITY(1,1) PRIMARY KEY,
    order_item_id INT NOT NULL,
    reason NVARCHAR(MAX),
    status NVARCHAR(50),
    claim_date DATE,
    FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
);

-- Blogs
CREATE TABLE blogs (
    blog_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    title NVARCHAR(500) NOT NULL,
    content NVARCHAR(MAX),
    published_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

GO

-- ===============================
-- INSERT INITIAL DATA
-- ===============================

-- Insert Roles
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (role_id, name) VALUES
(1, 'ADMIN'),
(2, 'USER'),
(3, 'STAFF');
SET IDENTITY_INSERT roles OFF;

-- Insert Users (password: Admin@123 - BCrypt hashed)
SET IDENTITY_INSERT users ON;
INSERT INTO users (user_id, username, email, password_hash, phone_number, status, role_id) VALUES
(1, 'admin', 'admin@computershop.com', '$2a$12$hmVTITrNbLV9ktZSpk6r7O6Qx5HNogF6jNs6jiPW69b6So83gjUfy', '0901234567', 'ACTIVE', 1),
(2, 'user1', 'user1@example.com', '$2a$12$hmVTITrNbLV9ktZSpk6r7O6Qx5HNogF6jNs6jiPW69b6So83gjUfy', '0907654321', 'ACTIVE', 2),
(3, 'staff1', 'staff1@computershop.com', '$2a$12$hmVTITrNbLV9ktZSpk6r7O6Qx5HNogF6jNs6jiPW69b6So83gjUfy', '0909876543', 'ACTIVE', 3);
SET IDENTITY_INSERT users OFF;

-- Insert Categories
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (category_id, category_name, parent_category_id) VALUES
(1, 'CPU', NULL),
(2, 'GPU', NULL),
(3, 'Mainboard', NULL),
(4, 'RAM', NULL),
(5, 'SSD', NULL),
(6, 'HDD', NULL),
(7, 'Power Supply', NULL),
(8, 'Case', NULL),
(9, 'Cooling', NULL),
(10, 'Monitor', NULL),
(11, 'Keyboard', NULL),
(12, 'Mouse', NULL),
(13, 'Intel CPU', 1),
(14, 'AMD CPU', 1),
(15, 'NVIDIA GPU', 2),
(16, 'AMD GPU', 2);
SET IDENTITY_INSERT categories OFF;

-- Insert Brands
SET IDENTITY_INSERT brands ON;
INSERT INTO brands (brand_id, brand_name, website) VALUES
(1, 'Intel', 'https://www.intel.com'),
(2, 'AMD', 'https://www.amd.com'),
(3, 'NVIDIA', 'https://www.nvidia.com'),
(4, 'ASUS', 'https://www.asus.com'),
(5, 'MSI', 'https://www.msi.com'),
(6, 'Gigabyte', 'https://www.gigabyte.com'),
(7, 'Corsair', 'https://www.corsair.com'),
(8, 'Kingston', 'https://www.kingston.com'),
(9, 'Samsung', 'https://www.samsung.com'),
(10, 'Logitech', 'https://www.logitech.com');
SET IDENTITY_INSERT brands OFF;

-- Insert Attributes
SET IDENTITY_INSERT attributes ON;
INSERT INTO attributes (attribute_id, attribute_name, description) VALUES
(1, 'Core Count', 'Number of CPU cores'),
(2, 'Clock Speed', 'Processor speed in GHz'),
(3, 'Memory Size', 'RAM/Storage capacity'),
(4, 'Memory Type', 'DDR4, DDR5, etc.'),
(5, 'Socket Type', 'CPU socket compatibility'),
(6, 'TDP', 'Thermal Design Power in Watts'),
(7, 'Form Factor', 'Size specification'),
(8, 'Interface', 'Connection type'),
(9, 'Wattage', 'Power supply wattage'),
(10, 'Efficiency Rating', 'PSU efficiency rating');
SET IDENTITY_INSERT attributes OFF;

-- Insert Sample Products
SET IDENTITY_INSERT products ON;
INSERT INTO products (product_id, name, description, category_id, brand_id, price, stock_quantity) VALUES
(1, 'Intel Core i9-14900K', 'High-end desktop processor with 24 cores', 13, 1, 589.99, 25),
(2, 'AMD Ryzen 9 7950X', 'Flagship AMD processor with 16 cores', 14, 2, 699.99, 20),
(3, 'NVIDIA RTX 4090', 'Top-tier graphics card for gaming', 15, 3, 1599.99, 15),
(4, 'AMD Radeon RX 7900 XTX', 'High-performance AMD GPU', 16, 2, 999.99, 18),
(5, 'ASUS ROG Strix Z790-E', 'Premium Intel Z790 motherboard', 3, 4, 499.99, 30),
(6, 'Corsair Vengeance DDR5 32GB', '32GB DDR5 RAM kit 6000MHz', 4, 7, 159.99, 50),
(7, 'Samsung 990 PRO 2TB', 'NVMe SSD with exceptional speed', 5, 9, 199.99, 40),
(8, 'Corsair RM1000x', '1000W 80+ Gold PSU', 7, 7, 189.99, 35),
(9, 'NZXT H7 Flow', 'Mid-tower case with excellent airflow', 8, 4, 129.99, 45),
(10, 'Logitech G Pro X Superlight', 'Wireless gaming mouse', 12, 10, 159.99, 60);
SET IDENTITY_INSERT products OFF;

-- Insert Sample Promotions
SET IDENTITY_INSERT promotions ON;
INSERT INTO promotions (promotion_id, promo_code, discount_percent, start_date, end_date) VALUES
(1, 'SUMMER2026', 15, '2026-06-01', '2026-08-31'),
(2, 'NEWUSER10', 10, '2026-01-01', '2026-12-31'),
(3, 'BLACKFRIDAY', 25, '2026-11-25', '2026-11-30');
SET IDENTITY_INSERT promotions OFF;

-- Insert Sample Promotion Products
SET IDENTITY_INSERT promotion_product ON;
INSERT INTO promotion_product (promo_prod_id, promotion_id, product_id) VALUES
(1, 1, 1),
(2, 1, 3),
(3, 1, 7),
(4, 2, 6),
(5, 2, 10);
SET IDENTITY_INSERT promotion_product OFF;

GO

PRINT 'Database initialization completed successfully!';
PRINT 'Default users:';
PRINT '  - admin / Admin@123';
PRINT '  - user1 / Admin@123';
PRINT '  - staff1 / Admin@123';
GO
