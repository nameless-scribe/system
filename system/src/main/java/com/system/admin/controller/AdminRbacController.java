package com.system.admin.controller;

import com.system.common.Result;
import com.system.rbac.entity.Permission;
import com.system.rbac.entity.Role;
import com.system.rbac.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：权限管理（RBAC）
 * - 角色管理
 * - 权限点管理
 * - 用户分配角色
 * - 角色分配权限
 */
@RestController
@RequestMapping("/api/admin/rbac")
public class AdminRbacController {

    @Autowired
    private RbacService rbacService;

    // -------- 角色 --------
    @GetMapping("/roles")
    public Result<List<Role>> listRoles() {
        return Result.success(rbacService.listRoles());
    }

    @PostMapping("/roles")
    public Result<Void> createRole(@RequestBody Role role) {
        rbacService.createRole(role);
        return Result.success();
    }

    @PutMapping("/roles/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        rbacService.updateRole(role);
        return Result.success();
    }

    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        rbacService.deleteRole(id);
        return Result.success();
    }

    // -------- 权限点 --------
    @GetMapping("/permissions")
    public Result<List<Permission>> listPermissions() {
        return Result.success(rbacService.listPermissions());
    }

    @PostMapping("/permissions")
    public Result<Void> createPermission(@RequestBody Permission permission) {
        rbacService.createPermission(permission);
        return Result.success();
    }

    @PutMapping("/permissions/{id}")
    public Result<Void> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        rbacService.updatePermission(permission);
        return Result.success();
    }

    @DeleteMapping("/permissions/{id}")
    public Result<Void> deletePermission(@PathVariable Long id) {
        rbacService.deletePermission(id);
        return Result.success();
    }

    // -------- 分配：用户-角色 --------
    @PutMapping("/users/{userId}/roles")
    public Result<Void> setUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        rbacService.setUserRoles(userId, roleIds);
        return Result.success();
    }

    @GetMapping("/users/{userId}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        return Result.success(rbacService.getUserRoleIds(userId));
    }

    // -------- 分配：角色-权限 --------
    @PutMapping("/roles/{roleId}/permissions")
    public Result<Void> setRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        rbacService.setRolePermissions(roleId, permissionIds);
        return Result.success();
    }

    @GetMapping("/roles/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(rbacService.getRolePermissionIds(roleId));
    }
}

