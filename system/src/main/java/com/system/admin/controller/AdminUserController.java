package com.system.admin.controller;

import com.system.common.Result;
import com.system.user.entity.User;
import com.system.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端用户管理：仅管理员可访问（由拦截器控制）
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.listAll());
    }

    @PostMapping
    public Result<Void> create(@RequestBody User user, HttpServletRequest request) {
        User currentAdmin = (User) request.getAttribute("currentUser");
        if (currentAdmin == null) {
            return Result.fail("未登录");
        }
        String operatorRole = currentAdmin.getRole() == null ? "" : currentAdmin.getRole();
        String targetRole = user.getRole() == null ? "USER" : user.getRole();

        // 只有 SUPER_ADMIN 才能创建管理员账号
        if (("ADMIN".equalsIgnoreCase(targetRole) || "SUPER_ADMIN".equalsIgnoreCase(targetRole))
                && !"SUPER_ADMIN".equalsIgnoreCase(operatorRole)) {
            return Result.fail("只有超级管理员可以创建管理员账号");
        }

        // 默认新建普通用户
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        userService.register(user);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody User user,
                               HttpServletRequest request) {
        User currentAdmin = (User) request.getAttribute("currentUser");
        if (currentAdmin == null) {
            return Result.fail("未登录");
        }
        User target = userService.getById(id);
        if (target == null) {
            return Result.fail("用户不存在");
        }
        String operatorRole = currentAdmin.getRole() == null ? "" : currentAdmin.getRole();
        String targetRole = target.getRole() == null ? "USER" : target.getRole();

        // 如果目标用户是管理员或超级管理员，则只有超级管理员可以编辑
        if (("ADMIN".equalsIgnoreCase(targetRole) || "SUPER_ADMIN".equalsIgnoreCase(targetRole))
                && !"SUPER_ADMIN".equalsIgnoreCase(operatorRole)) {
            return Result.fail("只有超级管理员可以编辑管理员用户");
        }

        user.setId(id);
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        User currentAdmin = (User) request.getAttribute("currentUser");
        if (currentAdmin == null) {
            return Result.fail("未登录");
        }
        User target = userService.getById(id);
        if (target == null) {
            return Result.fail("用户不存在");
        }
        String operatorRole = currentAdmin.getRole() == null ? "" : currentAdmin.getRole();
        String targetRole = target.getRole() == null ? "USER" : target.getRole();

        // 只有超级管理员可以删除管理员账号
        if (("ADMIN".equalsIgnoreCase(targetRole) || "SUPER_ADMIN".equalsIgnoreCase(targetRole))
                && !"SUPER_ADMIN".equalsIgnoreCase(operatorRole)) {
            return Result.fail("只有超级管理员可以删除管理员用户");
        }

        userService.delete(id);
        return Result.success();
    }

    /**
     * 封禁 / 解封 用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     HttpServletRequest request) {
        User currentAdmin = (User) request.getAttribute("currentUser");
        if (currentAdmin == null) {
            return Result.fail("未登录");
        }
        User target = userService.getById(id);
        if (target == null) {
            return Result.fail("用户不存在");
        }
        String operatorRole = currentAdmin.getRole() == null ? "" : currentAdmin.getRole();
        String targetRole = target.getRole() == null ? "USER" : target.getRole();

        // 只有超级管理员可以封禁/解封管理员
        if (("ADMIN".equalsIgnoreCase(targetRole) || "SUPER_ADMIN".equalsIgnoreCase(targetRole))
                && !"SUPER_ADMIN".equalsIgnoreCase(operatorRole)) {
            return Result.fail("只有超级管理员可以修改管理员用户状态");
        }

        userService.changeStatus(id, status);
        return Result.success();
    }
}

