// 角色与权限相关的通用工具

/**
 * 是否管理员（包含超级管理员）
 */
export function isAdminRole (role) {
  return role === 'ADMIN' || role === 'SUPER_ADMIN'
}

/**
 * 是否超级管理员
 */
export function isSuperAdmin (role) {
  return role === 'SUPER_ADMIN'
}

/**
 * 当前登录用户是否可以管理目标用户（编辑/封禁/删除）
 * 规则与后端一致：
 * - SUPER_ADMIN 不可操作自己，但可操作其他所有人
 * - ADMIN 只能操作普通用户 USER
 * - 其他角色不可操作 ADMIN / SUPER_ADMIN
 */
export function canManageUser (currentRole, currentUserId, targetUser) {
  if (!targetUser) return false
  const targetRole = targetUser.role
  const targetId = targetUser.id

  // 超级管理员不能操作自己（防误操作）
  if (isSuperAdmin(targetRole) && currentRole === 'SUPER_ADMIN' && currentUserId === targetId) {
    return false
  }

  if (currentRole === 'SUPER_ADMIN') {
    // 超级管理员可管理除自己以外所有人
    return currentUserId !== targetId
  }

  if (currentRole === 'ADMIN') {
    // 普通管理员只能管理普通用户
    return targetRole === 'USER'
  }

  // 其他角色无权管理
  return false
}

