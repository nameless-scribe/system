import request from '@/utils/request'

export function fetchAdminOrders (params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

export function updateOrderStatus (id, status) {
  return request({
    url: `/admin/orders/${id}/status`,
    method: 'put',
    params: { status }
  })
}

