package com.system.rbac.mapper;

import com.system.rbac.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {
    List<Permission> selectAll();

    Permission selectById(@Param("id") Long id);

    Permission selectByCode(@Param("code") String code);

    int insert(Permission permission);

    int update(Permission permission);

    int deleteById(@Param("id") Long id);

    /**
     * 查询用户拥有的权限码（通过 user->role->permission）。
     */
    List<String> selectCodesByUserId(@Param("userId") Long userId);
}

