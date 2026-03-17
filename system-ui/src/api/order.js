import request from '@/utils/request'

export function createOrder (data) {
  return request({
    url: '/user/orders',
    method: 'post',
    data
  })
}

export function fetchUserOrders (params) {
  return request({
    url: '/user/orders',
    method: 'get',
    params
  })
}

export function fetchOrderDetail (id) {
  return request({
    url: `/user/orders/${id}`,
    method: 'get'
  })
}

export function fetchSoldOrders (params) {
  return request({
    url: '/user/orders/sold',
    method: 'get',
    params
  })
}

export function shipOrder (id) {
  return request({
    url: `/user/orders/${id}/ship`,
    method: 'put'
  })
}

export function completeOrder (id) {
  return request({
    url: `/user/orders/${id}/complete`,
    method: 'put'
  })
}

export function returnOrder (id, data) {
  return request({
    url: `/user/orders/${id}/return`,
    method: 'put',
    data
  })
}

export function pickupOrder (id) {
  return request({
    url: `/user/orders/${id}/pickup`,
    method: 'put'
  })
}
