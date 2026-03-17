import request from '@/utils/request'

export function fetchCart () {
  return request({
    url: '/user/cart',
    method: 'get'
  })
}

export function addToCart (data) {
  return request({
    url: '/user/cart',
    method: 'post',
    data
  })
}

export function updateCartItem (id, data) {
  return request({
    url: `/user/cart/${id}`,
    method: 'put',
    data
  })
}

export function removeCartItem (id) {
  return request({
    url: `/user/cart/${id}`,
    method: 'delete'
  })
}

export function clearCart () {
  return request({
    url: '/user/cart/clear',
    method: 'delete'
  })
}

