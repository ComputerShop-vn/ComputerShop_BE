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
GO USE ComputerShopDB;
GO -- ===============================
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
GO -- ===============================
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
    is_active BIT DEFAULT 1
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
    payment_type NVARCHAR(20),
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
GO -- ===============================
    -- SEED DATA
    -- ===============================
    -- Roles  (must match @PreAuthorize: ADMIN, STAFF, MEMBER)
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (role_id, name)
VALUES (1, 'ADMIN'),
    (2, 'STAFF'),
    (3, 'MEMBER');
SET IDENTITY_INSERT roles OFF;
-- Installment Packages
SET IDENTITY_INSERT installment_package ON;

INSERT INTO installment_package (
        package_id,
        name,
        duration_months,
        interest_rate,
        min_order_amount,
        down_payment_percentage,
        is_active
    )
VALUES (
        1,
        N'Trả góp 3 tháng - Lãi suất 0% (Trả trước 0%)',
        3,
        0.0,
        3000000.00,
        0.0,
        1
    ),
    (
        2,
        N'Trả góp 6 tháng - Lãi suất 1% (Trả trước 10%)',
        6,
        1.0,
        5000000.00,
        10.0,
        1
    ),
    (
        3,
        N'Trả góp 12 tháng - Lãi suất 1.5% (Trả trước 20%)',
        12,
        1.5,
        10000000.00,
        20.0,
        1
    ),
    (
        4,
        N'Trả góp 9 tháng - Không hoạt động',
        9,
        1.5,
        5000000.00,
        15.0,
        0
    );
SET IDENTITY_INSERT installment_package OFF;
-- Users  (password = Admin@123 for all)
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
        'admin',
        'admin@computershop.com',
        '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K',
        '0901234567',
        GETDATE(),
        'ACTIVE',
        1
    ),
    (
        2,
        'staff1',
        'staff1@computershop.com',
        '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K',
        '0902345678',
        GETDATE(),
        'ACTIVE',
        2
    ),
    (
        3,
        'member1',
        'member1@example.com',
        '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K',
        '0903456789',
        GETDATE(),
        'ACTIVE',
        3
    ),
    (
        4,
        'member2',
        'member2@example.com',
        '$2a$12$lU/EpH6mhkL4ERuMRO2cjeeT1CPHxqAyDHGwnDctIWzojS6k/oG/K',
        '0904567890',
        GETDATE(),
        'ACTIVE',
        3
    );
SET IDENTITY_INSERT users OFF;
-- Categories
SET IDENTITY_INSERT categories ON;
INSERT INTO categories (category_id, category_name, parent_category_id)
VALUES (1, 'CPU', NULL),
    (2, 'GPU', NULL),
    (3, 'Mainboard', NULL),
    (4, 'RAM', NULL),
    (5, 'SSD', NULL),
    (6, 'HDD', NULL),
    (7, 'PSU', NULL),
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
-- Brands
SET IDENTITY_INSERT brands ON;
INSERT INTO brands (brand_id, brand_name)
VALUES (1, 'Intel'),
    (2, 'AMD'),
    (3, 'NVIDIA'),
    (4, 'ASUS'),
    (5, 'MSI'),
    (6, 'Corsair'),
    (7, 'Kingston'),
    (8, 'Samsung'),
    (9, 'Logitech'),
    (10, 'NZXT'),
    (11, 'Noctua'),
    (12, 'Fractal Design');
SET IDENTITY_INSERT brands OFF;
-- Attributes
SET IDENTITY_INSERT attributes ON;
INSERT INTO attributes (attribute_id, attribute_name)
VALUES -- CPU / GPU general
    (1, 'Core Count'),
    (2, 'Thread Count'),
    (3, 'Base Clock'),
    (4, 'Boost Clock'),
    (5, 'TDP'),
    (6, 'Socket'),
    -- Memory
    (7, 'Memory Size'),
    (8, 'Memory Type'),
    (9, 'Memory Bus'),
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
    (19, 'Max GPU Length'),
    -- on CASE: max GPU length it can fit (mm)
    (20, 'Max Cooler Height'),
    -- on CASE: max CPU cooler height it can fit (mm)
    (21, 'GPU Length'),
    -- on GPU variant: physical length (mm)
    (22, 'Cooler Height');
-- on COOLING variant: physical height (mm)
SET IDENTITY_INSERT attributes OFF;
-- Products
SET IDENTITY_INSERT products ON;
INSERT INTO products (
        product_id,
        name,
        description,
        base_price,
        category_id,
        brand_id
    )
VALUES -- CPUs
    (
        1,
        'Intel Core i9-14900K',
        'Top-end Intel desktop CPU, 24 cores (8P+16E), LGA1700, DDR5.',
        559.99,
        13,
        1
    ),
    (
        2,
        'AMD Ryzen 9 7950X',
        'Flagship Ryzen CPU, 16 cores / 32 threads, AM5 socket, DDR5.',
        699.99,
        14,
        2
    ),
    -- GPUs
    (
        3,
        'NVIDIA RTX 4090',
        'Flagship Ada Lovelace GPU, 24 GB GDDR6X.',
        1599.99,
        15,
        3
    ),
    (
        4,
        'AMD Radeon RX 7900 XTX',
        'High-end RDNA3 GPU, 24 GB GDDR6.',
        999.99,
        16,
        2
    ),
    -- Mainboards                                                                                   cat=3 (Mainboard)
    (
        5,
        'ASUS ROG Strix Z790-E Gaming',
        'Premium Z790 ATX motherboard for LGA1700, DDR5, 4 DIMM slots.',
        499.99,
        3,
        4
    ),
    (
        6,
        'MSI MAG X670E TOMAHAWK WIFI',
        'ATX AM5 mainboard, DDR5-6000, 4 DIMM slots, PCIe 5.0.',
        349.99,
        3,
        5
    ),
    (
        7,
        'ASUS TUF Gaming B450M-PLUS',
        'mATX AM4 mainboard, DDR4-4400, 2 DIMM slots. (Test: AM4/DDR4)',
        89.99,
        3,
        4
    ),
    -- RAM                                                                                          cat=4
    (
        8,
        'Corsair Vengeance DDR5',
        'High-speed DDR5 memory kit.',
        89.99,
        4,
        6
    ),
    (
        9,
        'Kingston Fury Beast DDR4-3200',
        'Reliable DDR4 kit for AM4 platforms. (Test: DDR4 mismatch)',
        39.99,
        4,
        7
    ),
    -- SSD                                                                                          cat=5
    (
        10,
        'Samsung 990 PRO NVMe SSD',
        'PCIe 4.0 NVMe SSD, up to 7450 MB/s read.',
        119.99,
        5,
        8
    ),
    -- PSU                                                                                          cat=7
    (
        11,
        'Corsair RM1000x',
        '1000W 80+ Gold fully-modular ATX PSU.',
        189.99,
        7,
        6
    ),
    -- Case                                                                                         cat=8
    (
        12,
        'NZXT H7 Flow',
        'Mid-tower ATX case with mesh front. Max GPU 400mm, Max Cooler 185mm.',
        109.99,
        8,
        10
    ),
    (
        13,
        'Fractal Design Pop Mini Air',
        'mATX Mini Tower. Max GPU 300mm, Max Cooler 160mm.',
        79.99,
        8,
        12
    ),
    -- Cooling                                                                                      cat=9
    (
        14,
        'Noctua NH-D15',
        'Dual-tower air cooler, 165mm height, compatible AM4/AM5/LGA1700.',
        99.99,
        9,
        11
    ),
    -- Peripherals
    (
        15,
        'Logitech G Pro X Superlight 2',
        'Ultra-light wireless gaming mouse, 60g.',
        159.99,
        12,
        9
    );
SET IDENTITY_INSERT products OFF;
-- Product Variants
SET IDENTITY_INSERT product_variants ON;
INSERT INTO product_variants (
        variant_id,
        product_id,
        sku,
        price,
        stock_quantity,
        variant_name
    )
VALUES -- i9-14900K  (product 1, LGA1700, DDR5)
    (
        1,
        1,
        'CPU-I9-14900K-BOX',
        589.99,
        20,
        'i9-14900K Box (with cooler)'
    ),
    (
        2,
        1,
        'CPU-I9-14900K-TRAY',
        559.99,
        15,
        'i9-14900K Tray (no cooler)'
    ),
    -- Ryzen 9 7950X  (product 2, AM5, DDR5)
    (
        3,
        2,
        'CPU-R9-7950X-BOX',
        699.99,
        18,
        'Ryzen 9 7950X Box'
    ),
    -- RTX 4090  (product 3)
    (
        4,
        3,
        'GPU-RTX4090-ASUS',
        1799.99,
        8,
        'ASUS ROG STRIX RTX 4090 OC 24GB — 357mm'
    ),
    (
        5,
        3,
        'GPU-RTX4090-MSI',
        1699.99,
        6,
        'MSI GAMING TRIO RTX 4090 24GB — 340mm'
    ),
    (
        6,
        3,
        'GPU-RTX4090-FE',
        1599.99,
        5,
        'NVIDIA Founders Edition RTX 4090 — 336mm'
    ),
    -- RX 7900 XTX  (product 4)
    (
        7,
        4,
        'GPU-RX7900XTX-REF',
        999.99,
        12,
        'AMD Reference RX 7900 XTX 24GB — 287mm'
    ),
    -- Z790-E mainboard  (product 5, LGA1700, ATX, DDR5, 4 slots)
    (
        8,
        5,
        'MB-ROGZ790E-ATX',
        499.99,
        25,
        'ROG Strix Z790-E ATX LGA1700 DDR5'
    ),
    -- MSI X670E mainboard  (product 6, AM5, ATX, DDR5, 4 slots)
    (
        9,
        6,
        'MB-MAGX670E-ATX',
        349.99,
        20,
        'MSI MAG X670E TOMAHAWK WIFI AM5 DDR5'
    ),
    -- ASUS B450M mainboard  (product 7, AM4, mATX, DDR4, 2 slots — test mismatch)
    (
        10,
        7,
        'MB-TUFB450M-MATX',
        89.99,
        15,
        'ASUS TUF B450M-PLUS mATX AM4 DDR4'
    ),
    -- Corsair DDR5 RAM  (product 8)
    (
        11,
        8,
        'RAM-DDR5-16G-6000',
        89.99,
        50,
        'Corsair Vengeance DDR5 16GB 6000MHz'
    ),
    (
        12,
        8,
        'RAM-DDR5-32G-6000',
        159.99,
        40,
        'Corsair Vengeance DDR5 32GB 6000MHz'
    ),
    (
        13,
        8,
        'RAM-DDR5-64G-6000',
        299.99,
        20,
        'Corsair Vengeance DDR5 64GB 6000MHz'
    ),
    -- Kingston DDR4 RAM  (product 9 — for AM4 / DDR4 mismatch test)
    (
        14,
        9,
        'RAM-DDR4-16G-3200',
        39.99,
        60,
        'Kingston Fury Beast DDR4 16GB 3200MHz'
    ),
    (
        15,
        9,
        'RAM-DDR4-32G-3200',
        69.99,
        40,
        'Kingston Fury Beast DDR4 32GB 3200MHz'
    ),
    -- Samsung 990 PRO SSD  (product 10)
    (
        16,
        10,
        'SSD-990PRO-1TB',
        119.99,
        35,
        'Samsung 990 PRO 1TB NVMe PCIe 4.0'
    ),
    (
        17,
        10,
        'SSD-990PRO-2TB',
        199.99,
        30,
        'Samsung 990 PRO 2TB NVMe PCIe 4.0'
    ),
    -- Corsair RM1000x PSU  (product 11)
    (
        18,
        11,
        'PSU-RM1000X-2024',
        189.99,
        30,
        'Corsair RM1000x 1000W 80+ Gold'
    ),
    -- NZXT H7 Flow ATX Case  (product 12)
    (
        19,
        12,
        'CASE-H7FLOW-BLACK',
        109.99,
        40,
        'NZXT H7 Flow ATX Black'
    ),
    (
        20,
        12,
        'CASE-H7FLOW-WHITE',
        119.99,
        25,
        'NZXT H7 Flow ATX White'
    ),
    -- Fractal Pop Mini Air mATX Case  (product 13)
    (
        21,
        13,
        'CASE-POPMINI-BLACK',
        79.99,
        30,
        'Fractal Pop Mini Air mATX Black'
    ),
    -- Noctua NH-D15 Cooler  (product 14)
    (
        22,
        14,
        'COOL-NHD15',
        99.99,
        25,
        'Noctua NH-D15 Dual Tower Air Cooler — 165mm'
    ),
    -- Mouse  (product 15)
    (
        23,
        15,
        'MOUSE-GPXSL2-BLACK',
        159.99,
        50,
        'G Pro X Superlight 2 Black'
    ),
    (
        24,
        15,
        'MOUSE-GPXSL2-WHITE',
        159.99,
        45,
        'G Pro X Superlight 2 White'
    );
SET IDENTITY_INSERT product_variants OFF;
-- Product Variant Attributes
-- attr legend: 1=CoreCount 2=ThreadCount 3=BaseClock 4=BoostClock 5=TDP 6=Socket
--              7=MemorySize 8=MemoryType 9=MemoryBus 10=MemorySpeed
--              11=FormFactor 12=Interface 13=ReadSpeed 14=WriteSpeed
--              15=Wattage 16=Efficiency 17=MaxRAMSpeed 18=RAMSlots
--              19=MaxGPULength 20=MaxCoolerHeight 21=GPULength 22=CoolerHeight
SET IDENTITY_INSERT product_variant_attributes ON;
INSERT INTO product_variant_attributes (variant_attr_id, variant_id, attribute_id, value)
VALUES -- ─── CPU: i9-14900K Box (variant 1) — LGA1700, DDR5 ───
    (1, 1, 1, '24 (8P+16E)'),
    (2, 1, 2, '32'),
    (3, 1, 3, '3.2GHz'),
    (4, 1, 4, '6.0GHz'),
    (5, 1, 5, '125W'),
    (6, 1, 6, 'LGA1700'),
    (7, 1, 8, 'DDR5'),
    -- ─── CPU: i9-14900K Tray (variant 2) — LGA1700, DDR5 ───
    (8, 2, 1, '24 (8P+16E)'),
    (9, 2, 2, '32'),
    (10, 2, 3, '3.2GHz'),
    (11, 2, 4, '6.0GHz'),
    (12, 2, 5, '125W'),
    (13, 2, 6, 'LGA1700'),
    (14, 2, 8, 'DDR5'),
    -- ─── CPU: Ryzen 9 7950X (variant 3) — AM5, DDR5 ───
    (15, 3, 1, '16'),
    (16, 3, 2, '32'),
    (17, 3, 3, '4.5GHz'),
    (18, 3, 4, '5.7GHz'),
    (19, 3, 5, '170W'),
    (20, 3, 6, 'AM5'),
    (21, 3, 8, 'DDR5'),
    -- ─── GPU: ASUS RTX 4090 (variant 4) — 357mm, 450W TDP ───
    (22, 4, 7, '24GB'),
    (23, 4, 8, 'GDDR6X'),
    (24, 4, 9, '384-bit'),
    (25, 4, 5, '450W'),
    (26, 4, 21, '357mm'),
    -- ─── GPU: MSI RTX 4090 (variant 5) — 340mm, 450W TDP ───
    (27, 5, 7, '24GB'),
    (28, 5, 8, 'GDDR6X'),
    (29, 5, 9, '384-bit'),
    (30, 5, 5, '450W'),
    (31, 5, 21, '340mm'),
    -- ─── GPU: NVIDIA FE RTX 4090 (variant 6) — 336mm, 450W TDP ───
    (32, 6, 7, '24GB'),
    (33, 6, 8, 'GDDR6X'),
    (34, 6, 9, '384-bit'),
    (35, 6, 5, '450W'),
    (36, 6, 21, '336mm'),
    -- ─── GPU: RX 7900 XTX Ref (variant 7) — 287mm, 355W TDP ───
    (37, 7, 7, '24GB'),
    (38, 7, 8, 'GDDR6'),
    (39, 7, 9, '384-bit'),
    (40, 7, 5, '355W'),
    (41, 7, 21, '287mm'),
    -- ─── Mainboard: Z790-E ATX LGA1700 (variant 8) — 4 DIMM DDR5 ───
    (42, 8, 6, 'LGA1700'),
    (43, 8, 8, 'DDR5'),
    (44, 8, 11, 'ATX'),
    (45, 8, 17, '7200'),
    (46, 8, 18, '4'),
    -- ─── Mainboard: MSI X670E ATX AM5 (variant 9) — 4 DIMM DDR5 ───
    (47, 9, 6, 'AM5'),
    (48, 9, 8, 'DDR5'),
    (49, 9, 11, 'ATX'),
    (50, 9, 17, '6000'),
    (51, 9, 18, '4'),
    -- ─── Mainboard: ASUS B450M mATX AM4 (variant 10) — 2 DIMM DDR4 (test slot limit!) ───
    (52, 10, 6, 'AM4'),
    (53, 10, 8, 'DDR4'),
    (54, 10, 11, 'mATX'),
    (55, 10, 17, '4400'),
    (56, 10, 18, '2'),
    -- ─── RAM: Corsair DDR5 16GB 6000MHz (variant 11) ───
    (57, 11, 7, '16GB'),
    (58, 11, 8, 'DDR5'),
    (59, 11, 10, '6000MHz'),
    -- ─── RAM: Corsair DDR5 32GB 6000MHz (variant 12) ───
    (60, 12, 7, '32GB'),
    (61, 12, 8, 'DDR5'),
    (62, 12, 10, '6000MHz'),
    -- ─── RAM: Corsair DDR5 64GB 6000MHz (variant 13) ───
    (63, 13, 7, '64GB'),
    (64, 13, 8, 'DDR5'),
    (65, 13, 10, '6000MHz'),
    -- ─── RAM: Kingston DDR4 16GB 3200MHz (variant 14) — DDR4 mismatch test ───
    (66, 14, 7, '16GB'),
    (67, 14, 8, 'DDR4'),
    (68, 14, 10, '3200MHz'),
    -- ─── RAM: Kingston DDR4 32GB 3200MHz (variant 15) ───
    (69, 15, 7, '32GB'),
    (70, 15, 8, 'DDR4'),
    (71, 15, 10, '3200MHz'),
    -- ─── SSD: 990 PRO 1TB (variant 16) ───
    (72, 16, 7, '1TB'),
    (73, 16, 12, 'PCIe 4.0 NVMe M.2'),
    (74, 16, 13, '7450MB/s'),
    (75, 16, 14, '6900MB/s'),
    -- ─── SSD: 990 PRO 2TB (variant 17) ───
    (76, 17, 7, '2TB'),
    (77, 17, 12, 'PCIe 4.0 NVMe M.2'),
    (78, 17, 13, '7450MB/s'),
    (79, 17, 14, '6900MB/s'),
    -- ─── PSU: RM1000x 1000W (variant 18) ───
    (80, 18, 15, '1000W'),
    (81, 18, 16, '80+ Gold'),
    -- ─── Case: NZXT H7 Flow ATX Black (variant 19) ───
    (82, 19, 11, 'ATX'),
    (83, 19, 19, '400mm'),
    (84, 19, 20, '185mm'),
    -- ─── Case: NZXT H7 Flow ATX White (variant 20) ───
    (85, 20, 11, 'ATX'),
    (86, 20, 19, '400mm'),
    (87, 20, 20, '185mm'),
    -- ─── Case: Fractal Pop Mini Air mATX Black (variant 21) ───
    -- Max GPU 300mm: RTX4090 (357/340/336mm) won''t fit; RX7900XTX (287mm) fits ✓
    (88, 21, 11, 'mATX'),
    (89, 21, 19, '300mm'),
    (90, 21, 20, '160mm'),
    -- ─── Cooling: Noctua NH-D15 (variant 22) — 165mm height ───
    -- Fits H7 Flow (185mm); does NOT fit Fractal Pop Mini (160mm) ✓
    (91, 22, 22, '165mm');
SET IDENTITY_INSERT product_variant_attributes OFF;
-- Compatibility Rules
-- Drives getFilterHints(): tells the API what attributes to cross-check between component types.
SET IDENTITY_INSERT compatibility_rules ON;
INSERT INTO compatibility_rules (
        rule_id,
        component_type_1,
        component_type_2,
        attribute_1,
        attribute_2,
        rule_type,
        description
    )
VALUES -- R1: CPU ↔ Mainboard socket must match (AM5, LGA1700, AM4 …)
    (
        1,
        'CPU',
        'MAINBOARD',
        'Socket',
        'Socket',
        'MUST_MATCH',
        'CPU socket must match the mainboard socket (e.g. AM5 ↔ AM5)'
    ),
    -- R2: Mainboard DDR gen must match RAM DDR gen (DDR4 vs DDR5)
    (
        2,
        'MAINBOARD',
        'RAM',
        'Memory Type',
        'Memory Type',
        'MUST_MATCH',
        'Mainboard and RAM must use the same DDR generation (DDR4 or DDR5)'
    ),
    -- R3: RAM speed must not exceed mainboard max RAM speed
    --     MUST_SUPPORT: RAM.Memory Speed ≤ MAINBOARD.Max RAM Speed
    --     → filter hint: "Memory Speed lte <maxValue>" when selecting RAM
    (
        3,
        'RAM',
        'MAINBOARD',
        'Memory Speed',
        'Max RAM Speed',
        'MUST_SUPPORT',
        'RAM speed must not exceed the maximum RAM speed supported by the mainboard'
    ),
    -- R4: GPU physical length must fit inside the case
    --     MUST_FIT: GPU.GPU Length ≤ CASE.Max GPU Length
    (
        4,
        'GPU',
        'CASE',
        'GPU Length',
        'Max GPU Length',
        'MUST_FIT',
        'GPU length must not exceed the case maximum GPU clearance'
    ),
    -- R5: CPU cooler height must fit inside the case
    --     MUST_FIT: COOLING.Cooler Height ≤ CASE.Max Cooler Height
    (
        5,
        'COOLING',
        'CASE',
        'Cooler Height',
        'Max Cooler Height',
        'MUST_FIT',
        'CPU cooler height must not exceed the case maximum cooler clearance'
    ),
    -- R6: Mainboard form factor must match case (ATX in ATX case, mATX in mATX case)
    --     Note: simplified as MUST_MATCH; in reality ATX cases also accept smaller boards.
    (
        6,
        'MAINBOARD',
        'CASE',
        'Form Factor',
        'Form Factor',
        'MUST_MATCH',
        'Mainboard form factor must be supported by the case'
    ),
    -- R7: PSU MIN_WATTAGE — handled in code by summing TDP of selected components × 1.2
    --     component_type_2 = NULL, attribute_2 = NULL per RuleType definition
    (
        7,
        'PSU',
        NULL,
        'Wattage',
        NULL,
        'MIN_WATTAGE',
        'PSU wattage must cover total system TDP plus 20% headroom'
    ),
    -- R8: CPU DDR generation must match Mainboard DDR generation
    --     CPU stores "Memory Type" = "DDR5" / "DDR4" (which DDR gen the CPU supports)
    --     MAINBOARD stores "Memory Type" = "DDR5" / "DDR4"
    --     → filter hint: "Memory Type eq DDR5" when selecting MAINBOARD with CPU selected
    (
        8,
        'CPU',
        'MAINBOARD',
        'Memory Type',
        'Memory Type',
        'MUST_MATCH',
        'CPU supported memory type must match mainboard memory type (DDR4 vs DDR5)'
    );
SET IDENTITY_INSERT compatibility_rules OFF;
-- Product Images
SET IDENTITY_INSERT product_images ON;
INSERT INTO product_images (image_id, product_id, image_url, is_thumbnail)
VALUES (
        1,
        1,
        'https://placehold.co/600x400?text=i9-14900K',
        1
    ),
    (
        2,
        2,
        'https://placehold.co/600x400?text=R9-7950X',
        1
    ),
    (
        3,
        3,
        'https://placehold.co/600x400?text=RTX-4090',
        1
    ),
    (
        4,
        4,
        'https://placehold.co/600x400?text=RX-7900-XTX',
        1
    ),
    (
        5,
        5,
        'https://placehold.co/600x400?text=Z790-E',
        1
    ),
    (
        6,
        6,
        'https://placehold.co/600x400?text=MSI-X670E',
        1
    ),
    (
        7,
        7,
        'https://placehold.co/600x400?text=ASUS-B450M',
        1
    ),
    (
        8,
        8,
        'https://placehold.co/600x400?text=DDR5-RAM',
        1
    ),
    (
        9,
        9,
        'https://placehold.co/600x400?text=DDR4-RAM',
        1
    ),
    (
        10,
        10,
        'https://placehold.co/600x400?text=990-PRO-SSD',
        1
    ),
    (
        11,
        11,
        'https://placehold.co/600x400?text=RM1000x-PSU',
        1
    ),
    (
        12,
        12,
        'https://placehold.co/600x400?text=NZXT-H7-Flow',
        1
    ),
    (
        13,
        13,
        'https://placehold.co/600x400?text=Fractal-Pop-Mini',
        1
    ),
    (
        14,
        14,
        'https://placehold.co/600x400?text=Noctua-NH-D15',
        1
    ),
    (
        15,
        15,
        'https://placehold.co/600x400?text=GPX-SL2-Mouse',
        1
    );
SET IDENTITY_INSERT product_images OFF;
-- Blogs
SET IDENTITY_INSERT blogs ON;
INSERT INTO blogs (blog_id, user_id, title, content, published_at)
VALUES (
        1,
        1,
        'Top PC Builds for 2026',
        'A roundup of the best value builds using 2026 hardware.',
        GETDATE()
    ),
    (
        2,
        2,
        'DDR5 vs DDR4: Is the Upgrade Worth?',
        'Detailed comparison of DDR5 and DDR4 performance in 2026.',
        GETDATE()
    ),
    (
        3,
        3,
        'How to Pick the Right GPU in 2026',
        'Tips for choosing a GPU based on resolution and budget.',
        GETDATE()
    );
SET IDENTITY_INSERT blogs OFF;
-- Promotions
SET IDENTITY_INSERT promotions ON;
INSERT INTO promotions (
        promotion_id,
        promo_code,
        discount_percent,
        start_date,
        end_date
    )
VALUES (1, 'SALE2026', 15, '2026-01-01', '2026-12-31'),
    (2, 'NEWMEMBER10', 10, '2026-01-01', '2026-12-31'),
    (3, 'SUMMER25', 25, '2026-06-01', '2026-08-31');
SET IDENTITY_INSERT promotions OFF;
-- Promotion Products (product IDs updated to match new catalog)
SET IDENTITY_INSERT promotion_product ON;
INSERT INTO promotion_product (promo_prod_id, promotion_id, product_id)
VALUES (1, 1, 3),
    -- RTX 4090
    (2, 1, 4),
    -- RX 7900 XTX
    (3, 2, 8),
    -- Corsair DDR5 RAM
    (4, 2, 10),
    -- Samsung 990 PRO SSD
    (5, 3, 1),
    -- i9-14900K
    (6, 3, 2);
-- Ryzen 9 7950X
SET IDENTITY_INSERT promotion_product OFF;
-- Reviews
SET IDENTITY_INSERT reviews ON;
INSERT INTO reviews (
        review_id,
        user_id,
        product_id,
        rating,
        comment,
        created_at
    )
VALUES (
        1,
        3,
        1,
        5,
        'Blazing fast CPU, handles everything at max settings.',
        GETDATE()
    ),
    (
        2,
        3,
        3,
        5,
        'Overkill for gaming but the performance is incredible.',
        GETDATE()
    ),
    (
        3,
        4,
        10,
        4,
        'Very fast SSD, great value for the 2TB capacity.',
        GETDATE()
    ),
    (
        4,
        4,
        15,
        5,
        'Best wireless mouse ever. Zero noticeable latency.',
        GETDATE()
    ),
    (
        5,
        3,
        8,
        4,
        'Solid DDR5 kit, easy XMP/EXPO setup in BIOS.',
        GETDATE()
    );
SET IDENTITY_INSERT reviews OFF;
-- ============================================================
-- Additional products for Build PC boundary testing
-- ============================================================
SET IDENTITY_INSERT products ON;
INSERT INTO products (
        product_id,
        name,
        description,
        base_price,
        category_id,
        brand_id
    )
VALUES (
        16,
        N'AMD Ryzen 5 5600X',
        N'Mid-range AM4 CPU, 6C/12T, 65W TDP, DDR4. Dùng để test build AM4/DDR4/mATX với B450M.',
        149.99,
        14,
        2
    ),
    (
        17,
        N'Corsair CV650',
        N'650W 80+ Bronze PSU. Dùng để test MIN_WATTAGE FAIL (650W < 700W khi có i9+RTX4090).',
        59.99,
        7,
        6
    ),
    (
        18,
        N'Corsair RM750x',
        N'750W 80+ Gold PSU. Dùng để test MIN_WATTAGE PASS boundary (750W >= 700W).',
        109.99,
        7,
        6
    );
SET IDENTITY_INSERT products OFF;
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
        25,
        16,
        N'CPU-R5-5600X-BOX',
        149.99,
        25,
        N'Ryzen 5 5600X Box (with cooler)'
    ),
    (
        26,
        17,
        N'PSU-CV650-650W',
        59.99,
        30,
        N'Corsair CV650 650W 80+ Bronze'
    ),
    (
        27,
        18,
        N'PSU-RM750X-750W',
        109.99,
        30,
        N'Corsair RM750x 750W 80+ Gold'
    );
SET IDENTITY_INSERT product_variants OFF;
SET IDENTITY_INSERT product_variant_attributes ON;
INSERT INTO product_variant_attributes (variant_attr_id, variant_id, attribute_id, value)
VALUES -- Ryzen 5 5600X (variant 25): AM4, DDR4, 65W TDP
    (92, 25, 1, N'6'),
    (93, 25, 2, N'12'),
    (94, 25, 3, N'3.7GHz'),
    (95, 25, 4, N'4.6GHz'),
    (96, 25, 5, N'65W'),
    (97, 25, 6, N'AM4'),
    (98, 25, 8, N'DDR4'),
    -- Corsair CV650 (variant 26): 650W
    (99, 26, 15, N'650W'),
    (100, 26, 16, N'80+ Bronze'),
    -- Corsair RM750x (variant 27): 750W
    (101, 27, 15, N'750W'),
    (102, 27, 16, N'80+ Gold');
SET IDENTITY_INSERT product_variant_attributes OFF;
SET IDENTITY_INSERT product_images ON;
INSERT INTO product_images (image_id, product_id, image_url, is_thumbnail)
VALUES (
        16,
        16,
        N'https://placehold.co/600x400?text=Ryzen5-5600X',
        1
    ),
    (
        17,
        17,
        N'https://placehold.co/600x400?text=CV650-PSU',
        1
    ),
    (
        18,
        18,
        N'https://placehold.co/600x400?text=RM750x-PSU',
        1
    );
SET IDENTITY_INSERT product_images OFF;
GO PRINT '========================================';
PRINT 'Database initialized successfully!';
PRINT 'All passwords: Admin@123';
PRINT '  admin@computershop.com  -> ADMIN';
PRINT '  staff1@computershop.com -> STAFF';
PRINT '  member1@example.com     -> MEMBER';
PRINT '  member2@example.com     -> MEMBER';
PRINT '========================================';
GO