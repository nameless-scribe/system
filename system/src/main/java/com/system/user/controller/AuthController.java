package com.system.user.controller;

import com.system.common.Result;
import com.system.config.RateLimitUtil;
import com.system.config.TokenUtil;
import com.system.rbac.service.RbacService;
import com.system.user.entity.User;
import com.system.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private RbacService rbacService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestParam String username,
                                       @RequestParam String password,
                                       HttpServletRequest request) {
        String ip = request != null ? request.getRemoteAddr() : "unknown";
        // 简单限流：同一用户名+IP 1 分钟内最多 10 次尝试
        String limitKey = "login:" + username + ":" + ip;
        boolean allowed = RateLimitUtil.tryAcquire(limitKey, 10, 60_000L);
        if (!allowed) {
            log.warn("登录频率过高，已被限流，username={}, ip={}", username, ip);
            return Result.fail("操作过于频繁，请稍后再试");
        }

        User user;
        try {
            user = userService.login(username, password);
        } catch (IllegalArgumentException e) {
            // 用业务异常的 message 直接返回给前端，例如账号被封禁等
            log.warn("用户登录业务校验失败，username={}, ip={}, msg={}", username, ip, e.getMessage());
            return Result.fail(e.getMessage());
        }
        if (user == null) {
            // 用户不存在或密码不正确的统一提示
            log.warn("用户登录失败（用户名或密码错误），username={}, ip={}", username, ip);
            return Result.fail("用户名或密码错误");
        }

        // 生成 30 分钟有效的 token
        String token = TokenUtil.generateToken(user.getId());

        user.setPassword(null);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUser(user);
        return Result.success(resp);
    }

    /**
     * 未登录修改密码：根据用户名 + 旧密码校验后，设置新密码
     */
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestParam String username,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       HttpServletRequest request) {
        String ip = request != null ? request.getRemoteAddr() : "unknown";
        // 修改密码限制更严格：同一用户名+IP 5 分钟内最多 5 次尝试
        String limitKey = "changePwd:" + username + ":" + ip;
        boolean allowed = RateLimitUtil.tryAcquire(limitKey, 5, 5 * 60_000L);
        if (!allowed) {
            log.warn("修改密码频率过高，已被限流，username={}, ip={}", username, ip);
            return Result.fail("操作过于频繁，请稍后再试");
        }

        try {
            userService.changePassword(username, oldPassword, newPassword);
            log.info("用户修改密码成功，username={}, ip={}", username, ip);
            return Result.success();
        } catch (IllegalArgumentException e) {
            log.warn("用户修改密码失败，username={}, ip={}, msg={}", username, ip, e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // 无状态：前端删除 token 即为登出，这里仅返回成功
        return Result.success();
    }

    @GetMapping("/me")
    public Result<User> currentUser(HttpServletRequest request) {
        // 仅从 token 获取当前用户
        String authHeader = request.getHeader("Authorization");
        User user = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = TokenUtil.parseToken(token);
            if (userId != null) {
                user = userService.getById(userId);
            }
        }

        if (user == null) {
            return Result.fail("未登录");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 当前登录用户的权限码集合（用于前端做菜单/按钮级权限控制）
     */
    @GetMapping("/myPerms")
    public Result<Set<String>> myPerms(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        String token = authHeader.substring(7);
        Long userId = TokenUtil.parseToken(token);
        if (userId == null) {
            return Result.fail("未登录");
        }
        if (rbacService == null) {
            return Result.success(Set.of());
        }
        return Result.success(rbacService.getPermissionCodesByUserId(userId));
    }

    /**
     * 登录返回体：包含 token 与用户信息
     */
    public static class LoginResponse {
        private String token;
        private User user;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}

