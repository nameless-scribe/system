package com.system.rbac.mapper;

import com.system.rbac.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    List<UserRole> selectByUserId(@Param("userId") Long userId);

    int insert(UserRole userRole);

    int deleteByUserId(@Param("userId") Long userId);
}

