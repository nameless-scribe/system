-- RBAC + 系统日志（管理员操作审计）
-- 适用数据库：MySQL 8.x

-- 1) 权限表：权限点（资源/操作）定义
create table if not exists sys_permission (
    id          bigint primary key,
    code        varchar(100) not null unique comment '权限编码，例如 ADMIN:USER:WRITE',
    name        varchar(100) not null comment '权限名称',
    module      varchar(50)  null comment '模块，例如 USER/GOODS/ORDER',
    type        varchar(20)  null comment '类型，例如 MENU/BUTTON/API',
    status      tinyint      not null default 1 comment '1启用 0停用',
    sort        int          not null default 0,
    remark      varchar(255) null,
    create_time datetime     not null default current_timestamp,
    update_time datetime     not null default current_timestamp on update current_timestamp
) comment='权限表';

-- 2) 角色表
create table if not exists sys_role (
    id          bigint primary key,
    code        varchar(50)  not null unique comment '角色编码，例如 ADMIN',
    name        varchar(100) not null comment '角色名称',
    status      tinyint      not null default 1,
    sort        int          not null default 0,
    remark      varchar(255) null,
    create_time datetime     not null default current_timestamp,
    update_time datetime     not null default current_timestamp on update current_timestamp
) comment='角色表';

-- 3) 用户-角色 关联表（一个用户可多个角色）
create table if not exists sys_user_role (
    id          bigint primary key,
    user_id     bigint not null,
    role_id     bigint not null,
    create_time datetime not null default current_timestamp,
    unique key uk_user_role (user_id, role_id),
    key idx_user_id (user_id),
    key idx_role_id (role_id)
) comment='用户角色关联';

-- 4) 角色-权限 关联表
create table if not exists sys_role_permission (
    id            bigint primary key,
    role_id       bigint not null,
    permission_id bigint not null,
    create_time   datetime not null default current_timestamp,
    unique key uk_role_perm (role_id, permission_id),
    key idx_role_id (role_id),
    key idx_perm_id (permission_id)
) comment='角色权限关联';

-- 5) 管理员操作审计日志
create table if not exists sys_audit_log (
    id            bigint primary key,
    operator_id   bigint null comment '操作人用户ID（可能为空：未登录/解析失败）',
    operator_name varchar(100) null comment '操作人用户名快照',
    operator_role varchar(50)  null comment '操作人旧role字段快照',
    ip            varchar(64)  null,
    http_method   varchar(16)  null,
    uri           varchar(255) null,
    query_string  varchar(1024) null,
    request_body  text null comment '参数摘要（截断）',
    success       tinyint not null default 1 comment '1成功 0失败',
    result_code   int null comment '业务code（Result.code）或HTTP状态码',
    error_message varchar(500) null comment '失败原因（截断）',
    cost_ms       bigint null,
    create_time   datetime not null default current_timestamp,
    key idx_operator_id (operator_id),
    key idx_create_time (create_time),
    key idx_uri (uri)
) comment='系统日志-管理员操作审计';

-- =========================
-- 初始化数据（可按需改）
-- =========================

-- 角色：与旧 user.role 保持一致
insert ignore into sys_role(id, code, name, status, sort, remark)
values
  (1001, 'SUPER_ADMIN', '超级管理员', 1, 1, '内置角色'),
  (1002, 'ADMIN', '管理员', 1, 2, '内置角色'),
  (1003, 'USER', '普通用户', 1, 3, '内置角色');

-- 最小权限集合：先给一个“后台访问”权限，后续可继续细分
insert ignore into sys_permission(id, code, name, module, type, status, sort, remark)
values
  (2001, 'ADMIN:ACCESS', '后台访问权限', 'ADMIN', 'API', 1, 1, '访问 /api/admin/** 的基础权限'),
  (2101, 'ADMIN:USER:READ', '用户管理-查询', 'USER', 'API', 1, 10, null),
  (2102, 'ADMIN:USER:WRITE', '用户管理-新增/编辑/删除', 'USER', 'API', 1, 11, null),
  (2201, 'ADMIN:GOODS:READ', '商品管理-查询', 'GOODS', 'API', 1, 20, null),
  (2202, 'ADMIN:GOODS:WRITE', '商品管理-审核/上下架/编辑', 'GOODS', 'API', 1, 21, null),
  (2301, 'ADMIN:ORDER:READ', '订单管理-查询', 'ORDER', 'API', 1, 30, null),
  (2302, 'ADMIN:ORDER:WRITE', '订单管理-状态变更', 'ORDER', 'API', 1, 31, null),
  (2401, 'ADMIN:DICT:WRITE', '字典管理-维护', 'DICT', 'API', 1, 40, null),
  (2501, 'ADMIN:AUDIT:READ', '审计日志-查询', 'AUDIT', 'API', 1, 50, null),
  (2601, 'ADMIN:RBAC:WRITE', '权限管理-维护', 'RBAC', 'API', 1, 60, null);

-- 角色授权（默认：ADMIN 拥有大部分后台权限；SUPER_ADMIN 拥有全部；USER 无后台权限）
-- SUPER_ADMIN -> 全部权限
insert ignore into sys_role_permission(id, role_id, permission_id)
select (900000 + p.id) as id, 1001 as role_id, p.id as permission_id
from sys_permission p;

-- ADMIN -> 后台访问 + 常用管理权限（不含 RBAC 维护）
insert ignore into sys_role_permission(id, role_id, permission_id)
select (800000 + p.id) as id, 1002 as role_id, p.id as permission_id
from sys_permission p
where p.code in (
  'ADMIN:ACCESS',
  'ADMIN:USER:READ','ADMIN:USER:WRITE',
  'ADMIN:GOODS:READ','ADMIN:GOODS:WRITE',
  'ADMIN:ORDER:READ','ADMIN:ORDER:WRITE',
  'ADMIN:DICT:WRITE',
  'ADMIN:AUDIT:READ'
);

-- USER -> 不授予后台权限（可按需授予前台权限）

-- =========================
-- 从旧 user.role 迁移到 sys_user_role（幂等）
-- =========================
-- 注意：如果你的 user 表没有 role 字段或字段名不同，请自行调整
insert ignore into sys_user_role(id, user_id, role_id)
select
  (700000 + u.id) as id,
  u.id as user_id,
  case
    when upper(ifnull(u.role, 'USER')) = 'SUPER_ADMIN' then 1001
    when upper(ifnull(u.role, 'USER')) = 'ADMIN' then 1002
    else 1003
  end as role_id
from user u;

