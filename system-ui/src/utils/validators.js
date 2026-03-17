// 通用表单校验工具

/**
 * 校验用户名和密码是否填写
 */
export function validateLoginForm ({ username, password }) {
  if (!username || !password) {
    return '请输入用户名和密码'
  }
  return null
}

/**
 * 校验注册表单
 */
export function validateRegisterForm ({ username, password, confirm }) {
  if (!username || !password || !confirm) {
    return '请填写完整信息'
  }
  if (password !== confirm) {
    return '两次输入的密码不一致'
  }
  if (password.length < 6 || password.length > 32) {
    return '密码长度需在 6-32 位之间'
  }
  return null
}

/**
 * 校验修改密码表单
 */
export function validateChangePasswordForm ({ username, oldPassword, newPassword }) {
  if (!username || !oldPassword || !newPassword) {
    return '请填写用户名、旧密码和新密码'
  }
  if (newPassword.length < 6 || newPassword.length > 32) {
    return '新密码长度需在 6-32 位之间'
  }
  return null
}

