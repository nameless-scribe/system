-- 求购广场：求购信息 + 评论 + 点赞
-- MySQL 8.x

create table if not exists buy_request (
    id            bigint primary key auto_increment,
    user_id       bigint not null comment '发布人',
    title         varchar(120) not null,
    content       varchar(2000) not null,
    price_min     decimal(10,2) null,
    price_max     decimal(10,2) null,
    contact       varchar(200) null comment '联系方式（可选）',
    status        tinyint not null default 1 comment '1正常 0下架/隐藏',
    like_count    int not null default 0,
    comment_count int not null default 0,
    create_time   datetime not null default current_timestamp,
    update_time   datetime not null default current_timestamp on update current_timestamp,
    key idx_user_id (user_id),
    key idx_create_time (create_time),
    key idx_like_comment (like_count, comment_count)
) comment='求购信息';

create table if not exists buy_request_comment (
    id           bigint primary key auto_increment,
    request_id   bigint not null,
    user_id      bigint not null,
    content      varchar(800) not null,
    status       tinyint not null default 1 comment '1正常 0删除',
    create_time  datetime not null default current_timestamp,
    key idx_request_id (request_id),
    key idx_user_id (user_id),
    key idx_create_time (create_time)
) comment='求购评论';

create table if not exists buy_request_like (
    id           bigint primary key auto_increment,
    request_id   bigint not null,
    user_id      bigint not null,
    create_time  datetime not null default current_timestamp,
    unique key uk_req_user (request_id, user_id),
    key idx_request_id (request_id),
    key idx_user_id (user_id)
) comment='求购点赞';

