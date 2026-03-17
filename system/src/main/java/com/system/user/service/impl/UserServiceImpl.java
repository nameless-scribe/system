package com.system.user.service.impl;

import com.system.user.entity.User;
import com.system.user.mapper.UserMapper;
import com.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("注册信息不能为空");
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3-20 个字符之间");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("密码长度需在 6-32 个字符之间");
        }
        // 用户名唯一性校验
        User exists = userMapper.selectByUsername(username);
        if (exists != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 密码使用 MD5 加密后再入库（不可逆摘要）
        user.setPassword(encodePassword(password));

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认正常
        }
        userMapper.insert(user);
    }

    @Override
    public User login(String username, String password) {
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser == null) {
            return null;
        }
        // 使用与注册时相同的 MD5 加密规则进行对比
        String encoded = encodePassword(password);
        if (!encoded.equals(dbUser.getPassword())) {
            return null;
        }
        // 封禁用户禁止登录：与用户名/密码错误区分开，便于前端精确提示
        if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被封禁，请联系管理员");
        }
        return dbUser;
    }

    /**
     * 使用 MD5 对明文密码做摘要
     */
    private String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectAll();
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 32) {
            throw new IllegalArgumentException("新密码长度需在 6-32 个字符之间");
        }

        User dbUser = userMapper.selectByUsername(username);
        if (dbUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 校验旧密码
        String oldEncoded = encodePassword(oldPassword);
        if (!oldEncoded.equals(dbUser.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }

        // 更新为新密码（MD5 摘要）
        String newEncoded = encodePassword(newPassword);
        dbUser.setPassword(newEncoded);
        userMapper.updatePassword(dbUser.getId(), dbUser.getPassword());
    }
}

