import request from '@/utils/request'

export function fetchAuditLogs (params) {
  return request({
    url: '/admin/auditLogs',
    method: 'get',
    params
  })
}

export function fetchAuditLogDetail (id) {
  return request({
    url: `/admin/auditLogs/${id}`,
    method: 'get'
  })
}

