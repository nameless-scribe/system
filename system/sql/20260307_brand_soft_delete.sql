-- 品牌软删除改造
-- 执行前请先备份数据库

ALTER TABLE brand
    ADD COLUMN delete_status TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除' AFTER description;

CREATE INDEX idx_brand_delete_status ON brand(delete_status);
