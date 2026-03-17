package com.system.user.service;

import com.system.user.entity.User;

import java.util.List;

public interface UserService {

    User getById(Long id);

    User getByUsername(String username);

    void register(User user);

    /**
     * 登录校验：根据用户名和密码返回用户
     */
    User login(String username, String password);

    /**
     * 管理端：查询所有用户
     */
    List<User> listAll();

    /**
     * 管理端：更新用户（含角色）
     */
    void update(User user);

    /**
     * 管理端：删除用户
     */
    void delete(Long id);

    /**
     * 管理端：更新用户状态（封禁/解封）
     */
    void changeStatus(Long id, Integer status);

    /**
     * 未登录修改密码：根据用户名和旧密码校验后，设置新密码
     */
    void changePassword(String username, String oldPassword, String newPassword);
}

