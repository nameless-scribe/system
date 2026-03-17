package com.system.rbac.service;

import com.system.rbac.entity.Permission;
import com.system.rbac.entity.Role;

import java.util.List;
import java.util.Set;

public interface RbacService {
    Set<String> getPermissionCodesByUserId(Long userId);

    boolean hasPermission(Long userId, String permissionCode);

    List<Role> listRoles();

    List<Permission> listPermissions();

    void createRole(Role role);

    void updateRole(Role role);

    void deleteRole(Long roleId);

    void createPermission(Permission permission);

    void updatePermission(Permission permission);

    void deletePermission(Long permissionId);

    void setUserRoles(Long userId, List<Long> roleIds);

    void setRolePermissions(Long roleId, List<Long> permissionIds);

    List<Long> getUserRoleIds(Long userId);

    List<Long> getRolePermissionIds(Long roleId);
}

