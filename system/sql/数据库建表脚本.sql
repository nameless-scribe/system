-- MySQL 8.x compatible init schema (idempotent). Aligns with current MyBatis mappers.
-- Run order: core users → domain tables → relations → auxiliary.

-- 1) Core user and profile
CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  status TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  nickname VARCHAR(64),
  avatar_url VARCHAR(255),
  gender TINYINT,
  bio VARCHAR(512),
  phone VARCHAR(32),
  email VARCHAR(128),
  address VARCHAR(255),
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_reputation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  total_score INT DEFAULT 0,
  total_count INT DEFAULT 0,
  good_count INT DEFAULT 0,
  score DECIMAL(5,2) DEFAULT 0.00,
  level INT DEFAULT 0,
  positive_rate DECIMAL(5,2) DEFAULT 0.00,
  last_calc_time DATETIME,
  CONSTRAINT fk_user_reputation_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) Brand and Goods
CREATE TABLE IF NOT EXISTS brand (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  logo_url VARCHAR(255),
  description VARCHAR(512)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  brand_id BIGINT NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 0,            -- 0下架 1上架
  audit_status TINYINT NOT NULL DEFAULT 0,      -- 0待审 1通过 2退回
  audit_remark VARCHAR(255),
  image_url VARCHAR(255),
  description TEXT,
  owner_id BIGINT,
  condition_level TINYINT,
  view_count INT DEFAULT 0,
  delete_status TINYINT NOT NULL DEFAULT 0,
  add_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_goods_brand (brand_id),
  KEY idx_goods_owner (owner_id),
  KEY idx_goods_status (status),
  KEY idx_goods_audit (audit_status),
  CONSTRAINT fk_goods_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
  CONSTRAINT fk_goods_owner FOREIGN KEY (owner_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Cart
CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_user_goods (user_id, goods_id),
  KEY idx_cart_user (user_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_cart_goods FOREIGN KEY (goods_id) REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) Orders
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  status TINYINT NOT NULL,                      -- 订单状态
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ship_time DATETIME,
  complete_time DATETIME,
  return_info VARCHAR(255),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_orders_buyer (buyer_id),
  KEY idx_orders_seller (seller_id),
  KEY idx_orders_status (status),
  CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES user(id),
  CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  count INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  KEY idx_od_order (order_id),
  CONSTRAINT fk_od_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_od_goods FOREIGN KEY (goods_id) REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) Ratings
CREATE TABLE IF NOT EXISTS rating (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  from_user_id BIGINT NOT NULL,
  to_user_id BIGINT NOT NULL,
  score TINYINT NOT NULL,                       -- 建议1~5
  comment VARCHAR(500),
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_rating_to (to_user_id),
  KEY idx_rating_from (from_user_id),
  CONSTRAINT fk_rating_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_rating_from FOREIGN KEY (from_user_id) REFERENCES user(id),
  CONSTRAINT fk_rating_to FOREIGN KEY (to_user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) Favorites
CREATE TABLE IF NOT EXISTS user_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_fav_user_goods (user_id, goods_id),
  KEY idx_fav_user (user_id),
  CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_fav_goods FOREIGN KEY (goods_id) REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) Buy Requests
CREATE TABLE IF NOT EXISTS buy_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  price_min DECIMAL(10,2),
  price_max DECIMAL(10,2),
  contact VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,            -- 1发布 0下架
  like_count INT NOT NULL DEFAULT 0,
  comment_count INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_br_user (user_id),
  KEY idx_br_status (status),
  CONSTRAINT fk_br_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS buy_request_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_brc_request (request_id),
  CONSTRAINT fk_brc_request FOREIGN KEY (request_id) REFERENCES buy_request(id),
  CONSTRAINT fk_brc_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS buy_request_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_brl_user_req (request_id, user_id),
  KEY idx_brl_req (request_id),
  CONSTRAINT fk_brl_request FOREIGN KEY (request_id) REFERENCES buy_request(id),
  CONSTRAINT fk_brl_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) Reports
CREATE TABLE IF NOT EXISTS report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter_id BIGINT NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  reason VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_report_target (target_type, target_id),
  CONSTRAINT fk_report_user FOREIGN KEY (reporter_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) Announcements
CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10) Audit logs
CREATE TABLE IF NOT EXISTS sys_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_id BIGINT,
  action VARCHAR(100),
  resource_type VARCHAR(50),
  resource_id BIGINT,
  ip VARCHAR(64),
  user_agent VARCHAR(255),
  detail TEXT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11) Dicts
CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type_code VARCHAR(64) NOT NULL,
  value VARCHAR(128) NOT NULL,
  label VARCHAR(128) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  color VARCHAR(32),
  remark VARCHAR(255),
  KEY idx_dict_type (type_code),
  CONSTRAINT fk_dict_type FOREIGN KEY (type_code) REFERENCES sys_dict_type(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12) RBAC
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  remark VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT PRIMARY KEY,
  code VARCHAR(128) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  module VARCHAR(64),
  type VARCHAR(32),
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  remark VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  KEY idx_rp_role (role_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_ur_user (user_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Useful seed data (optional)
-- INSERT IGNORE INTO sys_role(id, code, name) VALUES (1,'ADMIN','管理员'),(2,'SUPER_ADMIN','超管'),(3,'USER','普通用户');
-- INSERT IGNORE INTO sys_permission(id, code, name) VALUES (1,'ADMIN:ACCESS','后台访问');

