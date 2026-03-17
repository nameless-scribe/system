/**
 * 时间格式化工具（不依赖第三方库）
 */

function pad2 (n) {
  return String(n).padStart(2, '0')
}

/**
 * 兼容后端返回的 Date/时间戳/字符串，输出 yyyy-MM-dd HH:mm:ss
 */
export function formatDateTime (val) {
  if (!val) return ''

  let d = null
  if (val instanceof Date) {
    d = val
  } else if (typeof val === 'number') {
    d = new Date(val)
  } else if (typeof val === 'string') {
    // 兼容 "2026-03-17 12:00:00" / ISO
    const s = val.replace('T', ' ').replace('Z', '')
    const tryDate = new Date(s)
    d = isNaN(tryDate.getTime()) ? null : tryDate
  }

  if (!d || isNaN(d.getTime())) return String(val)

  const yyyy = d.getFullYear()
  const MM = pad2(d.getMonth() + 1)
  const dd = pad2(d.getDate())
  const HH = pad2(d.getHours())
  const mm = pad2(d.getMinutes())
  const ss = pad2(d.getSeconds())
  return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`
}

