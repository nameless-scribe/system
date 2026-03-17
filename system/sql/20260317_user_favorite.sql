-- 用户收藏表：用于“我的收藏”
-- MySQL 8.x

create table if not exists user_favorite (
    id          bigint primary key auto_increment,
    user_id     bigint not null,
    goods_id    bigint not null,
    create_time datetime not null default current_timestamp,
    unique key uk_user_goods (user_id, goods_id),
    key idx_user_id (user_id),
    key idx_goods_id (goods_id)
) comment='用户收藏';

