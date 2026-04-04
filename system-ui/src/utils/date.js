// 简单的日期时间格式化（本地时区）
// 支持 ISO 字符串（含时区，如 2026-04-04T12:41:59.000+00:00）、
// 时间戳（毫秒/秒）以及 Date 对象
export function formatDateTime (input, withSeconds = true) {
  if (!input && input !== 0) return ''
  let date
  if (input instanceof Date) {
    date = input
  } else if (typeof input === 'number') {
    // 识别秒级时间戳
    date = new Date(input < 1e12 ? input * 1000 : input)
  } else if (typeof input === 'string') {
    // Safari 兼容：把 2026-04-04T12:41:59.000+00:00 转为可解析格式
    // 统一用 Date 解析 ISO 字符串
    date = new Date(input)
  } else {
    return ''
  }
  if (Number.isNaN(date.getTime())) return String(input)

  const opts = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: withSeconds ? '2-digit' : undefined,
    hour12: false
  }
  // 使用本地时区、本地语言环境
  const s = new Intl.DateTimeFormat(undefined, opts).format(date)
  // 格式通常为 2026/04/04 20:41:59 或 2026/4/4 20:41:59，这里统一替换为连字符
  return s.replace(/\//g, '-')
}

