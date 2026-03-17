// 各类状态展示（文案 + 颜色）统一工具

// 订单状态
export function orderStatusLabel (status, options = {}) {
  const opt = options[status]
  if (opt && opt.label) return opt.label
  switch (status) {
    case 0: return '待支付'
    case 1: return '待发货'
    case 2: return '已发货'
    case 3: return '已完成'
    case 4: return '已取消'
    case 5: return '已退回'
    default: return '未知'
  }
}

export function orderStatusType (status, options = {}) {
  const opt = options[status]
  if (opt && opt.color) return opt.color
  switch (status) {
    case 0: return 'warning'
    case 1: return 'info'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'danger'
    case 5: return 'warning'
    default: return 'info'
  }
}

// 用户状态（启用/封禁）
export function userStatusLabel (status, options = {}) {
  const opt = options[status]
  if (opt && opt.label) return opt.label
  return status === 1 ? '正常' : '已封禁'
}

export function userStatusType (status, options = {}) {
  const opt = options[status]
  if (opt && opt.color) return opt.color
  return status === 1 ? 'success' : 'info'
}

// 公告状态
export function announcementStatusLabel (status, options = {}) {
  const opt = options[status]
  if (opt && opt.label) return opt.label
  return status === 1 ? '已发布' : '草稿'
}

export function announcementStatusType (status, options = {}) {
  const opt = options[status]
  if (opt && opt.color) return opt.color
  return status === 1 ? 'success' : 'info'
}

// 举报状态
export function reportStatusLabel (status, options = {}) {
  const opt = options[status]
  if (opt && opt.label) return opt.label
  if (status === 'PENDING') return '待处理'
  if (status === 'RESOLVED') return '已处理'
  return status || '未知'
}

export function reportStatusType (status, options = {}) {
  const opt = options[status]
  if (opt && opt.color) return opt.color
  return status === 'PENDING' ? 'warning' : 'success'
}

