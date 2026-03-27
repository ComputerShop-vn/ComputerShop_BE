-- ===============================
-- SBA301 Computer Shop - Database Initialization
-- SQL Server - matches Hibernate entity model (ddl-auto: validate)
-- ===============================
IF NOT EXISTS (
    SELECT name
    FROM sys.databases
    WHERE name = N'ComputerShopDB'
) BEGIN CREATE DATABASE ComputerShopDB;
END
GO
USE ComputerShopDB;
GO
-- ===============================
    -- DROP TABLES (reverse FK order)
    -- ===============================
    IF OBJECT_ID('warranty_claim', 'U') IS NOT NULL DROP TABLE warranty_claim;
IF OBJECT_ID('warranties', 'U') IS NOT NULL DROP TABLE warranties;
IF OBJECT_ID('reviews', 'U') IS NOT NULL DROP TABLE reviews;
IF OBJECT_ID('order_payment_schedule', 'U') IS NOT NULL DROP TABLE order_payment_schedule;
IF OBJECT_ID('order_items', 'U') IS NOT NULL DROP TABLE order_items;
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('cart_items', 'U') IS NOT NULL DROP TABLE cart_items;
IF OBJECT_ID('carts', 'U') IS NOT NULL DROP TABLE carts;
IF OBJECT_ID('pc_build_items', 'U') IS NOT NULL DROP TABLE pc_build_items;
IF OBJECT_ID('pc_builds', 'U') IS NOT NULL DROP TABLE pc_builds;
-- legacy names (drop if exist from old schema)
IF OBJECT_ID('pc_build_item', 'U') IS NOT NULL DROP TABLE pc_build_item;
IF OBJECT_ID('pc_build', 'U') IS NOT NULL DROP TABLE pc_build;
IF OBJECT_ID('promotion_product', 'U') IS NOT NULL DROP TABLE promotion_product;
IF OBJECT_ID('promotions', 'U') IS NOT NULL DROP TABLE promotions;
IF OBJECT_ID('product_images', 'U') IS NOT NULL DROP TABLE product_images;
IF OBJECT_ID('product_variant_attributes', 'U') IS NOT NULL DROP TABLE product_variant_attributes;
IF OBJECT_ID('product_items', 'U') IS NOT NULL DROP TABLE product_items;
IF OBJECT_ID('product_variants', 'U') IS NOT NULL DROP TABLE product_variants;
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('compatibility_rules', 'U') IS NOT NULL DROP TABLE compatibility_rules;
IF OBJECT_ID('attributes', 'U') IS NOT NULL DROP TABLE attributes;
IF OBJECT_ID('blogs', 'U') IS NOT NULL DROP TABLE blogs;
IF OBJECT_ID('brands', 'U') IS NOT NULL DROP TABLE brands;
IF OBJECT_ID('categories', 'U') IS NOT NULL DROP TABLE categories;
IF OBJECT_ID('installment_package', 'U') IS NOT NULL DROP TABLE installment_package;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('roles', 'U') IS NOT NULL DROP TABLE roles;
IF OBJECT_ID('invalidated_token', 'U') IS NOT NULL DROP TABLE invalidated_token;
GO
-- ===============================
    -- CREATE TABLES
    -- ===============================
    -- roles
    CREATE TABLE roles (
        role_id INT IDENTITY(1, 1) PRIMARY KEY,
        name NVARCHAR(50) NOT NULL UNIQUE
    );
-- installment_package
CREATE TABLE installment_package (
    package_id INT IDENTITY(1, 1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    duration_months INT NOT NULL,
    interest_rate FLOAT NOT NULL,
    min_order_amount FLOAT NOT NULL,
    down_payment_percentage FLOAT NOT NULL DEFAULT 20.0,
    is_active BIT DEFAULT 1,
    annual_penalty_rate FLOAT NOT NULL DEFAULT 0.365
);
-- users
CREATE TABLE users (
    user_id INT IDENTITY(1, 1) PRIMARY KEY,
    username NVARCHAR(100) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    phone_number NVARCHAR(20),
    created_at DATETIME2,
    status NVARCHAR(50),
    role_id INT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
-- invalidated_token
CREATE TABLE invalidated_token (
    id NVARCHAR(255) PRIMARY KEY,
    expiry_time DATETIME2 NOT NULL
);
-- categories  (self-referencing)
CREATE TABLE categories (
    category_id INT IDENTITY(1, 1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL,
    parent_category_id INT NULL,
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);
-- brands
CREATE TABLE brands (
    brand_id INT IDENTITY(1, 1) PRIMARY KEY,
    brand_name NVARCHAR(100) NOT NULL,
    logo_url NVARCHAR(500)
);
-- products  (NO price/stock_quantity -- those live on product_variants)
CREATE TABLE products (
    product_id INT IDENTITY(1, 1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    base_price FLOAT,
    category_id INT NOT NULL,
    brand_id INT NOT NULL,
    warranty_months INT NOT NULL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id)
);
-- product_variants  (SKU-level stock & price)
CREATE TABLE product_variants (
    variant_id INT IDENTITY(1, 1) PRIMARY KEY,
    product_id INT NOT NULL,
    sku NVARCHAR(100) NOT NULL UNIQUE,
    price FLOAT NOT NULL,
    stock_quantity INT DEFAULT 0,
    variant_name NVARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
-- attributes
CREATE TABLE attributes (
    attribute_id INT IDENTITY(1, 1) PRIMARY KEY,
    attribute_name NVARCHAR(100) NOT NULL UNIQUE
);
-- compatibility_rules  (drive getFilterHints + future validation)
CREATE TABLE compatibility_rules (
    rule_id INT IDENTITY(1, 1) PRIMARY KEY,
    component_type_1 NVARCHAR(50) NOT NULL,
    component_type_2 NVARCHAR(50) NULL,
    -- NULL for MIN_WATTAGE
    attribute_1 NVARCHAR(100) NOT NULL,
    attribute_2 NVARCHAR(100) NULL,
    -- NULL for MIN_WATTAGE
    rule_type NVARCHAR(20) NOT NULL,
    description NVARCHAR(500)
);
-- product_variant_attributes  (EAV for variant specs)
CREATE TABLE product_variant_attributes (
    variant_attr_id INT IDENTITY(1, 1) PRIMARY KEY,
    variant_id INT NOT NULL,
    attribute_id INT NOT NULL,
    value NVARCHAR(500) NOT NULL,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id)
);
-- product_items  (physical units / serial numbers — 1 unit = 1 OrderItem via OneToOne)
CREATE TABLE product_items (
    item_id INT IDENTITY(1, 1) PRIMARY KEY,
    variant_id INT NULL,
    serial_number NVARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);
-- product_images
CREATE TABLE product_images (
    image_id INT IDENTITY(1, 1) PRIMARY KEY,
    product_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    is_thumbnail BIT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
-- blogs
CREATE TABLE blogs (
    blog_id INT IDENTITY(1, 1) PRIMARY KEY,
    user_id INT NOT NULL,
    title NVARCHAR(500) NOT NULL,
    content NVARCHAR(MAX),
    published_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
-- promotions
CREATE TABLE promotions (
    promotion_id INT IDENTITY(1, 1) PRIMARY KEY,
    promo_code NVARCHAR(50) NOT NULL UNIQUE,
    discount_percent INT NOT NULL,
    start_date DATE,
    end_date DATE
);
-- promotion_product
CREATE TABLE promotion_product (
    promo_prod_id INT IDENTITY(1, 1) PRIMARY KEY,
    promotion_id INT NOT NULL,
    product_id INT NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES promotions(promotion_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
-- carts  (1-to-1 with user)
CREATE TABLE carts (
    cart_id INT IDENTITY(1, 1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
-- cart_items  (reference variant, not raw product)
CREATE TABLE cart_items (
    cart_item_id INT IDENTITY(1, 1) PRIMARY KEY,
    cart_id INT NOT NULL,
    variant_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);
-- orders
CREATE TABLE orders (
    order_id INT IDENTITY(1, 1) PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount FLOAT,
    status NVARCHAR(50),
    payment_method NVARCHAR(20),
    payment_mode NVARCHAR(20),
    installment_package_id INT NULL,
    order_date DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (installment_package_id) REFERENCES installment_package(package_id)
);
-- order_items  (item_id is UNIQUE — OneToOne with product_items, 1 physical unit sold once)
CREATE TABLE order_items (
    order_item_id INT IDENTITY(1, 1) PRIMARY KEY,
    order_id INT NOT NULL,
    item_id INT NOT NULL UNIQUE,
    quantity INT NOT NULL,
    unit_price FLOAT NOT NULL,
    recipient_name NVARCHAR(200),
    recipient_phone NVARCHAR(20),
    shipping_address NVARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (item_id) REFERENCES product_items(item_id)
);
-- order_payment_schedule
CREATE TABLE order_payment_schedule (
    payment_schedule_id INT IDENTITY(1, 1) PRIMARY KEY,
    order_id INT NOT NULL,
    installment_no INT NOT NULL,
    amount FLOAT NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    vnp_transaction_no NVARCHAR(255),
    status NVARCHAR(20) NOT NULL,
    penalty_amount FLOAT DEFAULT 0.0, -- The total accumulated penalty fee
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
-- pc_builds  (replaces old pc_build — note updated_at added)
CREATE TABLE pc_builds (
    build_id INT IDENTITY(1, 1) PRIMARY KEY,
    user_id INT NOT NULL,
    build_name NVARCHAR(255),
    total_price FLOAT,
    created_at DATETIME2,
    updated_at DATETIME2,
    status NVARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
-- pc_build_items  (replaces old pc_build_item — variant-based, has component_type and price)
CREATE TABLE pc_build_items (
    build_item_id INT IDENTITY(1, 1) PRIMARY KEY,
    build_id INT NOT NULL,
    component_type NVARCHAR(50) NOT NULL,
    variant_id INT NOT NULL,
    quantity INT NOT NULL,
    price FLOAT NOT NULL,
    FOREIGN KEY (build_id) REFERENCES pc_builds(build_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);
-- reviews
CREATE TABLE reviews (
    review_id INT IDENTITY(1, 1) PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT NOT NULL CHECK (
        rating >= 1
        AND rating <= 5
    ),
    comment NVARCHAR(MAX),
    created_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);
-- warranties
CREATE TABLE warranties (
    id INT IDENTITY(1, 1) PRIMARY KEY,
    order_item_id INT NOT NULL,
    serial_number NVARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description NVARCHAR(MAX),
    status NVARCHAR(50) NOT NULL,
    type NVARCHAR(50) NOT NULL,
    FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
);
-- warranty_claim
CREATE TABLE warranty_claim (
    claim_id INT IDENTITY(1, 1) PRIMARY KEY,
    warranty_id INT NOT NULL,
    claim_date DATE,
    customer_note NVARCHAR(MAX),
    technician_note NVARCHAR(MAX),
    status NVARCHAR(50) NOT NULL,
    solution_type NVARCHAR(50),
    return_date DATE,
    FOREIGN KEY (warranty_id) REFERENCES warranties(id)
);
GO
-- ===============================
-- SEED DATA
-- ===============================

-- ── Roles ──────────────────────────────────────────────────────────────────
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (role_id, name) VALUES (1, 'ADMIN'), (2, 'STAFF'), (3, 'MEMBER');
SET IDENTITY_INSERT roles OFF;

-- ── Installment Packages ────────────────────────────────────────────────────
SET IDENTITY_INSERT installment_package ON;

INSERT INTO installment_package (
        package_id,
        name,
        duration_months,
        interest_rate,
        min_order_amount,
        down_payment_percentage,
        is_active,
        annual_penalty_rate
    )
VALUES (
        1,
        N'Trả góp 3 tháng - Lãi suất 0% (Trả trước 0%)',
        3,
        0.0,
        500000.00,
        0.0,
        1,
        0.365
    ),
    (
        2,
        N'Trả góp 6 tháng - Lãi suất 1% (Trả trước 10%)',
        6,
        1.0,
        1000000.00,
        10.0,
        1,
        0.365
    ),
    (
        3,
        N'Trả góp 12 tháng - Lãi suất 1.5% (Trả trước 20%)',
        12,
        1.5,
        3000000.00,
        20.0,
        1,
        0.365
    ),
    (
        4,
        N'Trả góp 9 tháng - Không hoạt động',
        9,
        1.5,
        2000000.00,
        15.0,
        0,
        0.365
    );
SET IDENTITY_INSERT installment_package OFF;

-- ── Users  (password = Admin@123 for all) ───────────────────────────────────
SET IDENTITY_INSERT users ON;
INSERT INTO users (user_id, username, email, password_hash, phone_number, created_at, status, role_id)
VALUES
(1, 'admin',   'admin@computershop.com',  '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0901234567', GETDATE(), 'ACTIVE', 1),
(2, 'staff1',  'staff1@computershop.com', '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0902345678', GETDATE(), 'ACTIVE', 2),
(3, 'staff2',  'staff2@computershop.com', '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0902345679', GETDATE(), 'ACTIVE', 2),
(4, 'member1', 'member1@example.com',     '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0903456789', GETDATE(), 'ACTIVE', 3),
(5, 'member2', 'member2@example.com',     '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0904567890', GETDATE(), 'ACTIVE', 3),
(6, 'member3', 'member3@example.com',     '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K', '0905678901', GETDATE(), 'ACTIVE', 3);
SET IDENTITY_INSERT users OFF;

-- ── Categories ──────────────────────────────────────────────────────────────
-- Cấu trúc: parent NULL = danh mục gốc, parent_id = danh mục con
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (category_id, category_name, parent_category_id)
VALUES
-- ── Laptop (parent: NULL) ──
(1,  N'Laptop',                 NULL),
(2,  N'Laptop Gaming',          1),
(3,  N'Laptop Văn Phòng',       1),
(4,  N'MacBook',                1),
(5,  N'Laptop Đồ Họa',          1),

-- ── Linh Kiện (parent: NULL) ──
(6,  N'Linh Kiện',              NULL),
(7,  N'CPU - Vi xử lý',         6),
(8,  N'VGA - Card màn hình',    6),
(9,  N'Mainboard - Bo mạch chủ',6),
(10, N'RAM - Bộ nhớ trong',     6),
(11, N'SSD - Ổ cứng',           6),
(26, N'PSU - Nguồn máy tính',   6),
(27, N'Thùng máy - Case',       6),
(28, N'Tản nhiệt',              6),

-- ── Build PC (parent: NULL) ──
(12, N'Build PC',               NULL),
(13, N'PC Gaming',              12),
(14, N'PC Văn Phòng',           12),
(15, N'Workstation',            12),
(16, N'PC Custom Water Cooling',12),

-- ── Màn hình (parent: NULL) ──
(17, N'Màn hình',               NULL),
(18, N'Màn hình Gaming',        17),
(19, N'Màn hình Đồ Họa',        17),
(20, N'Màn hình Cong',          17),

-- ── Phụ Kiện (parent: NULL) ──
(21, N'Phụ Kiện',               NULL),
(22, N'Bàn phím cơ',            21),
(23, N'Chuột Gaming',           21),
(24, N'Tai Nghe',               21),
(25, N'Ghế Gaming',             21);
SET IDENTITY_INSERT categories OFF;

-- ── Brands ──────────────────────────────────────────────────────────────────
SET IDENTITY_INSERT brands ON;
INSERT INTO brands (brand_id, brand_name)
VALUES
(1,  'Intel'),
(2,  'AMD'),
(3,  'NVIDIA'),
(4,  'ASUS'),
(5,  'MSI'),
(6,  'Corsair'),
(7,  'Kingston'),
(8,  'Samsung'),
(9,  'Logitech'),
(10, 'NZXT'),
(11, 'Noctua'),
(12, 'Fractal Design'),
(13, 'Apple'),
(14, 'LG'),
(15, 'Dell'),
(16, 'Gigabyte'),
(17, 'G.Skill'),
(18, 'Seagate'),
(19, 'Keychron'),
(20, 'SteelSeries'),
(21, 'HyperX'),
(22, 'Secretlab'),
(23, 'Acer'),
(24, 'Lenovo');
SET IDENTITY_INSERT brands OFF;

-- ── Attributes ──────────────────────────────────────────────────────────────
SET IDENTITY_INSERT attributes ON;
INSERT INTO attributes (attribute_id, attribute_name)
VALUES
-- CPU / GPU general
(1,  'Core Count'),
(2,  'Thread Count'),
(3,  'Base Clock'),
(4,  'Boost Clock'),
(5,  'TDP'),
(6,  'Socket'),
-- Memory
(7,  'Memory Size'),
(8,  'Memory Type'),
(9,  'Memory Bus'),
(10, 'Memory Speed'),
-- Form factor / physical fit
(11, 'Form Factor'),
-- Storage
(12, 'Interface'),
(13, 'Read Speed'),
(14, 'Write Speed'),
-- PSU
(15, 'Wattage'),
(16, 'Efficiency'),
-- Mainboard capacity
(17, 'Max RAM Speed'),
(18, 'RAM Slots'),
-- Physical dimensions (for MUST_FIT rules)
(19, 'Max GPU Length'),   -- on CASE: max GPU length it can fit (mm)
(20, 'Max Cooler Height'),-- on CASE: max CPU cooler height it can fit (mm)
(21, 'GPU Length'),       -- on GPU variant: physical length (mm)
(22, 'Cooler Height'),    -- on COOLING variant: physical height (mm)
-- Display / Laptop
(23, 'Display Size'),     -- inch
(24, 'Refresh Rate'),     -- Hz
(25, 'Resolution'),
(26, 'Response Time'),    -- ms
(27, 'Panel Type');       -- IPS, VA, OLED...
SET IDENTITY_INSERT attributes OFF;

-- ── Products ────────────────────────────────────────────────────────────────
-- category mapping:
--   7  = CPU           8  = VGA          9  = Mainboard
--   10 = RAM           11 = SSD          26 = PSU
--   27 = Case          28 = Tản nhiệt    23 = Chuột Gaming
--   2  = Laptop Gaming 3  = Laptop VP    4  = MacBook
--   18 = Màn hình Gaming  19 = Màn hình Đồ Họa
--   22 = Bàn phím cơ   24 = Tai Nghe
SET IDENTITY_INSERT products ON;
INSERT INTO products (product_id, name, description, base_price, category_id, brand_id, warranty_months)
VALUES
-- ── CPU ──
(1,  N'Intel Core i9-14900K',
     N'CPU cao cấp Intel thế hệ 14, 24 nhân (8P+16E), socket LGA1700, hỗ trợ DDR5.',
     13990000, 7, 1, 36),
(2,  N'AMD Ryzen 9 7950X',
     N'CPU flagship Ryzen, 16 nhân/32 luồng, socket AM5, hỗ trợ DDR5.',
     17490000, 7, 2, 36),
(16, N'AMD Ryzen 5 5600X',
     N'CPU tầm trung AM4, 6 nhân/12 luồng, 65W TDP, hỗ trợ DDR4.',
     3790000,  7, 2, 36),
(29, N'Intel Core i5-13600K',
     N'CPU phổ thông Intel thế hệ 13, 14 nhân (6P+8E), socket LGA1700, DDR5/DDR4.',
     7490000,  7, 1, 36),

-- ── VGA ──
(3,  N'NVIDIA RTX 4090',
     N'GPU flagship Ada Lovelace, 24 GB GDDR6X, hiệu năng đỉnh cao cho gaming 4K và AI.',
     39990000, 8, 3, 36),
(4,  N'AMD Radeon RX 7900 XTX',
     N'GPU high-end RDNA3, 24 GB GDDR6, hiệu năng gaming 4K vượt trội.',
     24990000, 8, 2, 36),
(30, N'NVIDIA RTX 4070 Super',
     N'GPU tầm cao Ada Lovelace, 12 GB GDDR6X, lý tưởng cho gaming 1440p.',
     17990000, 8, 3, 36),
(31, N'NVIDIA RTX 4060',
     N'GPU tầm trung Ada Lovelace, 8 GB GDDR6, gaming 1080p mượt mà.',
     8990000,  8, 3, 36),

-- ── Mainboard ──
(5,  N'ASUS ROG Strix Z790-E Gaming',
     N'Mainboard ATX cao cấp socket LGA1700, DDR5, 4 khe DIMM.',
     12490000, 9, 4, 36),
(6,  N'MSI MAG X670E TOMAHAWK WIFI',
     N'Mainboard ATX socket AM5, DDR5-6000, 4 khe DIMM, PCIe 5.0.',
     8790000,  9, 5, 36),
(7,  N'ASUS TUF Gaming B450M-PLUS',
     N'Mainboard mATX socket AM4, DDR4-4400, 2 khe DIMM.',
     2290000,  9, 4, 36),
(32, N'MSI PRO B760M-A WIFI',
     N'Mainboard mATX socket LGA1700, DDR5, 2 khe DIMM, WiFi 6.',
     3490000,  9, 5, 36),

-- ── RAM ──
(8,  N'Corsair Vengeance DDR5',
     N'Bộ nhớ DDR5 tốc độ cao, XMP 3.0, nhiều dung lượng.',
     2290000,  10, 6, 24),
(9,  N'Kingston Fury Beast DDR4-3200',
     N'Bộ nhớ DDR4 ổn định cho nền tảng AM4. Dùng test DDR4.',
     990000,   10, 7, 24),
(33, N'G.Skill Trident Z5 DDR5-6400',
     N'RAM DDR5 cao cấp RGB, tốc độ 6400MHz, XMP 3.0.',
     3990000,  10, 17, 24),

-- ── SSD ──
(10, N'Samsung 990 PRO NVMe SSD',
     N'SSD PCIe 4.0 NVMe, đọc lên đến 7450 MB/s.',
     2990000,  11, 8, 60),
(34, N'Seagate FireCuda 530 NVMe SSD',
     N'SSD PCIe 4.0 NVMe cao cấp, đọc 7300 MB/s.',
     2490000,  11, 18, 60),

-- ── PSU ──
(11, N'Corsair RM1000x',
     N'Nguồn 1000W 80+ Gold fully-modular ATX.',
     4790000,  26, 6, 60),
(17, N'Corsair CV650',
     N'Nguồn 650W 80+ Bronze. Test MIN_WATTAGE FAIL khi dùng i9+RTX4090.',
     1490000,  26, 6, 36),
(18, N'Corsair RM750x',
     N'Nguồn 750W 80+ Gold. Test MIN_WATTAGE PASS boundary.',
     2790000,  26, 6, 60),
(35, N'Corsair RM850x',
     N'Nguồn 850W 80+ Gold fully-modular, phù hợp build tầm trung cao cấp.',
     3490000,  26, 6, 60),

-- ── Case ──
(12, N'NZXT H7 Flow',
     N'Thùng máy ATX mid-tower lưới tản nhiệt. Max GPU 400mm, Max Cooler 185mm.',
     2790000,  27, 10, 24),
(13, N'Fractal Design Pop Mini Air',
     N'Thùng máy mATX mini-tower. Max GPU 300mm, Max Cooler 160mm.',
     1990000,  27, 12, 24),
(36, N'Lian Li PC-O11 Dynamic EVO',
     N'Thùng máy ATX full-tower dual-chamber. Max GPU 420mm, Max Cooler 167mm.',
     3990000,  27, 16, 24),

-- ── Tản nhiệt ──
(14, N'Noctua NH-D15',
     N'Tản nhiệt khí dual-tower, cao 165mm, tương thích AM4/AM5/LGA1700.',
     2490000,  28, 11, 24),
(37, N'Corsair iCUE H150i Elite LCD',
     N'Tản nhiệt nước AIO 360mm, màn LCD hiển thị nhiệt độ.',
     5490000,  28, 6, 24),

-- ── Chuột Gaming ──
(15, N'Logitech G Pro X Superlight 2',
     N'Chuột gaming không dây siêu nhẹ 60g, sensor HERO 2.',
     3990000,  23, 9, 24),

-- ── Laptop ──
(19, N'Apple MacBook Pro 14 M3 Pro',
     N'Laptop cao cấp chip M3 Pro, màn hình Liquid Retina XDR 14.2", pin 18 giờ.',
     49990000, 4, 13, 12),
(20, N'ASUS ROG Strix G16 2024',
     N'Laptop gaming mạnh mẽ, chip Intel i9-14900HX, màn hình 16" 240Hz.',
     42990000, 2, 4, 12),
(38, N'Acer Nitro V 15',
     N'Laptop gaming phổ thông, chip Ryzen 5 7535HS, RTX 3050, màn hình 144Hz.',
     18990000, 2, 23, 12),
(21, N'Lenovo ThinkPad X1 Carbon Gen 12',
     N'Laptop văn phòng siêu mỏng, chip Intel Ultra 7, màn hình 14" IPS, pin 15 giờ.',
     32990000, 3, 24, 12),
(39, N'Dell XPS 15 9530',
     N'Laptop đồ họa cao cấp, chip i7-13700H, RTX 4060, màn hình OLED 3.5K.',
     45990000, 5, 15, 12),

-- ── Màn hình ──
(22, N'LG UltraGear 27GP850-B',
     N'Màn hình gaming 27" QHD 180Hz IPS, 1ms GtG, NVIDIA G-Sync Compatible.',
     8490000,  18, 14, 24),
(40, N'ASUS ROG Swift PG279QM',
     N'Màn hình gaming 27" QHD 240Hz IPS, G-Sync Ultimate.',
     14990000, 18, 4, 24),
(23, N'Dell UltraSharp U2723D',
     N'Màn hình đồ họa 27" 4K IPS Black, DCI-P3 98%, USB-C 90W.',
     14990000, 19, 15, 24),
(41, N'LG 34WP85C-B UltraWide',
     N'Màn hình cong 34" UltraWide QHD, IPS, 160Hz, USB-C 96W.',
     12990000, 20, 14, 24),

-- ── Bàn phím cơ ──
(24, N'Keychron K2 V2',
     N'Bàn phím cơ TKL compact 75%, Bluetooth/USB-C, switch Gateron G Pro.',
     2190000,  22, 19, 12),
(42, N'Corsair K70 RGB MK.2',
     N'Bàn phím cơ full-size, Cherry MX switch, RGB per-key, aluminum frame.',
     3490000,  22, 6, 24),

-- ── Tai nghe ──
(25, N'SteelSeries Arctis Nova Pro Wireless',
     N'Tai nghe gaming không dây cao cấp, driver 40mm, ANC, pin dự phòng.',
     8990000,  24, 20, 12),
(43, N'HyperX Cloud Alpha Wireless',
     N'Tai nghe gaming không dây, driver 50mm, pin 300 giờ.',
     3490000,  24, 21, 12);
SET IDENTITY_INSERT products OFF;

-- ── Product Variants ────────────────────────────────────────────────────────
SET IDENTITY_INSERT product_variants ON;
INSERT INTO product_variants (variant_id, product_id, sku, price, stock_quantity, variant_name)
VALUES
-- i9-14900K (product 1)
(1,  1,  'CPU-I9-14900K-BOX',     14790000, 20, N'i9-14900K Box (kèm tản)'),
(2,  1,  'CPU-I9-14900K-TRAY',    13990000, 15, N'i9-14900K Tray (không tản)'),
-- Ryzen 9 7950X (product 2)
(3,  2,  'CPU-R9-7950X-BOX',      17490000, 18, N'Ryzen 9 7950X Box'),
-- Ryzen 5 5600X (product 16)
(25, 16, 'CPU-R5-5600X-BOX',       3790000, 25, N'Ryzen 5 5600X Box (kèm tản)'),
-- i5-13600K (product 29)
(44, 29, 'CPU-I5-13600K-BOX',      7490000, 30, N'i5-13600K Box'),
(45, 29, 'CPU-I5-13600K-TRAY',     6990000, 20, N'i5-13600K Tray'),

-- RTX 4090 (product 3)
(4,  3,  'GPU-RTX4090-ASUS',      44990000,  8, N'ASUS ROG STRIX RTX 4090 OC 24GB — 357mm'),
(5,  3,  'GPU-RTX4090-MSI',       42490000,  6, N'MSI GAMING TRIO RTX 4090 24GB — 340mm'),
(6,  3,  'GPU-RTX4090-FE',        39990000,  5, N'NVIDIA Founders Edition RTX 4090 — 336mm'),
-- RX 7900 XTX (product 4)
(7,  4,  'GPU-RX7900XTX-REF',     24990000, 12, N'AMD Reference RX 7900 XTX 24GB — 287mm'),
-- RTX 4070 Super (product 30)
(46, 30, 'GPU-RTX4070S-ASUS',     18990000, 20, N'ASUS DUAL RTX 4070 Super OC 12GB — 267mm'),
(47, 30, 'GPU-RTX4070S-MSI',      17990000, 18, N'MSI VENTUS RTX 4070 Super 12GB — 285mm'),
-- RTX 4060 (product 31)
(48, 31, 'GPU-RTX4060-ASUS',       9490000, 35, N'ASUS DUAL RTX 4060 OC 8GB — 200mm'),
(49, 31, 'GPU-RTX4060-MSI',        8990000, 30, N'MSI VENTUS RTX 4060 8GB — 214mm'),

-- Z790-E mainboard (product 5)
(8,  5,  'MB-ROGZ790E-ATX',       12490000, 25, N'ROG Strix Z790-E ATX LGA1700 DDR5'),
-- MSI X670E (product 6)
(9,  6,  'MB-MAGX670E-ATX',        8790000, 20, N'MSI MAG X670E TOMAHAWK AM5 DDR5'),
-- ASUS B450M (product 7)
(10, 7,  'MB-TUFB450M-MATX',       2290000, 15, N'ASUS TUF B450M-PLUS mATX AM4 DDR4'),
-- MSI PRO B760M (product 32)
(50, 32, 'MB-PROB760M-MATX',       3490000, 20, N'MSI PRO B760M-A WIFI mATX LGA1700 DDR5'),

-- Corsair DDR5 (product 8)
(11, 8,  'RAM-DDR5-16G-6000',      2290000, 50, N'Corsair Vengeance DDR5 16GB 6000MHz'),
(12, 8,  'RAM-DDR5-32G-6000',      3990000, 40, N'Corsair Vengeance DDR5 32GB 6000MHz'),
(13, 8,  'RAM-DDR5-64G-6000',      7490000, 20, N'Corsair Vengeance DDR5 64GB 6000MHz'),
-- Kingston DDR4 (product 9)
(14, 9,  'RAM-DDR4-16G-3200',       990000, 60, N'Kingston Fury Beast DDR4 16GB 3200MHz'),
(15, 9,  'RAM-DDR4-32G-3200',      1790000, 40, N'Kingston Fury Beast DDR4 32GB 3200MHz'),
-- G.Skill DDR5 (product 33)
(51, 33, 'RAM-DDR5-32G-6400-GS',   4990000, 30, N'G.Skill Trident Z5 DDR5 32GB 6400MHz RGB'),
(52, 33, 'RAM-DDR5-64G-6400-GS',   9490000, 15, N'G.Skill Trident Z5 DDR5 64GB 6400MHz RGB'),

-- Samsung 990 PRO (product 10)
(16, 10, 'SSD-990PRO-1TB',         2990000, 35, N'Samsung 990 PRO 1TB NVMe PCIe 4.0'),
(17, 10, 'SSD-990PRO-2TB',         4990000, 30, N'Samsung 990 PRO 2TB NVMe PCIe 4.0'),
-- Seagate FireCuda 530 (product 34)
(53, 34, 'SSD-FC530-1TB',          2490000, 25, N'Seagate FireCuda 530 1TB NVMe PCIe 4.0'),
(54, 34, 'SSD-FC530-2TB',          4490000, 20, N'Seagate FireCuda 530 2TB NVMe PCIe 4.0'),

-- Corsair RM1000x PSU (product 11)
(18, 11, 'PSU-RM1000X-2024',       4790000, 30, N'Corsair RM1000x 1000W 80+ Gold'),
-- Corsair CV650 (product 17)
(26, 17, 'PSU-CV650-650W',         1490000, 30, N'Corsair CV650 650W 80+ Bronze'),
-- Corsair RM750x (product 18)
(27, 18, 'PSU-RM750X-750W',        2790000, 30, N'Corsair RM750x 750W 80+ Gold'),
-- Corsair RM850x (product 35)
(55, 35, 'PSU-RM850X-850W',        3490000, 25, N'Corsair RM850x 850W 80+ Gold'),

-- NZXT H7 Flow (product 12)
(19, 12, 'CASE-H7FLOW-BLACK',      2790000, 40, N'NZXT H7 Flow ATX Đen'),
(20, 12, 'CASE-H7FLOW-WHITE',      2990000, 25, N'NZXT H7 Flow ATX Trắng'),
-- Fractal Pop Mini (product 13)
(21, 13, 'CASE-POPMINI-BLACK',     1990000, 30, N'Fractal Pop Mini Air mATX Đen'),
-- Lian Li O11 (product 36)
(56, 36, 'CASE-O11EVO-BLACK',      3990000, 20, N'Lian Li O11 Dynamic EVO ATX Đen'),
(57, 36, 'CASE-O11EVO-WHITE',      3990000, 15, N'Lian Li O11 Dynamic EVO ATX Trắng'),

-- Noctua NH-D15 (product 14)
(22, 14, 'COOL-NHD15',             2490000, 25, N'Noctua NH-D15 Dual Tower — 165mm'),
-- Corsair H150i (product 37)
(58, 37, 'COOL-H150I-LCD-BLACK',   5490000, 15, N'Corsair iCUE H150i Elite LCD 360mm Đen'),
(59, 37, 'COOL-H150I-LCD-WHITE',   5490000, 10, N'Corsair iCUE H150i Elite LCD 360mm Trắng'),

-- G Pro X Superlight 2 (product 15)
(23, 15, 'MOUSE-GPXSL2-BLACK',     3990000, 50, N'G Pro X Superlight 2 Đen'),
(24, 15, 'MOUSE-GPXSL2-WHITE',     3990000, 45, N'G Pro X Superlight 2 Trắng'),

-- MacBook Pro 14 M3 Pro (product 19)
(28, 19, 'LAPTOP-MBP14-M3PRO-18-512', 49990000, 15, N'MacBook Pro 14 M3 Pro 18GB/512GB Space Black'),
(29, 19, 'LAPTOP-MBP14-M3PRO-36-1TB', 64990000, 10, N'MacBook Pro 14 M3 Pro 36GB/1TB Space Black'),

-- ASUS ROG Strix G16 (product 20)
(30, 20, 'LAPTOP-ROG-G16-4070',    42990000, 12, N'ROG Strix G16 i9-14900HX / RTX 4070 / 16GB DDR5 / 1TB'),
(31, 20, 'LAPTOP-ROG-G16-4060',    34990000, 15, N'ROG Strix G16 i7-14700HX / RTX 4060 / 16GB DDR5 / 512GB'),

-- Acer Nitro V 15 (product 38)
(60, 38, 'LAPTOP-NITROV15-3050',   18990000, 20, N'Acer Nitro V 15 Ryzen 5 / RTX 3050 / 16GB / 512GB'),

-- Lenovo ThinkPad X1 Carbon (product 21)
(32, 21, 'LAPTOP-X1C-GEN12-16-512', 32990000, 20, N'ThinkPad X1 Carbon Gen 12 Ultra 7 / 16GB / 512GB'),
(33, 21, 'LAPTOP-X1C-GEN12-32-1TB', 42990000, 10, N'ThinkPad X1 Carbon Gen 12 Ultra 7 / 32GB / 1TB'),

-- Dell XPS 15 (product 39)
(61, 39, 'LAPTOP-XPS15-4060-OLED', 45990000, 8,  N'Dell XPS 15 9530 i7-13700H / RTX 4060 / 32GB / 1TB OLED'),

-- LG UltraGear 27GP850-B (product 22)
(34, 22, 'MON-27GP850-B',           8490000, 30, N'LG UltraGear 27GP850-B 27" QHD 180Hz IPS'),

-- ASUS ROG Swift PG279QM (product 40)
(62, 40, 'MON-PG279QM',            14990000, 15, N'ASUS ROG Swift PG279QM 27" QHD 240Hz IPS'),

-- Dell UltraSharp U2723D (product 23)
(35, 23, 'MON-U2723D',             14990000, 20, N'Dell UltraSharp U2723D 27" 4K IPS Black'),

-- LG 34WP85C-B (product 41)
(63, 41, 'MON-34WP85C-B',          12990000, 12, N'LG 34WP85C-B 34" UltraWide QHD Cong 160Hz'),

-- Keychron K2 V2 (product 24)
(36, 24, 'KB-K2V2-BROWN',           2190000, 50, N'Keychron K2 V2 Switch Gateron Brown (Tactile)'),
(37, 24, 'KB-K2V2-RED',             2190000, 50, N'Keychron K2 V2 Switch Gateron Red (Linear)'),

-- Corsair K70 (product 42)
(64, 42, 'KB-K70RGB-RED',           3490000, 30, N'Corsair K70 RGB MK.2 Cherry MX Red'),
(65, 42, 'KB-K70RGB-BROWN',         3490000, 25, N'Corsair K70 RGB MK.2 Cherry MX Brown'),

-- SteelSeries Arctis Nova Pro Wireless (product 25)
(38, 25, 'HS-NOVAPRO-WIRELESS',     8990000, 25, N'Arctis Nova Pro Wireless'),

-- HyperX Cloud Alpha Wireless (product 43)
(66, 43, 'HS-CLOUDALPHA-WL',        3490000, 35, N'HyperX Cloud Alpha Wireless');
SET IDENTITY_INSERT product_variants OFF;

-- ── Attributes legend:
-- 1=CoreCount  2=ThreadCount  3=BaseClock  4=BoostClock  5=TDP  6=Socket
-- 7=MemorySize 8=MemoryType   9=MemoryBus  10=MemorySpeed
-- 11=FormFactor 12=Interface  13=ReadSpeed 14=WriteSpeed
-- 15=Wattage   16=Efficiency  17=MaxRAMSpeed 18=RAMSlots
-- 19=MaxGPULength 20=MaxCoolerHeight 21=GPULength 22=CoolerHeight
-- 23=DisplaySize  24=RefreshRate  25=Resolution  26=ResponseTime 27=PanelType

-- ── Product Variant Attributes ───────────────────────────────────────────────
SET IDENTITY_INSERT product_variant_attributes ON;
INSERT INTO product_variant_attributes (variant_attr_id, variant_id, attribute_id, value)
VALUES
-- ── i9-14900K Box (variant 1) ──
(1,  1, 1, '24 (8P+16E)'), (2,  1, 2, '32'), (3,  1, 3, '3.2GHz'),
(4,  1, 4, '6.0GHz'),      (5,  1, 5, '125W'),(6,  1, 6, 'LGA1700'),
(7,  1, 8, 'DDR5'),
-- ── i9-14900K Tray (variant 2) ──
(8,  2, 1, '24 (8P+16E)'), (9,  2, 2, '32'), (10, 2, 3, '3.2GHz'),
(11, 2, 4, '6.0GHz'),      (12, 2, 5, '125W'),(13, 2, 6, 'LGA1700'),
(14, 2, 8, 'DDR5'),
-- ── Ryzen 9 7950X (variant 3) ──
(15, 3, 1, '16'), (16, 3, 2, '32'), (17, 3, 3, '4.5GHz'),
(18, 3, 4, '5.7GHz'), (19, 3, 5, '170W'), (20, 3, 6, 'AM5'),
(21, 3, 8, 'DDR5'),
-- ── Ryzen 5 5600X (variant 25) ──
(92, 25, 1, '6'), (93, 25, 2, '12'), (94, 25, 3, '3.7GHz'),
(95, 25, 4, '4.6GHz'), (96, 25, 5, '65W'), (97, 25, 6, 'AM4'),
(98, 25, 8, 'DDR4'),
-- ── i5-13600K Box (variant 44) ──
(200, 44, 1, '14 (6P+8E)'), (201, 44, 2, '20'), (202, 44, 3, '3.5GHz'),
(203, 44, 4, '5.1GHz'), (204, 44, 5, '125W'), (205, 44, 6, 'LGA1700'),
(206, 44, 8, 'DDR5'),
-- ── i5-13600K Tray (variant 45) ──
(207, 45, 1, '14 (6P+8E)'), (208, 45, 2, '20'), (209, 45, 3, '3.5GHz'),
(210, 45, 4, '5.1GHz'), (211, 45, 5, '125W'), (212, 45, 6, 'LGA1700'),
(213, 45, 8, 'DDR5'),

-- ── RTX 4090 ASUS (variant 4) ──
(22, 4, 7, '24GB'), (23, 4, 8, 'GDDR6X'), (24, 4, 9, '384-bit'),
(25, 4, 5, '450W'), (26, 4, 21, '357mm'),
-- ── RTX 4090 MSI (variant 5) ──
(27, 5, 7, '24GB'), (28, 5, 8, 'GDDR6X'), (29, 5, 9, '384-bit'),
(30, 5, 5, '450W'), (31, 5, 21, '340mm'),
-- ── RTX 4090 FE (variant 6) ──
(32, 6, 7, '24GB'), (33, 6, 8, 'GDDR6X'), (34, 6, 9, '384-bit'),
(35, 6, 5, '450W'), (36, 6, 21, '336mm'),
-- ── RX 7900 XTX Ref (variant 7) ──
(37, 7, 7, '24GB'), (38, 7, 8, 'GDDR6'), (39, 7, 9, '384-bit'),
(40, 7, 5, '355W'), (41, 7, 21, '287mm'),
-- ── RTX 4070 Super ASUS (variant 46) ──
(214, 46, 7, '12GB'), (215, 46, 8, 'GDDR6X'), (216, 46, 9, '192-bit'),
(217, 46, 5, '220W'), (218, 46, 21, '267mm'),
-- ── RTX 4070 Super MSI (variant 47) ──
(219, 47, 7, '12GB'), (220, 47, 8, 'GDDR6X'), (221, 47, 9, '192-bit'),
(222, 47, 5, '220W'), (223, 47, 21, '285mm'),
-- ── RTX 4060 ASUS (variant 48) ──
(224, 48, 7, '8GB'), (225, 48, 8, 'GDDR6'), (226, 48, 9, '128-bit'),
(227, 48, 5, '115W'), (228, 48, 21, '200mm'),
-- ── RTX 4060 MSI (variant 49) ──
(229, 49, 7, '8GB'), (230, 49, 8, 'GDDR6'), (231, 49, 9, '128-bit'),
(232, 49, 5, '115W'), (233, 49, 21, '214mm'),

-- ── Z790-E ATX (variant 8) ──
(42, 8, 6, 'LGA1700'), (43, 8, 8, 'DDR5'), (44, 8, 11, 'ATX'),
(45, 8, 17, '7200'), (46, 8, 18, '4'),
-- ── MSI X670E ATX (variant 9) ──
(47, 9, 6, 'AM5'), (48, 9, 8, 'DDR5'), (49, 9, 11, 'ATX'),
(50, 9, 17, '6000'), (51, 9, 18, '4'),
-- ── ASUS B450M mATX (variant 10) ──
(52, 10, 6, 'AM4'), (53, 10, 8, 'DDR4'), (54, 10, 11, 'mATX'),
(55, 10, 17, '4400'), (56, 10, 18, '2'),
-- ── MSI PRO B760M (variant 50) ──
(234, 50, 6, 'LGA1700'), (235, 50, 8, 'DDR5'), (236, 50, 11, 'mATX'),
(237, 50, 17, '5600'), (238, 50, 18, '2'),

-- ── Corsair DDR5 16GB (variant 11) ──
(57, 11, 7, '16GB'), (58, 11, 8, 'DDR5'), (59, 11, 10, '6000MHz'),
-- ── Corsair DDR5 32GB (variant 12) ──
(60, 12, 7, '32GB'), (61, 12, 8, 'DDR5'), (62, 12, 10, '6000MHz'),
-- ── Corsair DDR5 64GB (variant 13) ──
(63, 13, 7, '64GB'), (64, 13, 8, 'DDR5'), (65, 13, 10, '6000MHz'),
-- ── Kingston DDR4 16GB (variant 14) ──
(66, 14, 7, '16GB'), (67, 14, 8, 'DDR4'), (68, 14, 10, '3200MHz'),
-- ── Kingston DDR4 32GB (variant 15) ──
(69, 15, 7, '32GB'), (70, 15, 8, 'DDR4'), (71, 15, 10, '3200MHz'),
-- ── G.Skill DDR5 32GB (variant 51) ──
(239, 51, 7, '32GB'), (240, 51, 8, 'DDR5'), (241, 51, 10, '6400MHz'),
-- ── G.Skill DDR5 64GB (variant 52) ──
(242, 52, 7, '64GB'), (243, 52, 8, 'DDR5'), (244, 52, 10, '6400MHz'),

-- ── Samsung 990 PRO 1TB (variant 16) ──
(72, 16, 7, '1TB'), (73, 16, 12, 'PCIe 4.0 NVMe M.2'),
(74, 16, 13, '7450MB/s'), (75, 16, 14, '6900MB/s'),
-- ── Samsung 990 PRO 2TB (variant 17) ──
(76, 17, 7, '2TB'), (77, 17, 12, 'PCIe 4.0 NVMe M.2'),
(78, 17, 13, '7450MB/s'), (79, 17, 14, '6900MB/s'),
-- ── Seagate FireCuda 530 1TB (variant 53) ──
(245, 53, 7, '1TB'), (246, 53, 12, 'PCIe 4.0 NVMe M.2'),
(247, 53, 13, '7300MB/s'), (248, 53, 14, '6900MB/s'),
-- ── Seagate FireCuda 530 2TB (variant 54) ──
(249, 54, 7, '2TB'), (250, 54, 12, 'PCIe 4.0 NVMe M.2'),
(251, 54, 13, '7300MB/s'), (252, 54, 14, '6900MB/s'),

-- ── RM1000x (variant 18) ──
(80, 18, 15, '1000W'), (81, 18, 16, '80+ Gold'),
-- ── CV650 (variant 26) ──
(99, 26, 15, '650W'), (100, 26, 16, '80+ Bronze'),
-- ── RM750x (variant 27) ──
(101, 27, 15, '750W'), (102, 27, 16, '80+ Gold'),
-- ── RM850x (variant 55) ──
(253, 55, 15, '850W'), (254, 55, 16, '80+ Gold'),

-- ── NZXT H7 Flow Black (variant 19) ──
(82, 19, 11, 'ATX'), (83, 19, 19, '400mm'), (84, 19, 20, '185mm'),
-- ── NZXT H7 Flow White (variant 20) ──
(85, 20, 11, 'ATX'), (86, 20, 19, '400mm'), (87, 20, 20, '185mm'),
-- ── Fractal Pop Mini Black (variant 21) — RTX4090 (357/340/336mm) KHÔNG vừa, RX7900XTX (287mm) vừa ──
(88, 21, 11, 'mATX'), (89, 21, 19, '300mm'), (90, 21, 20, '160mm'),
-- ── Lian Li O11 Black (variant 56) ──
(255, 56, 11, 'ATX'), (256, 56, 19, '420mm'), (257, 56, 20, '167mm'),
-- ── Lian Li O11 White (variant 57) ──
(258, 57, 11, 'ATX'), (259, 57, 19, '420mm'), (260, 57, 20, '167mm'),

-- ── Noctua NH-D15 (variant 22) — vừa H7 Flow (185mm), KHÔNG vừa Fractal Pop Mini (160mm) ──
(91, 22, 22, '165mm'),
-- ── Corsair H150i LCD Black (variant 58) ──
(261, 58, 22, '50mm'),  -- AIO, chiều cao pump nhỏ
-- ── Corsair H150i LCD White (variant 59) ──
(262, 59, 22, '50mm'),

-- ── Monitor: LG 27GP850-B (variant 34) ──
(103, 34, 23, '27"'), (104, 34, 24, '180Hz'), (105, 34, 25, '2560x1440'),
(106, 34, 26, '1ms'),  (107, 34, 27, 'IPS'),
-- ── Monitor: ASUS PG279QM (variant 62) ──
(263, 62, 23, '27"'), (264, 62, 24, '240Hz'), (265, 62, 25, '2560x1440'),
(266, 62, 26, '1ms'),  (267, 62, 27, 'IPS'),
-- ── Monitor: Dell U2723D (variant 35) ──
(108, 35, 23, '27"'), (109, 35, 24, '60Hz'), (110, 35, 25, '3840x2160'),
(111, 35, 26, '5ms'),  (112, 35, 27, 'IPS Black'),
-- ── Monitor: LG 34WP85C (variant 63) ──
(268, 63, 23, '34"'), (269, 63, 24, '160Hz'), (270, 63, 25, '3440x1440'),
(271, 63, 26, '1ms'),  (272, 63, 27, 'IPS'),

-- ── Keychron K2 Brown (variant 36) ──
(113, 36, 11, 'TKL 75%'),
-- ── Keychron K2 Red (variant 37) ──
(114, 37, 11, 'TKL 75%'),
-- ── Corsair K70 Red (variant 64) ──
(273, 64, 11, 'Full-size'),
-- ── Corsair K70 Brown (variant 65) ──
(274, 65, 11, 'Full-size'),

-- ── MacBook Pro 14 M3 Pro 18GB/512GB (variant 28) ──
(115, 28, 7,  '18GB'), (116, 28, 8,  'LPDDR5'),
(117, 28, 23, '14.2"'),(118, 28, 24, '120Hz'),
(119, 28, 25, '3024x1964'), (120, 28, 27, 'Liquid Retina XDR'),
-- ── MacBook Pro 14 M3 Pro 36GB/1TB (variant 29) ──
(121, 29, 7,  '36GB'), (122, 29, 8,  'LPDDR5'),
(123, 29, 23, '14.2"'),(124, 29, 24, '120Hz'),
(125, 29, 25, '3024x1964'), (126, 29, 27, 'Liquid Retina XDR'),
-- ── ASUS ROG G16 RTX4070 (variant 30) ──
(127, 30, 7,  '16GB'), (128, 30, 8,  'DDR5'),
(129, 30, 23, '16"'),  (130, 30, 24, '240Hz'),
(131, 30, 25, '2560x1600'), (132, 30, 27, 'IPS'),
-- ── ASUS ROG G16 RTX4060 (variant 31) ──
(133, 31, 7,  '16GB'), (134, 31, 8,  'DDR5'),
(135, 31, 23, '16"'),  (136, 31, 24, '240Hz'),
(137, 31, 25, '2560x1600'), (138, 31, 27, 'IPS'),
-- ── Acer Nitro V 15 (variant 60) ──
(275, 60, 7,  '16GB'), (276, 60, 8,  'DDR5'),
(277, 60, 23, '15.6"'),(278, 60, 24, '144Hz'),
(279, 60, 25, '1920x1080'), (280, 60, 27, 'IPS'),
-- ── ThinkPad X1 Carbon 16GB (variant 32) ──
(139, 32, 7,  '16GB'), (140, 32, 8,  'LPDDR5'),
(141, 32, 23, '14"'),  (142, 32, 24, '60Hz'),
(143, 32, 25, '1920x1200'), (144, 32, 27, 'IPS'),
-- ── ThinkPad X1 Carbon 32GB (variant 33) ──
(145, 33, 7,  '32GB'), (146, 33, 8,  'LPDDR5'),
(147, 33, 23, '14"'),  (148, 33, 24, '60Hz'),
(149, 33, 25, '1920x1200'), (150, 33, 27, 'IPS'),
-- ── Dell XPS 15 OLED (variant 61) ──
(281, 61, 7,  '32GB'), (282, 61, 8,  'DDR5'),
(283, 61, 23, '15.6"'),(284, 61, 24, '60Hz'),
(285, 61, 25, '3456x2160'), (286, 61, 27, 'OLED');
SET IDENTITY_INSERT product_variant_attributes OFF;

-- ── Compatibility Rules ──────────────────────────────────────────────────────
SET IDENTITY_INSERT compatibility_rules ON;
INSERT INTO compatibility_rules (rule_id, component_type_1, component_type_2, attribute_1, attribute_2, rule_type, description)
VALUES
(1, 'CPU',      'MAINBOARD', 'Socket',       'Socket',          'MUST_MATCH',   'CPU socket phải khớp mainboard (AM5↔AM5, LGA1700↔LGA1700)'),
(2, 'MAINBOARD','RAM',       'Memory Type',  'Memory Type',     'MUST_MATCH',   'Mainboard và RAM phải cùng thế hệ DDR (DDR4 hoặc DDR5)'),
(3, 'RAM',      'MAINBOARD', 'Memory Speed', 'Max RAM Speed',   'MUST_SUPPORT', 'Tốc độ RAM không được vượt quá tốc độ tối đa mainboard hỗ trợ'),
(4, 'GPU',      'CASE',      'GPU Length',   'Max GPU Length',  'MUST_FIT',     'Chiều dài GPU không được vượt quá khoảng trống tối đa trong case'),
(5, 'COOLING',  'CASE',      'Cooler Height','Max Cooler Height','MUST_FIT',    'Chiều cao tản nhiệt không được vượt quá giới hạn trong case'),
(6, 'MAINBOARD','CASE',      'Form Factor',  'Form Factor',     'MUST_MATCH',   'Form factor mainboard phải được case hỗ trợ'),
(7, 'PSU',      NULL,        'Wattage',      NULL,              'MIN_WATTAGE',  'Công suất PSU phải đủ để cấp điện toàn hệ thống (TDP × 1.2)'),
(8, 'CPU',      'MAINBOARD', 'Memory Type',  'Memory Type',     'MUST_MATCH',   'CPU hỗ trợ thế hệ DDR nào phải khớp mainboard (DDR4 vs DDR5)');
SET IDENTITY_INSERT compatibility_rules OFF;

-- ── Product Images ───────────────────────────────────────────────────────────
SET IDENTITY_INSERT product_images ON;
INSERT INTO product_images (image_id, product_id, image_url, is_thumbnail)
VALUES
(1,  1,  'https://placehold.co/600x400?text=i9-14900K',        1),
(2,  2,  'https://placehold.co/600x400?text=R9-7950X',         1),
(3,  3,  'https://placehold.co/600x400?text=RTX-4090',         1),
(4,  4,  'https://placehold.co/600x400?text=RX-7900-XTX',      1),
(5,  5,  'https://placehold.co/600x400?text=Z790-E',           1),
(6,  6,  'https://placehold.co/600x400?text=MSI-X670E',        1),
(7,  7,  'https://placehold.co/600x400?text=ASUS-B450M',       1),
(8,  8,  'https://placehold.co/600x400?text=DDR5-RAM',         1),
(9,  9,  'https://placehold.co/600x400?text=DDR4-RAM',         1),
(10, 10, 'https://placehold.co/600x400?text=990-PRO-SSD',      1),
(11, 11, 'https://placehold.co/600x400?text=RM1000x-PSU',      1),
(12, 12, 'https://placehold.co/600x400?text=NZXT-H7-Flow',     1),
(13, 13, 'https://placehold.co/600x400?text=Fractal-Pop-Mini', 1),
(14, 14, 'https://placehold.co/600x400?text=Noctua-NH-D15',    1),
(15, 15, 'https://placehold.co/600x400?text=GPX-SL2-Mouse',    1),
(16, 16, 'https://placehold.co/600x400?text=Ryzen5-5600X',     1),
(17, 17, 'https://placehold.co/600x400?text=CV650-PSU',        1),
(18, 18, 'https://placehold.co/600x400?text=RM750x-PSU',       1),
(19, 19, 'https://placehold.co/600x400?text=MacBook-Pro-M3',   1),
(20, 20, 'https://placehold.co/600x400?text=ROG-Strix-G16',    1),
(21, 21, 'https://placehold.co/600x400?text=ThinkPad-X1C',     1),
(22, 22, 'https://placehold.co/600x400?text=LG-27GP850',       1),
(23, 23, 'https://placehold.co/600x400?text=Dell-U2723D',      1),
(24, 24, 'https://placehold.co/600x400?text=Keychron-K2',      1),
(25, 25, 'https://placehold.co/600x400?text=Arctis-Nova-Pro',  1),
(26, 29, 'https://placehold.co/600x400?text=i5-13600K',        1),
(27, 30, 'https://placehold.co/600x400?text=RTX-4070-Super',   1),
(28, 31, 'https://placehold.co/600x400?text=RTX-4060',         1),
(29, 32, 'https://placehold.co/600x400?text=MSI-B760M',        1),
(30, 33, 'https://placehold.co/600x400?text=GSkill-DDR5',      1),
(31, 34, 'https://placehold.co/600x400?text=FireCuda-530',     1),
(32, 35, 'https://placehold.co/600x400?text=RM850x-PSU',       1),
(33, 36, 'https://placehold.co/600x400?text=Lian-Li-O11',      1),
(34, 37, 'https://placehold.co/600x400?text=Corsair-H150i',    1),
(35, 38, 'https://placehold.co/600x400?text=Acer-Nitro-V15',   1),
(36, 39, 'https://placehold.co/600x400?text=Dell-XPS15',       1),
(37, 40, 'https://placehold.co/600x400?text=ASUS-PG279QM',     1),
(38, 41, 'https://placehold.co/600x400?text=LG-34WP85C',       1),
(39, 42, 'https://placehold.co/600x400?text=Corsair-K70',      1),
(40, 43, 'https://placehold.co/600x400?text=HyperX-Cloud-Alpha',1);
SET IDENTITY_INSERT product_images OFF;

-- ── Blogs ────────────────────────────────────────────────────────────────────
SET IDENTITY_INSERT blogs ON;
INSERT INTO blogs (blog_id, user_id, title, content, published_at)
VALUES
(1, 1, N'Top PC Gaming Build 2026 — Tất tần tật từ A đến Z',
        N'Hướng dẫn chi tiết cách chọn linh kiện, lắp ráp và tối ưu PC gaming cho năm 2026 với ngân sách từ 20 đến 80 triệu.', DATEADD(day,-30,GETDATE())),
(2, 2, N'DDR5 vs DDR4: Nâng cấp có đáng không?',
        N'So sánh chi tiết hiệu năng DDR5 và DDR4 trong gaming, render và đa nhiệm. Khi nào nên nâng cấp?', DATEADD(day,-20,GETDATE())),
(3, 1, N'Hướng dẫn chọn màn hình gaming phù hợp',
        N'Phân tích các thông số refresh rate, response time, panel type để giúp bạn chọn màn hình gaming phù hợp.', DATEADD(day,-15,GETDATE())),
(4, 3, N'MacBook Pro M3 Pro: Laptop tốt nhất cho developer?',
        N'Đánh giá toàn diện MacBook Pro 14 M3 Pro sau 1 tháng sử dụng cho lập trình, Docker và thiết kế.', DATEADD(day,-10,GETDATE())),
(5, 2, N'Hướng dẫn Build PC tích hợp Water Cooling',
        N'Từng bước lắp đặt tản nhiệt nước AIO 360mm, quản lý cáp và tối ưu nhiệt độ cho PC gaming cao cấp.', DATEADD(day,-5,GETDATE()));
SET IDENTITY_INSERT blogs OFF;

-- ── Promotions ───────────────────────────────────────────────────────────────
SET IDENTITY_INSERT promotions ON;
INSERT INTO promotions (promotion_id, promo_code, discount_percent, start_date, end_date)
VALUES
(1, 'SALE2026',    15, '2026-01-01', '2026-12-31'),
(2, 'NEWMEMBER10', 10, '2026-01-01', '2026-12-31'),
(3, 'SUMMER25',    25, '2026-06-01', '2026-08-31'),
(4, 'LAPTOP15',    15, '2026-03-01', '2026-05-31');
SET IDENTITY_INSERT promotions OFF;

SET IDENTITY_INSERT promotion_product ON;
INSERT INTO promotion_product (promo_prod_id, promotion_id, product_id)
VALUES
(1,  1, 3),  -- RTX 4090
(2,  1, 4),  -- RX 7900 XTX
(3,  1, 30), -- RTX 4070 Super
(4,  2, 8),  -- Corsair DDR5 RAM
(5,  2, 10), -- Samsung 990 PRO SSD
(6,  3, 1),  -- i9-14900K
(7,  3, 2),  -- Ryzen 9 7950X
(8,  4, 19), -- MacBook Pro M3 Pro
(9,  4, 20), -- ASUS ROG Strix G16
(10, 4, 21); -- ThinkPad X1 Carbon
SET IDENTITY_INSERT promotion_product OFF;

-- ── Reviews ──────────────────────────────────────────────────────────────────
SET IDENTITY_INSERT reviews ON;
INSERT INTO reviews (review_id, user_id, product_id, rating, comment, created_at)
VALUES
(1,  4, 1,  5, N'CPU cực kỳ mạnh, chạy game 4K max setting mượt mà.', DATEADD(day,-25,GETDATE())),
(2,  4, 3,  5, N'RTX 4090 quá mạnh, overkill với game nhưng render video rất nhanh.', DATEADD(day,-20,GETDATE())),
(3,  5, 10, 4, N'SSD rất nhanh, khởi động Windows chưa đến 5 giây.', DATEADD(day,-18,GETDATE())),
(4,  5, 15, 5, N'Chuột không dây tốt nhất từng dùng, không lag, pin lâu.', DATEADD(day,-15,GETDATE())),
(5,  4, 8,  4, N'RAM DDR5 cài XMP dễ, hiệu năng tăng rõ rệt.', DATEADD(day,-12,GETDATE())),
(6,  5, 19, 5, N'MacBook M3 Pro pin dùng cả ngày không hết, hiệu năng đỉnh cho dev.', DATEADD(day,-10,GETDATE())),
(7,  6, 22, 5, N'Màn hình 180Hz cực mượt khi chơi Valorant, màu sắc đẹp.', DATEADD(day,-8,GETDATE())),
(8,  4, 24, 4, N'Bàn phím cơ chất lượng tốt cho tầm giá, Bluetooth kết nối ổn.', DATEADD(day,-6,GETDATE())),
(9,  6, 20, 5, N'Laptop gaming cực mạnh, RTX 4070 chạy Cyberpunk 2077 1440p 60fps+.', DATEADD(day,-4,GETDATE())),
(10, 5, 14, 5, N'Tản nhiệt Noctua êm và hiệu quả, CPU i9 không quá 75 độ khi full load.', DATEADD(day,-2,GETDATE()));
SET IDENTITY_INSERT reviews OFF;

-- ── Product Items (serial numbers — 3 per variant for demo) ─────────────────
-- item_id = (variant_order - 1) * 3 + offset, variant_id referenced
SET IDENTITY_INSERT product_items ON;
INSERT INTO product_items (item_id, variant_id, serial_number)
VALUES
-- variant 1: i9-14900K Box
(1, 1,'SN-CPU-I9-14900K-001'),(2, 1,'SN-CPU-I9-14900K-002'),(3, 1,'SN-CPU-I9-14900K-003'),
-- variant 2: i9-14900K Tray
(4, 2,'SN-CPU-I9-14900K-T-001'),(5, 2,'SN-CPU-I9-14900K-T-002'),(6, 2,'SN-CPU-I9-14900K-T-003'),
-- variant 3: Ryzen 9 7950X
(7, 3,'SN-CPU-R9-7950X-001'),(8, 3,'SN-CPU-R9-7950X-002'),(9, 3,'SN-CPU-R9-7950X-003'),
-- variant 4: RTX 4090 ASUS
(10, 4,'SN-GPU-4090-ASUS-001'),(11, 4,'SN-GPU-4090-ASUS-002'),(12, 4,'SN-GPU-4090-ASUS-003'),
-- variant 5: RTX 4090 MSI
(13, 5,'SN-GPU-4090-MSI-001'),(14, 5,'SN-GPU-4090-MSI-002'),(15, 5,'SN-GPU-4090-MSI-003'),
-- variant 6: RTX 4090 FE
(16, 6,'SN-GPU-4090-FE-001'),(17, 6,'SN-GPU-4090-FE-002'),(18, 6,'SN-GPU-4090-FE-003'),
-- variant 7: RX 7900 XTX
(19, 7,'SN-GPU-7900XTX-001'),(20, 7,'SN-GPU-7900XTX-002'),(21, 7,'SN-GPU-7900XTX-003'),
-- variant 8: Z790-E mainboard
(22, 8,'SN-MB-Z790E-001'),(23, 8,'SN-MB-Z790E-002'),(24, 8,'SN-MB-Z790E-003'),
-- variant 9: MSI X670E
(25, 9,'SN-MB-X670E-001'),(26, 9,'SN-MB-X670E-002'),(27, 9,'SN-MB-X670E-003'),
-- variant 10: ASUS B450M
(28, 10,'SN-MB-B450M-001'),(29, 10,'SN-MB-B450M-002'),(30, 10,'SN-MB-B450M-003'),
-- variant 11: DDR5 16GB
(31, 11,'SN-RAM-DDR5-16G-001'),(32, 11,'SN-RAM-DDR5-16G-002'),(33, 11,'SN-RAM-DDR5-16G-003'),
-- variant 12: DDR5 32GB
(34, 12,'SN-RAM-DDR5-32G-001'),(35, 12,'SN-RAM-DDR5-32G-002'),(36, 12,'SN-RAM-DDR5-32G-003'),
-- variant 13: DDR5 64GB
(37, 13,'SN-RAM-DDR5-64G-001'),(38, 13,'SN-RAM-DDR5-64G-002'),(39, 13,'SN-RAM-DDR5-64G-003'),
-- variant 14: DDR4 16GB
(40, 14,'SN-RAM-DDR4-16G-001'),(41, 14,'SN-RAM-DDR4-16G-002'),(42, 14,'SN-RAM-DDR4-16G-003'),
-- variant 15: DDR4 32GB
(43, 15,'SN-RAM-DDR4-32G-001'),(44, 15,'SN-RAM-DDR4-32G-002'),(45, 15,'SN-RAM-DDR4-32G-003'),
-- variant 16: Samsung 990 PRO 1TB
(46, 16,'SN-SSD-990P-1TB-001'),(47, 16,'SN-SSD-990P-1TB-002'),(48, 16,'SN-SSD-990P-1TB-003'),
-- variant 17: Samsung 990 PRO 2TB
(49, 17,'SN-SSD-990P-2TB-001'),(50, 17,'SN-SSD-990P-2TB-002'),(51, 17,'SN-SSD-990P-2TB-003'),
-- variant 18: RM1000x PSU
(52, 18,'SN-PSU-RM1000X-001'),(53, 18,'SN-PSU-RM1000X-002'),(54, 18,'SN-PSU-RM1000X-003'),
-- variant 19: NZXT H7 Flow Black
(55, 19,'SN-CASE-H7-BLK-001'),(56, 19,'SN-CASE-H7-BLK-002'),(57, 19,'SN-CASE-H7-BLK-003'),
-- variant 20: NZXT H7 Flow White
(58, 20,'SN-CASE-H7-WHT-001'),(59, 20,'SN-CASE-H7-WHT-002'),(60, 20,'SN-CASE-H7-WHT-003'),
-- variant 21: Fractal Pop Mini
(61, 21,'SN-CASE-FRACTAL-001'),(62, 21,'SN-CASE-FRACTAL-002'),(63, 21,'SN-CASE-FRACTAL-003'),
-- variant 22: Noctua NH-D15
(64, 22,'SN-COOL-NHD15-001'),(65, 22,'SN-COOL-NHD15-002'),(66, 22,'SN-COOL-NHD15-003'),
-- variant 23: G Pro X Superlight 2 Black
(67, 23,'SN-MOUSE-GPX-BLK-001'),(68, 23,'SN-MOUSE-GPX-BLK-002'),(69, 23,'SN-MOUSE-GPX-BLK-003'),
-- variant 24: G Pro X Superlight 2 White
(70, 24,'SN-MOUSE-GPX-WHT-001'),(71, 24,'SN-MOUSE-GPX-WHT-002'),(72, 24,'SN-MOUSE-GPX-WHT-003'),
-- variant 25: Ryzen 5 5600X
(73, 25,'SN-CPU-R5-5600X-001'),(74, 25,'SN-CPU-R5-5600X-002'),(75, 25,'SN-CPU-R5-5600X-003'),
-- variant 26: CV650
(76, 26,'SN-PSU-CV650-001'),(77, 26,'SN-PSU-CV650-002'),(78, 26,'SN-PSU-CV650-003'),
-- variant 27: RM750x
(79, 27,'SN-PSU-RM750X-001'),(80, 27,'SN-PSU-RM750X-002'),(81, 27,'SN-PSU-RM750X-003'),
-- variant 28: MacBook Pro 14 M3 Pro 18GB
(82, 28,'SN-MBP14-M3P-18-001'),(83, 28,'SN-MBP14-M3P-18-002'),(84, 28,'SN-MBP14-M3P-18-003'),
-- variant 29: MacBook Pro 14 M3 Pro 36GB
(85, 29,'SN-MBP14-M3P-36-001'),(86, 29,'SN-MBP14-M3P-36-002'),(87, 29,'SN-MBP14-M3P-36-003'),
-- variant 30: ROG Strix G16 RTX4070
(88, 30,'SN-ROG-G16-4070-001'),(89, 30,'SN-ROG-G16-4070-002'),(90, 30,'SN-ROG-G16-4070-003'),
-- variant 31: ROG Strix G16 RTX4060
(91, 31,'SN-ROG-G16-4060-001'),(92, 31,'SN-ROG-G16-4060-002'),(93, 31,'SN-ROG-G16-4060-003'),
-- variant 32: ThinkPad X1 Carbon 16GB
(94, 32,'SN-X1C-16-001'),(95, 32,'SN-X1C-16-002'),(96, 32,'SN-X1C-16-003'),
-- variant 33: ThinkPad X1 Carbon 32GB
(97, 33,'SN-X1C-32-001'),(98, 33,'SN-X1C-32-002'),(99, 33,'SN-X1C-32-003'),
-- variant 34: LG 27GP850-B
(100, 34,'SN-MON-27GP850-001'),(101, 34,'SN-MON-27GP850-002'),(102, 34,'SN-MON-27GP850-003'),
-- variant 35: Dell U2723D
(103, 35,'SN-MON-U2723D-001'),(104, 35,'SN-MON-U2723D-002'),(105, 35,'SN-MON-U2723D-003'),
-- variant 36: Keychron K2 Brown
(106, 36,'SN-KB-K2-BROWN-001'),(107, 36,'SN-KB-K2-BROWN-002'),(108, 36,'SN-KB-K2-BROWN-003'),
-- variant 37: Keychron K2 Red
(109, 37,'SN-KB-K2-RED-001'),(110, 37,'SN-KB-K2-RED-002'),(111, 37,'SN-KB-K2-RED-003'),
-- variant 38: Arctis Nova Pro Wireless
(112, 38,'SN-HS-NOVA-001'),(113, 38,'SN-HS-NOVA-002'),(114, 38,'SN-HS-NOVA-003'),
-- variant 44: i5-13600K Box
(115,44,'SN-CPU-I5-13600K-001'),(116,44,'SN-CPU-I5-13600K-002'),(117,44,'SN-CPU-I5-13600K-003'),
-- variant 45: i5-13600K Tray
(118,45,'SN-CPU-I5-13600K-T-001'),(119,45,'SN-CPU-I5-13600K-T-002'),(120,45,'SN-CPU-I5-13600K-T-003'),
-- variant 46: RTX 4070 Super ASUS
(121,46,'SN-GPU-4070S-ASUS-001'),(122,46,'SN-GPU-4070S-ASUS-002'),(123,46,'SN-GPU-4070S-ASUS-003'),
-- variant 47: RTX 4070 Super MSI
(124,47,'SN-GPU-4070S-MSI-001'),(125,47,'SN-GPU-4070S-MSI-002'),(126,47,'SN-GPU-4070S-MSI-003'),
-- variant 48: RTX 4060 ASUS
(127,48,'SN-GPU-4060-ASUS-001'),(128,48,'SN-GPU-4060-ASUS-002'),(129,48,'SN-GPU-4060-ASUS-003'),
-- variant 49: RTX 4060 MSI
(130,49,'SN-GPU-4060-MSI-001'),(131,49,'SN-GPU-4060-MSI-002'),(132,49,'SN-GPU-4060-MSI-003'),
-- variant 50: MSI PRO B760M mATX
(133,50,'SN-MB-B760M-001'),(134,50,'SN-MB-B760M-002'),(135,50,'SN-MB-B760M-003'),
-- variant 51: G.Skill DDR5 32GB
(136,51,'SN-RAM-GS-DDR5-32G-001'),(137,51,'SN-RAM-GS-DDR5-32G-002'),(138,51,'SN-RAM-GS-DDR5-32G-003'),
-- variant 52: G.Skill DDR5 64GB
(139,52,'SN-RAM-GS-DDR5-64G-001'),(140,52,'SN-RAM-GS-DDR5-64G-002'),(141,52,'SN-RAM-GS-DDR5-64G-003'),
-- variant 53: Seagate FireCuda 530 1TB
(142,53,'SN-SSD-FC530-1TB-001'),(143,53,'SN-SSD-FC530-1TB-002'),(144,53,'SN-SSD-FC530-1TB-003'),
-- variant 54: Seagate FireCuda 530 2TB
(145,54,'SN-SSD-FC530-2TB-001'),(146,54,'SN-SSD-FC530-2TB-002'),(147,54,'SN-SSD-FC530-2TB-003'),
-- variant 55: Corsair RM850x
(148,55,'SN-PSU-RM850X-001'),(149,55,'SN-PSU-RM850X-002'),(150,55,'SN-PSU-RM850X-003'),
-- variant 56: Lian Li O11 Black
(151,56,'SN-CASE-O11-BLK-001'),(152,56,'SN-CASE-O11-BLK-002'),(153,56,'SN-CASE-O11-BLK-003'),
-- variant 57: Lian Li O11 White
(154,57,'SN-CASE-O11-WHT-001'),(155,57,'SN-CASE-O11-WHT-002'),(156,57,'SN-CASE-O11-WHT-003'),
-- variant 58: Corsair H150i LCD Black
(157,58,'SN-COOL-H150I-BLK-001'),(158,58,'SN-COOL-H150I-BLK-002'),(159,58,'SN-COOL-H150I-BLK-003'),
-- variant 59: Corsair H150i LCD White
(160,59,'SN-COOL-H150I-WHT-001'),(161,59,'SN-COOL-H150I-WHT-002'),(162,59,'SN-COOL-H150I-WHT-003'),
-- variant 60: Acer Nitro V15
(163,60,'SN-LAPTOP-NITROV15-001'),(164,60,'SN-LAPTOP-NITROV15-002'),(165,60,'SN-LAPTOP-NITROV15-003'),
-- variant 61: Dell XPS 15 OLED
(166,61,'SN-LAPTOP-XPS15-001'),(167,61,'SN-LAPTOP-XPS15-002'),(168,61,'SN-LAPTOP-XPS15-003'),
-- variant 62: ASUS ROG Swift PG279QM
(169,62,'SN-MON-PG279QM-001'),(170,62,'SN-MON-PG279QM-002'),(171,62,'SN-MON-PG279QM-003'),
-- variant 63: LG 34WP85C-B UltraWide
(172,63,'SN-MON-34WP85C-001'),(173,63,'SN-MON-34WP85C-002'),(174,63,'SN-MON-34WP85C-003'),
-- variant 64: Corsair K70 RGB MX Red
(175,64,'SN-KB-K70-RED-001'),(176,64,'SN-KB-K70-RED-002'),(177,64,'SN-KB-K70-RED-003'),
-- variant 65: Corsair K70 RGB MX Brown
(178,65,'SN-KB-K70-BROWN-001'),(179,65,'SN-KB-K70-BROWN-002'),(180,65,'SN-KB-K70-BROWN-003'),
-- variant 66: HyperX Cloud Alpha Wireless
(181,66,'SN-HS-CLOUDALPHA-001'),(182,66,'SN-HS-CLOUDALPHA-002'),(183,66,'SN-HS-CLOUDALPHA-003');
SET IDENTITY_INSERT product_items OFF;

-- ── Carts ────────────────────────────────────────────────────────────────────
SET IDENTITY_INSERT carts ON;
INSERT INTO carts (cart_id, user_id, created_at)
VALUES
(1, 4, GETDATE()),  -- member1
(2, 5, GETDATE()),  -- member2
(3, 6, GETDATE());  -- member3
SET IDENTITY_INSERT carts OFF;

SET IDENTITY_INSERT cart_items ON;
INSERT INTO cart_items (cart_item_id, cart_id, variant_id, quantity)
VALUES
-- member1 đang xem xét PC gaming
(1, 1, 1,  1),  -- i9-14900K Box
(2, 1, 4,  1),  -- RTX 4090 ASUS
(3, 1, 12, 1),  -- DDR5 32GB
-- member2 quan tâm laptop
(4, 2, 28, 1),  -- MacBook Pro 14 M3 Pro 18GB
(5, 2, 34, 1),  -- LG UltraGear 27GP850-B
-- member3 build tầm trung
(6, 3, 44, 1),  -- i5-13600K Box
(7, 3, 48, 1),  -- RTX 4060 ASUS
(8, 3, 11, 1);  -- DDR5 16GB
SET IDENTITY_INSERT cart_items OFF;

-- ── Orders ───────────────────────────────────────────────────────────────────
-- Orders dùng item từ product_items:
--   Order 1 (DELIVERED): item 1 (i9-14900K Box v1), item 10 (RTX4090 ASUS v4)
--   Order 2 (PROCESSING, installment): item 82 (MacBook Pro 14 M3 Pro 18GB v28)
--   Order 3 (PENDING, CASH): item 46 (Samsung 990 PRO 1TB v16)
--   Order 4 (CANCELLED): item 88 (ROG Strix G16 RTX4070 v30)
SET IDENTITY_INSERT orders ON;
INSERT INTO orders (order_id, user_id, total_amount, status, payment_method, payment_mode, installment_package_id, order_date)
VALUES
(1, 4, 59780000,  'DELIVERED',  'VNPAY', 'FULL',        NULL, DATEADD(day,-30,GETDATE())),
(2, 5, 49990000,  'PROCESSING', 'VNPAY', 'INSTALLMENT',  3,    DATEADD(day,-10,GETDATE())),
(3, 6, 2990000,   'PENDING',    'COD',   'FULL',         NULL, DATEADD(day, -2,GETDATE())),
(4, 4, 42990000,  'CANCELLED',  'VNPAY', 'FULL',        NULL, DATEADD(day,-20,GETDATE()));
SET IDENTITY_INSERT orders OFF;

SET IDENTITY_INSERT order_items ON;
INSERT INTO order_items (order_item_id, order_id, item_id, quantity, unit_price, recipient_name, recipient_phone, shipping_address)
VALUES
-- Order 1: i9-14900K Box + RTX 4090 ASUS
(1, 1,  1,  1, 14790000, N'Nguyễn Văn A', '0903456789', N'123 Lê Lợi, Quận 1, TP.HCM'),
(2, 1,  10, 1, 44990000, N'Nguyễn Văn A', '0903456789', N'123 Lê Lợi, Quận 1, TP.HCM'),
-- Order 2: MacBook Pro 14 M3 Pro 18GB (trả góp)
(3, 2,  82, 1, 49990000, N'Trần Thị B',   '0904567890', N'456 Nguyễn Huệ, Quận 1, TP.HCM'),
-- Order 3: Samsung 990 PRO 1TB
(4, 3,  46, 1,  2990000, N'Lê Văn C',     '0905678901', N'789 Hai Bà Trưng, Quận 3, TP.HCM'),
-- Order 4: ROG Strix G16 (CANCELLED — không tạo warranty)
(5, 4,  88, 1, 42990000, N'Nguyễn Văn A', '0903456789', N'123 Lê Lợi, Quận 1, TP.HCM');
SET IDENTITY_INSERT order_items OFF;

-- ── Order Payment Schedule (installment cho Order 2) ─────────────────────────
-- Order 2: 49,990,000đ, gói 12 tháng lãi 1.5%, trả trước 20%
-- Trả trước: 9,998,000đ. Còn lại: 39,992,000đ / 12 = ~3,332,667đ/tháng
SET IDENTITY_INSERT order_payment_schedule ON;
INSERT INTO order_payment_schedule (payment_schedule_id, order_id, installment_no, amount, due_date, paid_date, vnp_transaction_no, status)
VALUES
(1,  2,  1,  9998000, DATEADD(day,-10,CAST(GETDATE() AS DATE)), DATEADD(day,-10,CAST(GETDATE() AS DATE)), 'VNP-TXN-20260310-001', 'PAID'),
(2,  2,  2,  3332667, DATEADD(month, 1,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(3,  2,  3,  3332667, DATEADD(month, 2,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(4,  2,  4,  3332667, DATEADD(month, 3,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(5,  2,  5,  3332667, DATEADD(month, 4,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(6,  2,  6,  3332667, DATEADD(month, 5,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(7,  2,  7,  3332667, DATEADD(month, 6,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(8,  2,  8,  3332667, DATEADD(month, 7,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(9,  2,  9,  3332667, DATEADD(month, 8,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(10, 2, 10,  3332667, DATEADD(month, 9,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(11, 2, 11,  3332667, DATEADD(month,10,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID'),
(12, 2, 12,  3332331, DATEADD(month,11,DATEADD(day,-10,CAST(GETDATE() AS DATE))), NULL, NULL, 'UNPAID');
SET IDENTITY_INSERT order_payment_schedule OFF;

-- ── Warranties (cho Order 1 - DELIVERED) ─────────────────────────────────────
SET IDENTITY_INSERT warranties ON;
INSERT INTO warranties (id, order_item_id, serial_number, start_date, end_date, description, status, type)
VALUES
(1, 1, 'SN-CPU-I9-14900K-001',
   DATEADD(day,-30,CAST(GETDATE() AS DATE)),
   DATEADD(month,36,DATEADD(day,-30,CAST(GETDATE() AS DATE))),
   N'Bảo hành CPU Intel Core i9-14900K 36 tháng tại cửa hàng.',
   'ACTIVE', 'STORE'),
(2, 2, 'SN-GPU-4090-ASUS-001',
   DATEADD(day,-30,CAST(GETDATE() AS DATE)),
   DATEADD(month,36,DATEADD(day,-30,CAST(GETDATE() AS DATE))),
   N'Bảo hành VGA ASUS ROG STRIX RTX 4090 36 tháng tại cửa hàng.',
   'ACTIVE', 'STORE');
SET IDENTITY_INSERT warranties OFF;

-- ── Warranty Claims (1 claim demo cho warranty 2 - GPU) ──────────────────────
SET IDENTITY_INSERT warranty_claim ON;
INSERT INTO warranty_claim (claim_id, warranty_id, claim_date, customer_note, technician_note, status, solution_type, return_date)
VALUES
(1, 2,
 DATEADD(day,-5,CAST(GETDATE() AS DATE)),
 N'Card màn hình xuất hiện artifact khi chơi game nặng, nhiệt độ tăng bất thường.',
 N'Đã kiểm tra: thermal paste khô, tản nhiệt GPU kém tiếp xúc. Đã thay thermal pad và paste.',
 'RESOLVED', 'REPAIR',
 CAST(GETDATE() AS DATE));
SET IDENTITY_INSERT warranty_claim OFF;

-- ── PC Builds (demo builds) ───────────────────────────────────────────────────
-- Build 1: Gaming cao cấp — tất cả tương thích ✅
-- Build 2: Văn phòng tầm trung AM4/DDR4 — tất cả tương thích ✅
-- Build 3: Gaming 1440p LGA1700/DDR5 ATX — tất cả tương thích ✅
-- Build 4: DRAFT demo lỗi — 3 lỗi tương thích cố ý để demo validation ❌
--   • CPU AM5 (R9 7950X) + Mainboard AM4 (B450M)  → Socket KHÔNG khớp
--   • GPU 357mm (RTX4090) + Case 300mm (Fractal)   → GPU quá dài
--   • PSU 650W (CV650)   vs system TDP 620W×1.2=744W → Không đủ công suất
SET IDENTITY_INSERT pc_builds ON;
INSERT INTO pc_builds (build_id, user_id, build_name, total_price, created_at, updated_at, status)
VALUES
-- Total = sum of build_items prices (recalculated)
-- Build 1: 14790000+42490000+12490000+3990000+4990000+4790000+2790000+2490000 = 88,820,000
(1, 4, N'Gaming PC Cao Cấp 2026',          88820000, DATEADD(day,-7,GETDATE()), DATEADD(day,-3,GETDATE()), 'SAVED'),
-- Build 2: 3790000+2290000+990000+2490000+2790000+1990000+5490000 = 19,830,000
(2, 5, N'Build PC Văn Phòng Tầm Trung',    19830000, DATEADD(day,-5,GETDATE()), DATEADD(day,-5,GETDATE()), 'SAVED'),
-- Build 3: 7490000+18990000+12490000+4990000+2990000+3490000+3990000+5490000 = 59,920,000
(3, 6, N'PC Gaming Tầm Trung 1440p',       59920000, DATEADD(day,-2,GETDATE()), DATEADD(day,-1,GETDATE()), 'SAVED'),
-- Build 4: 17490000+2290000+990000+44990000+1990000+1490000 = 69,240,000
(4, 4, N'Build Demo - Kiểm Tra Tương Thích',69240000, DATEADD(day,-1,GETDATE()), DATEADD(day,-1,GETDATE()), 'DRAFT');
SET IDENTITY_INSERT pc_builds OFF;

SET IDENTITY_INSERT pc_build_items ON;
INSERT INTO pc_build_items (build_item_id, build_id, component_type, variant_id, quantity, price)
VALUES
-- ──────────────────────────────────────────────────────────────────────────────
-- BUILD 1: Gaming Cao Cấp — i9-14900K + RTX4090 + Z790-E ATX + DDR5 32G
--   Tất cả PASS:
--   ✅ CPU LGA1700 ↔ MB LGA1700   ✅ CPU DDR5 ↔ MB DDR5 ↔ RAM DDR5
--   ✅ RAM 6000MHz ≤ MB MaxRAM 7200MHz
--   ✅ GPU 340mm ≤ Case 400mm     ✅ Cooler 165mm ≤ Case 185mm
--   ✅ MB ATX ↔ Case ATX          ✅ PSU 1000W ≥ (125+450)×1.2=690W
-- ──────────────────────────────────────────────────────────────────────────────
(1,  1, 'CPU',      1,  1, 14790000),  -- i9-14900K Box     | LGA1700 | DDR5 | 125W TDP
(2,  1, 'GPU',      5,  1, 42490000),  -- RTX 4090 MSI      | 340mm   | 450W TDP
(3,  1, 'MAINBOARD',8,  1, 12490000),  -- Z790-E ATX        | LGA1700 | DDR5 | ATX | MaxRAM 7200MHz
(4,  1, 'RAM',      12, 1,  3990000),  -- Corsair DDR5 32GB | DDR5    | 6000MHz
(5,  1, 'SSD',      17, 1,  4990000),  -- Samsung 990 PRO 2TB
(6,  1, 'PSU',      18, 1,  4790000),  -- Corsair RM1000x   | 1000W
(7,  1, 'CASE',     19, 1,  2790000),  -- NZXT H7 Flow Black| ATX | MaxGPU 400mm | MaxCooler 185mm
(8,  1, 'COOLING',  22, 1,  2490000),  -- Noctua NH-D15     | 165mm

-- ──────────────────────────────────────────────────────────────────────────────
-- BUILD 2: Văn Phòng Tầm Trung — R5 5600X + B450M AM4 + DDR4 + Fractal mATX
--   Tất cả PASS:
--   ✅ CPU AM4 ↔ MB AM4           ✅ CPU DDR4 ↔ MB DDR4 ↔ RAM DDR4
--   ✅ RAM 3200MHz ≤ MB MaxRAM 4400MHz
--   ✅ Cooler 50mm (AIO pump) ≤ Case 160mm
--   ✅ MB mATX ↔ Case mATX        ✅ PSU 750W ≥ (65+0)×1.2=78W
--   ⚠️  Không có GPU rời → PSU thừa nhưng không lỗi
-- ──────────────────────────────────────────────────────────────────────────────
(9,  2, 'CPU',      25, 1,  3790000),  -- Ryzen 5 5600X     | AM4 | DDR4 | 65W TDP
(10, 2, 'MAINBOARD',10, 1,  2290000),  -- ASUS TUF B450M    | AM4 | DDR4 | mATX | MaxRAM 4400MHz
(11, 2, 'RAM',      14, 1,   990000),  -- Kingston DDR4 16GB| DDR4 | 3200MHz
(12, 2, 'SSD',      53, 1,  2490000),  -- FireCuda 530 1TB
(13, 2, 'PSU',      27, 1,  2790000),  -- Corsair RM750x    | 750W
(14, 2, 'CASE',     21, 1,  1990000),  -- Fractal Pop Mini  | mATX | MaxGPU 300mm | MaxCooler 160mm
-- ✅ H150i AIO pump height 50mm ≤ Fractal max 160mm  (NH-D15 165mm sẽ FAIL — dùng AIO ở đây)
(15, 2, 'COOLING',  58, 1,  5490000),  -- Corsair H150i LCD Black | AIO 50mm pump

-- ──────────────────────────────────────────────────────────────────────────────
-- BUILD 3: Gaming 1440p — i5-13600K + RTX4070S + Z790-E ATX + DDR5 32G
--   Tất cả PASS:
--   ✅ CPU LGA1700 ↔ MB LGA1700   ✅ CPU DDR5 ↔ MB DDR5 ↔ RAM DDR5
--   ✅ RAM 6400MHz ≤ MB MaxRAM 7200MHz
--   ✅ GPU 267mm ≤ Case 420mm     ✅ Cooler 50mm ≤ Case 167mm
--   ✅ MB ATX ↔ Case ATX          ✅ PSU 850W ≥ (125+220)×1.2=414W
-- ──────────────────────────────────────────────────────────────────────────────
(16, 3, 'CPU',      44, 1,  7490000),  -- i5-13600K Box     | LGA1700 | DDR5 | 125W TDP
(17, 3, 'GPU',      46, 1, 18990000),  -- RTX 4070S ASUS    | 267mm   | 220W TDP
(18, 3, 'MAINBOARD', 8, 1, 12490000),  -- Z790-E ATX        | LGA1700 | DDR5 | ATX | MaxRAM 7200MHz
(19, 3, 'RAM',      51, 1,  4990000),  -- G.Skill DDR5 32GB | DDR5    | 6400MHz
(20, 3, 'SSD',      16, 1,  2990000),  -- Samsung 990 PRO 1TB
(21, 3, 'PSU',      55, 1,  3490000),  -- Corsair RM850x    | 850W
(22, 3, 'CASE',     56, 1,  3990000),  -- Lian Li O11 Black | ATX | MaxGPU 420mm | MaxCooler 167mm
(23, 3, 'COOLING',  58, 1,  5490000),  -- Corsair H150i LCD Black | AIO 50mm pump

-- ──────────────────────────────────────────────────────────────────────────────
-- BUILD 4: DRAFT — Demo Kiểm Tra Tương Thích (3 lỗi cố ý)
--   ❌ CPU AM5 (R9 7950X) + Mainboard AM4 (B450M) → Socket KHÔNG khớp
--   ❌ GPU 357mm (RTX4090 ASUS) + Case 300mm (Fractal Pop Mini) → GPU quá dài
--   ❌ PSU 650W (CV650) vs TDP (170+450)×1.2=744W → Không đủ công suất
--   (Cũng: CPU R9 7950X hỗ trợ DDR5 nhưng B450M là DDR4 → CPU/MB DDR type FAIL)
-- ──────────────────────────────────────────────────────────────────────────────
(24, 4, 'CPU',       3, 1, 17490000),  -- R9 7950X   | AM5 | DDR5 | 170W TDP  ← AM5 ≠ AM4 ❌
(25, 4, 'MAINBOARD',10, 1,  2290000),  -- ASUS B450M | AM4 | DDR4 | mATX      ← socket sai ❌
(26, 4, 'RAM',      14, 1,   990000),  -- Kingston DDR4 16GB | DDR4 ← khớp B450M nhưng CPU socket sai
(27, 4, 'GPU',       4, 1, 44990000),  -- RTX 4090 ASUS | 357mm | 450W TDP    ← 357 > 300 ❌
(28, 4, 'CASE',     21, 1,  1990000),  -- Fractal Pop Mini | MaxGPU 300mm      ← GPU không vừa ❌
(29, 4, 'PSU',      26, 1,  1490000);  -- CV650 650W ← cần 744W, chỉ có 650W ❌
SET IDENTITY_INSERT pc_build_items OFF;

GO
PRINT '========================================';
PRINT 'Database initialized successfully!';
PRINT '----------------------------------------';
PRINT 'All passwords: Admin@123';
PRINT '  admin@computershop.com   -> ADMIN';
PRINT '  staff1@computershop.com  -> STAFF';
PRINT '  staff2@computershop.com  -> STAFF';
PRINT '  member1@example.com      -> MEMBER';
PRINT '  member2@example.com      -> MEMBER';
PRINT '  member3@example.com      -> MEMBER';
PRINT '----------------------------------------';
PRINT 'Data summary:';
PRINT '  Categories : 28 (5 parent + subcategories)';
PRINT '  Brands     : 24';
PRINT '  Products   : 43 (CPU, GPU, MB, RAM, SSD, PSU, Case, Tan nhiet,';
PRINT '                    Laptop, Monitor, Keyboard, Headset, Mouse)';
PRINT '  Variants   : 66';
PRINT '  Items (SN) : 183 (3 per variant, all 66 variants covered)';
PRINT '  Orders     : 4 (DELIVERED, PROCESSING/installment, PENDING, CANCELLED)';
PRINT '  PC Builds  : 4 (cao cap✅  van phong✅  1440p✅  demo-loi-tuong-thich❌)';
PRINT '----------------------------------------';
PRINT 'Build PC Compatibility Demo:';
PRINT '  Build 1 - Gaming Cao Cap    : ALL PASS (i9-14900K LGA1700/DDR5 + Z790-E + DDR5 32G)';
PRINT '  Build 2 - Van Phong         : ALL PASS (R5 5600X AM4/DDR4 + B450M + DDR4 16G)';
PRINT '  Build 3 - Gaming 1440p      : ALL PASS (i5-13600K LGA1700/DDR5 + Z790-E ATX + DDR5)';
PRINT '  Build 4 - Demo Tuong Thich  : 3 ERRORS (DRAFT)';
PRINT '    Socket mismatch : R9 7950X (AM5) + B450M (AM4)';
PRINT '    GPU too long    : RTX4090 357mm > Fractal Pop Mini 300mm';
PRINT '    PSU insufficient: CV650 650W < system TDP (170+450)x1.2=744W';
PRINT '========================================';
GO

-- ===============================
-- TEST DATA: REPORT - DOANH THU
-- ===============================
-- Muc dich: Test 3 loai bao cao:
--   1. Doanh thu theo thoi gian (groupBy DAY/MONTH/YEAR)
--   2. Doanh thu theo san pham (top products)
--   3. Doanh thu tra gop (PAID / UNPAID / OVERDUE)
--
-- Fix existing data: order_payment_schedule dung 'PENDING' khong dung enum
-- (Fixed in INSERT statements above)

-- ── Orders 5-13: COMPLETED, trai deu Jan / Feb / Mar 2026 ─────────────────
-- item_id da su dung: 1, 10, 82, 46, 88
-- Jan 2026 (day -79, -69, -59): order 5, 6, 7
-- Feb 2026 (day -48, -38, -28): order 8, 9, 10
-- Mar 2026 (day -20, -10, -5) : order 11, 12, 13
SET IDENTITY_INSERT orders ON;
INSERT INTO orders (order_id, user_id, total_amount, status, payment_method, payment_mode, installment_package_id, order_date)
VALUES
-- Thang 1
(5,  4, 44990000, 'COMPLETED', 'VNPAY',       'FULL',        NULL, DATEADD(day,-79,GETDATE())),  -- RTX 4090 ASUS
(6,  5, 14790000, 'COMPLETED', 'VNPAY',       'FULL',        NULL, DATEADD(day,-69,GETDATE())),  -- i9-14900K Box
(7,  6, 18990000, 'COMPLETED', 'COD',         'FULL',         NULL, DATEADD(day,-59,GETDATE())),  -- Acer Nitro V15
-- Thang 2
(8,  4, 49990000, 'COMPLETED', 'VNPAY',       'FULL',        NULL, DATEADD(day,-48,GETDATE())),  -- MacBook Pro M3 18GB
(9,  5, 45990000, 'COMPLETED', 'VNPAY',       'FULL',        NULL, DATEADD(day,-38,GETDATE())),  -- Dell XPS 15 OLED
(10, 6,  4990000, 'COMPLETED', 'COD',         'FULL',         NULL, DATEADD(day,-28,GETDATE())),  -- Samsung 990 PRO 2TB
-- Thang 3
(11, 4, 42990000, 'COMPLETED', 'VNPAY',       'FULL',        NULL, DATEADD(day,-20,GETDATE())),  -- ROG Strix G16 4070
(12, 5, 17490000, 'COMPLETED', 'VNPAY',       'FULL',        NULL, DATEADD(day,-10,GETDATE())),  -- Ryzen 9 7950X
(13, 6,  3790000, 'COMPLETED', 'COD',         'FULL',         NULL, DATEADD(day, -5,GETDATE())),  -- Ryzen 5 5600X
-- Tra gop: Order 14 — dang tra (4 PAID, 1 OVERDUE, 7 UNPAID)
-- MacBook Pro 14 M3 36GB, goi 12 thang, dat 4 thang truoc
(14, 5, 64990000, 'CONFIRMED', 'VNPAY', 'INSTALLMENT', 3,    DATEADD(month,-4,GETDATE())),
-- Tra gop: Order 15 — da tat toan (6/6 PAID)
-- ROG Strix G16 RTX4060, goi 6 thang, dat 7 thang truoc
(15, 6, 34990000, 'COMPLETED',      'VNPAY', 'INSTALLMENT', 2,    DATEADD(month,-7,GETDATE()));
SET IDENTITY_INSERT orders OFF;

-- ── Order Items cho orders 5-15 ────────────────────────────────────────────
SET IDENTITY_INSERT order_items ON;
INSERT INTO order_items (order_item_id, order_id, item_id, quantity, unit_price, recipient_name, recipient_phone, shipping_address)
VALUES
-- Order 5: RTX 4090 ASUS (variant 4, item 11)
(6,  5,  11, 1, 44990000, N'Nguyen Van A', '0903456789', N'123 Le Loi, Quan 1, TP.HCM'),
-- Order 6: i9-14900K Box (variant 1, item 2)
(7,  6,   2, 1, 14790000, N'Tran Thi B',   '0904567890', N'456 Nguyen Hue, Quan 1, TP.HCM'),
-- Order 7: Acer Nitro V15 (variant 60, item 163)
(8,  7, 163, 1, 18990000, N'Le Van C',     '0905678901', N'789 Hai Ba Trung, Quan 3, TP.HCM'),
-- Order 8: MacBook Pro 14 M3 18GB (variant 28, item 83)
(9,  8,  83, 1, 49990000, N'Nguyen Van A', '0903456789', N'123 Le Loi, Quan 1, TP.HCM'),
-- Order 9: Dell XPS 15 OLED (variant 61, item 166)
(10, 9, 166, 1, 45990000, N'Tran Thi B',   '0904567890', N'456 Nguyen Hue, Quan 1, TP.HCM'),
-- Order 10: Samsung 990 PRO 2TB (variant 17, item 47)
(11,10,  47, 1,  4990000, N'Le Van C',     '0905678901', N'789 Hai Ba Trung, Quan 3, TP.HCM'),
-- Order 11: ROG Strix G16 RTX4070 (variant 30, item 89)
(12,11,  89, 1, 42990000, N'Nguyen Van A', '0903456789', N'123 Le Loi, Quan 1, TP.HCM'),
-- Order 12: Ryzen 9 7950X (variant 3, item 7)
(13,12,   7, 1, 17490000, N'Tran Thi B',   '0904567890', N'456 Nguyen Hue, Quan 1, TP.HCM'),
-- Order 13: Ryzen 5 5600X (variant 25, item 73)
(14,13,  73, 1,  3790000, N'Le Van C',     '0905678901', N'789 Hai Ba Trung, Quan 3, TP.HCM'),
-- Order 14: MacBook Pro 14 M3 36GB (variant 29, item 85) - INSTALLMENT
(15,14,  85, 1, 64990000, N'Tran Thi B',   '0904567890', N'456 Nguyen Hue, Quan 1, TP.HCM'),
-- Order 15: ROG Strix G16 RTX4060 (variant 31, item 91) - INSTALLMENT da tat toan
(16,15,  91, 1, 34990000, N'Le Van C',     '0905678901', N'789 Hai Ba Trung, Quan 3, TP.HCM');
SET IDENTITY_INSERT order_items OFF;

-- ── Payment Schedule: Order 14 (12 ky, 4 PAID / 1 OVERDUE / 7 UNPAID) ─────
-- Goi 3: 12 thang, lai 1.5%, tra truoc 20%
-- Tong: 64,990,000 | Ky 1 (tra truoc): 64,990,000 * 20% = 12,998,000
-- Con lai: 51,992,000 / 11 ky ~ 4,726,545/ky (ky 12 = 4,726,605 de can bang)
-- Dat hang thang -4 => ky 1 due thang -4, ky 5 due thang 0 (hom nay) -> OVERDUE
SET IDENTITY_INSERT order_payment_schedule ON;
INSERT INTO order_payment_schedule (payment_schedule_id, order_id, installment_no, amount, due_date, paid_date, vnp_transaction_no, status)
VALUES
-- 4 ky da thanh toan
(13, 14,  1, 12998000, DATEADD(month,-4,CAST(GETDATE() AS DATE)), DATEADD(month,-4,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD14-001', 'PAID'),
(14, 14,  2,  4726545, DATEADD(month,-3,CAST(GETDATE() AS DATE)), DATEADD(month,-3,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD14-002', 'PAID'),
(15, 14,  3,  4726545, DATEADD(month,-2,CAST(GETDATE() AS DATE)), DATEADD(month,-2,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD14-003', 'PAID'),
(16, 14,  4,  4726545, DATEADD(month,-1,CAST(GETDATE() AS DATE)), DATEADD(month,-1,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD14-004', 'PAID'),
-- 1 ky qua han (den han hom nay, chua thanh toan)
(17, 14,  5,  4726545, CAST(GETDATE() AS DATE),                   NULL, NULL, 'OVERDUE'),
-- 7 ky chua den han
(18, 14,  6,  4726545, DATEADD(month, 1,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),
(19, 14,  7,  4726545, DATEADD(month, 2,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),
(20, 14,  8,  4726545, DATEADD(month, 3,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),
(21, 14,  9,  4726545, DATEADD(month, 4,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),
(22, 14, 10,  4726545, DATEADD(month, 5,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),
(23, 14, 11,  4726545, DATEADD(month, 6,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),
(24, 14, 12,  4726605, DATEADD(month, 7,CAST(GETDATE() AS DATE)), NULL, NULL, 'UNPAID'),

-- ── Payment Schedule: Order 15 (6 ky, tat ca PAID) ─────────────────────────
-- Goi 2: 6 thang, lai 1%, tra truoc 10%
-- Tong: 34,990,000 | Ky 1 (tra truoc): 34,990,000 * 10% = 3,499,000
-- Con lai: 31,491,000 / 5 ky = 6,298,200/ky
-- Dat hang thang -7 => ky 6 due thang -2 => tat ca da qua va da PAID
(25, 15,  1,  3499000, DATEADD(month,-7,CAST(GETDATE() AS DATE)), DATEADD(month,-7,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD15-001', 'PAID'),
(26, 15,  2,  6298200, DATEADD(month,-6,CAST(GETDATE() AS DATE)), DATEADD(month,-6,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD15-002', 'PAID'),
(27, 15,  3,  6298200, DATEADD(month,-5,CAST(GETDATE() AS DATE)), DATEADD(month,-5,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD15-003', 'PAID'),
(28, 15,  4,  6298200, DATEADD(month,-4,CAST(GETDATE() AS DATE)), DATEADD(month,-4,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD15-004', 'PAID'),
(29, 15,  5,  6298200, DATEADD(month,-3,CAST(GETDATE() AS DATE)), DATEADD(month,-3,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD15-005', 'PAID'),
(30, 15,  6,  6298200, DATEADD(month,-2,CAST(GETDATE() AS DATE)), DATEADD(month,-2,CAST(GETDATE() AS DATE)), 'VNP-TXN-ORD15-006', 'PAID');
SET IDENTITY_INSERT order_payment_schedule OFF;

PRINT '========================================';
PRINT 'REPORT TEST DATA INSERTED OK';
PRINT '  Orders 5-13  : COMPLETED (Jan/Feb/Mar 2026)';
PRINT '  Order  14    : INSTALLMENT - 4 PAID, 1 OVERDUE, 7 UNPAID';
PRINT '  Order  15    : INSTALLMENT - 6/6 PAID (tat toan)';
PRINT '========================================';
GO
