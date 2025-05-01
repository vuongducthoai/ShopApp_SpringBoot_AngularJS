CREATE DATABASE shopapp;
use shopapp;
-- Khach hang khi mua hang => phai dang ky tai khoan => bảng users 
CREATE TABLE users (
	id INT PRIMARY KEY AUTO_INCREMENT,
    fullname VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(10) NOT NULL,
    address VARCHAR(200) DEFAULT '',
    password VARCHAR(100) NOT NULL DEFAULT '', -- mat khau da ma hoa
    created_at DATETIME,
    updated_at DATETIME,
    is_active TINYINT(1) DEFAULT 1,
    date_of_birth DATE,
    facebook_account_id INT DEFAULT 0,
    google_account_id INT DEFAULT 0
);



CREATE TABLE roles(
	id INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

ALTER TABLE users ADD COLUMN role_id int;
ALTER TABLE users ADD FOREIGN KEY(role_id) REFERENCES roles(id);

CREATE TABLE tokens(
	id int PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_date DATETIME, -- ngay het han
    revoked TINYINT(1) NOT NULL,
    expired TINYINT(1) NOT NULL,
    user_id int,  -- Foreign key
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- hỗ trợ đăng nhập từ Facebook và Google
CREATE TABLE social_accounts(
	id INT PRIMARY KEY AUTO_INCREMENT,
    provider VARCHAR(20) NOT NULL COMMENT 'Tên nhà social network',
    provider_id VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL COMMENT 'Email tài khoản',
    name VARCHAR(100) NOT NULL COMMENT 'Tên người dùng',
	user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bang danh mục sản phẩm(Category)
CREATE TABLE categories(
	id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Tên danh muc, vd: đồ điện tử'
);

-- Bảng chứa sản phẩm (Product)
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL COMMENT 'Tên sản phẩm',
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0) COMMENT 'Giá sản phẩm',
    thumbnail VARCHAR(300) NOT NULL DEFAULT '' COMMENT 'Ảnh sản phẩm',
    description VARCHAR(300) DEFAULT '' COMMENT 'Mô tả sản phẩm',
    created_at DATETIME,
    updated_at DATETIME,
    category_id INT COMMENT 'Mã danh mục',
    FOREIGN KEY (category_id) REFERENCES categories(id) 
);


CREATE TABLE product_images(
	id INT PRIMARY KEY AUTO_INCREMENT,
	product_id INT,
    image_url VARCHAR(300),
    FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_product_images_id
		FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Đặt hàng - Orders
CREATE TABLE orders(
	id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    fullname VARCHAR(100) DEFAULT '',
    email VARCHAR(100) DEFAULT '',
	phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL, -- Dia chi noi gui
    note VARCHAR(100) DEFAULT '',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngay dat hang
    status VARCHAR(20),
    total_money FLOAT CHECK(total_money >= 0),
    shipping_method VARCHAR(100), -- phuong thuc van chuyen
    shipping_address VARCHAR(200), -- Dia chi giao hang
    shipping_date DATE, -- Ngay gui den
    tracking_number VARCHAR(100), -- So van don
    payment_method VARCHAR(100), -- Phuong thuc thanh toan
    active TINYINT(1)
);

ALTER TABLE orders
MODIFY COLUMN status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled')
COMMENT 'Trạng thái đơn hàng';


CREATE TABLE order_details(
	id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    product_id INT,
    FOREIGN KEY (product_id) REFERENCES products(id),
    price DECIMAL(10,2) CHECK(price >= 0),
    number_of_products INT CHECK(number_of_products > 0),
    total_money FLOAT CHECK (total_money >= 0),
    color VARCHAR(20) DEFAULT ''
);



CREATE TABLE `comments` (
  `id` int NOT NULL PRIMARY KEY ,
  `product_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;










