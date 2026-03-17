package com.system.config;

import com.system.user.entity.User;
import com.system.user.service.UserService;
import com.system.rbac.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 简单登录与角色拦截：
 * - 所有 /api/user/** /api/admin/** 需要登录
 * - /api/admin/** 需要 ADMIN 角色
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private RbacService rbacService;

    private static final String PERM_ADMIN_ACCESS = "ADMIN:ACCESS";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 登录接口、注册接口、前台商品浏览接口直接放行
        if (uri.startsWith("/api/auth")
                || uri.equals("/api/user/register")
                || uri.startsWith("/api/shop/")) {
            return true;
        }

        // 通过 Token 解析当前用户（无 session，彻底无状态）
        User currentUser = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = TokenUtil.parseToken(token);
            if (userId != null) {
                currentUser = userService.getById(userId);
                if (currentUser != null) {
                    // 将用户放到 request attribute，便于后续使用
                    request.setAttribute("currentUser", currentUser);
                }
            }
        }

        // 需要登录的前缀
        boolean needLogin = uri.startsWith("/api/user") || uri.startsWith("/api/admin");
        if (!needLogin) {
            return true;
        }

        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }

        // 已被封禁的用户禁止访问任何受保护接口
        if (currentUser.getStatus() != null && currentUser.getStatus() == 0) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"账号已被封禁，请联系管理员\"}");
            return false;
        }

        // 管理员接口校验角色：允许 ADMIN 与 SUPER_ADMIN 访问后台接口
        if (uri.startsWith("/api/admin")) {
            // 1) 优先走 RBAC 权限（如已初始化表数据）
            boolean allowedByRbac = false;
            try {
                if (rbacService != null && currentUser.getId() != null) {
                    allowedByRbac = rbacService.hasPermission(currentUser.getId(), PERM_ADMIN_ACCESS);
                }
            } catch (Exception ignored) {
                // RBAC 表未初始化/查询失败时，回退到旧 role 逻辑，保证兼容
                allowedByRbac = false;
            }

            // 2) 兼容旧 role 字段（保底）
            String role = currentUser.getRole() == null ? "" : currentUser.getRole();
            boolean allowedByLegacyRole = "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);

            if (!allowedByRbac && !allowedByLegacyRole) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"无权限访问管理员接口\"}");
                return false;
            }
        }

        return true;
    }
}

