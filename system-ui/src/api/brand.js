import request from '@/utils/request'

export function fetchBrands () {
  return request({
    url: '/admin/brands',
    method: 'get'
  })
}

// 前台商品分类列表（普通用户使用）
export function fetchPublicBrands () {
  return request({
    url: '/shop/brands',
    method: 'get'
  })
}

export function createBrand (data) {
  return request({
    url: '/admin/brands',
    method: 'post',
    data
  })
}

export function updateBrand (id, data) {
  return request({
    url: `/admin/brands/${id}`,
    method: 'put',
    data
  })
}

export function deleteBrand (id) {
  return request({
    url: `/admin/brands/${id}`,
    method: 'delete'
  })
}

