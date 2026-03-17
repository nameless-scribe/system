import request from '@/utils/request'

// roles
export function fetchRoles () {
  return request({
    url: '/admin/rbac/roles',
    method: 'get'
  })
}

export function createRole (data) {
  return request({
    url: '/admin/rbac/roles',
    method: 'post',
    data
  })
}

export function updateRole (id, data) {
  return request({
    url: `/admin/rbac/roles/${id}`,
    method: 'put',
    data
  })
}

export function deleteRole (id) {
  return request({
    url: `/admin/rbac/roles/${id}`,
    method: 'delete'
  })
}

// permissions
export function fetchPermissions () {
  return request({
    url: '/admin/rbac/permissions',
    method: 'get'
  })
}

export function createPermission (data) {
  return request({
    url: '/admin/rbac/permissions',
    method: 'post',
    data
  })
}

export function updatePermission (id, data) {
  return request({
    url: `/admin/rbac/permissions/${id}`,
    method: 'put',
    data
  })
}

export function deletePermission (id) {
  return request({
    url: `/admin/rbac/permissions/${id}`,
    method: 'delete'
  })
}

// assign
export function setUserRoles (userId, roleIds) {
  return request({
    url: `/admin/rbac/users/${userId}/roles`,
    method: 'put',
    data: roleIds
  })
}

export function fetchUserRoleIds (userId) {
  return request({
    url: `/admin/rbac/users/${userId}/roles`,
    method: 'get'
  })
}

export function setRolePermissions (roleId, permissionIds) {
  return request({
    url: `/admin/rbac/roles/${roleId}/permissions`,
    method: 'put',
    data: permissionIds
  })
}

export function fetchRolePermissionIds (roleId) {
  return request({
    url: `/admin/rbac/roles/${roleId}/permissions`,
    method: 'get'
  })
}

