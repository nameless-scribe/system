-- 商品软删除改造
-- 执行前请先备份数据库

ALTER TABLE goods
    ADD COLUMN delete_status TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除' AFTER view_count;

CREATE INDEX idx_goods_delete_status ON goods(delete_status);
