package com.system.rbac.service.impl;

import com.system.rbac.entity.Permission;
import com.system.rbac.entity.Role;
import com.system.rbac.entity.RolePermission;
import com.system.rbac.entity.UserRole;
import com.system.rbac.mapper.PermissionMapper;
import com.system.rbac.mapper.RoleMapper;
import com.system.rbac.mapper.RolePermissionMapper;
import com.system.rbac.mapper.UserRoleMapper;
import com.system.rbac.service.RbacService;
import com.system.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacServiceImpl implements RbacService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public Set<String> getPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        List<String> codes = permissionMapper.selectCodesByUserId(userId);
        return codes == null ? Set.of() : new HashSet<>(codes);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }
        return getPermissionCodesByUserId(userId).contains(permissionCode);
    }

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectAll();
    }

    @Override
    public List<Permission> listPermissions() {
        return permissionMapper.selectAll();
    }

    @Override
    public void createRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        if (role.getCode() == null || role.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        role.setId(SnowflakeIdGenerator.generateId());
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getSort() == null) {
            role.setSort(0);
        }
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(Role role) {
        if (role == null || role.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        roleMapper.update(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        // 先删除关联
        rolePermissionMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    @Override
    public void createPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("权限不能为空");
        }
        if (permission.getCode() == null || permission.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("权限编码不能为空");
        }
        if (permission.getName() == null || permission.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
        permission.setId(SnowflakeIdGenerator.generateId());
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        if (permission.getSort() == null) {
            permission.setSort(0);
        }
        permissionMapper.insert(permission);
    }

    @Override
    public void updatePermission(Permission permission) {
        if (permission == null || permission.getId() == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        permissionMapper.update(permission);
    }

    @Override
    public void deletePermission(Long permissionId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        permissionMapper.deleteById(permissionId);
    }

    @Override
    public void setUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        userRoleMapper.deleteByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            if (roleId == null) {
                continue;
            }
            UserRole ur = new UserRole();
            ur.setId(SnowflakeIdGenerator.generateId());
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public void setRolePermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long pid : permissionIds) {
            if (pid == null) {
                continue;
            }
            RolePermission rp = new RolePermission();
            rp.setId(SnowflakeIdGenerator.generateId());
            rp.setRoleId(roleId);
            rp.setPermissionId(pid);
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<UserRole> list = userRoleMapper.selectByUserId(userId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        List<RolePermission> list = rolePermissionMapper.selectByRoleId(roleId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
    }
}

