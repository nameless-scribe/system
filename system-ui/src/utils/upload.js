// 通用上传工具：前端文件大小校验 + 失败提示

import request from '@/utils/request'

/**
 * 创建一个 beforeUpload 校验函数
 * @param {Object} options
 * @param {number} options.maxSizeMB - 最大体积（MB）
 */
export function createBeforeUpload (options = {}) {
  const { maxSizeMB = 5 } = options
  return function beforeUpload (file) {
    const isLt = file.size / 1024 / 1024 < maxSizeMB
    if (!isLt && this && this.$message) {
      this.$message.error(`图片大小不能超过 ${maxSizeMB}MB`)
      return false
    }
    return true
  }
}

/**
 * 创建一个自定义上传方法（使用 axios，自动携带 token）
 * @param {string} url - 上传接口地址（相对于 /api）
 * @returns {Function} - el-upload 的 http-request 方法
 */
export function createHttpRequest (url) {
  return function customUpload (options) {
    const formData = new FormData()
    formData.append('file', options.file)
    request({
      url: url,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (options.onProgress && progressEvent.total > 0) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          options.onProgress({ percent })
        }
      }
    }).then(response => {
      if (response && response.code === 200) {
        options.onSuccess(response)
      } else {
        const msg = response && response.message ? response.message : '上传失败'
        options.onError(new Error(msg))
      }
    }).catch(err => {
      options.onError(err)
    })
  }
}

/**
 * 通用上传失败处理（适用于 el-upload 的 on-error）
 */
export function handleUploadError (vm, err, fallbackMsg) {
  if (!vm || !vm.$message) {
    // eslint-disable-next-line no-console
    console.error('upload error: ', err)
    return
  }
  const status = err && (err.status || (err.response && err.response.status))
  if (status === 413) {
    vm.$message.error('上传失败：图片过大，超过服务器限制，请压缩后再上传')
  } else {
    vm.$message.error(fallbackMsg || '上传失败，请稍后重试')
  }
  // 方便调试
  // eslint-disable-next-line no-console
  console.error('upload error: ', err)
}

