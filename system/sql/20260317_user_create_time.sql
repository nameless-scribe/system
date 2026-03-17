-- 为“卖家来到平台累计时间”提供基础字段
-- MySQL 8.x

-- 注意：部分 MySQL 8.x 小版本不支持 ADD COLUMN IF NOT EXISTS，这里用 information_schema 做兼容判断

set @db := database();
set @col_exists := (
    select count(1)
    from information_schema.columns
    where table_schema = @db
      and table_name = 'user'
      and column_name = 'create_time'
);

set @sql := if(
    @col_exists = 0,
    "alter table `user` add column `create_time` datetime not null default current_timestamp comment '注册/创建时间';",
    "select 'skip: column create_time already exists' as msg;"
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- 历史数据回填（如果 create_time 为空）
update `user`
set create_time = now()
where create_time is null;

