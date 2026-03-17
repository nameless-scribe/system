import request from '@/utils/request'

export function fetchAdminBuyRequests (params) {
  return request({
    url: '/admin/buyRequests',
    method: 'get',
    params
  })
}

export function deleteAdminBuyRequest (id) {
  return request({
    url: `/admin/buyRequests/${id}`,
    method: 'delete'
  })
}

