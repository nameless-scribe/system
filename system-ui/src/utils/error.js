// 统一的错误提示工具

/**
 * 在组件内调用：handleError(this, error, '默认失败提示')
 */
export function handleError (vm, error, fallbackMsg) {
  if (!vm || !vm.$message) {
    // 非 Vue 实例环境，直接打印
    // eslint-disable-next-line no-console
    console.error(error)
    return
  }
  const msg = (error && error.message) || fallbackMsg || '操作失败'
  vm.$message.error(msg)
}

