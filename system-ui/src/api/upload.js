import request from '@/utils/request'

export function uploadImage (formData) {
  return request({
    url: '/admin/upload/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

