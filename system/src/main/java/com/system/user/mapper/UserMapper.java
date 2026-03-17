package com.system.user.mapper;

import com.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    int insert(User user);

    List<User> selectAll();

    int update(User user);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}

