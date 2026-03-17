import request from '@/utils/request'

export function submitReport (data) {
  return request({
    url: '/user/reports',
    method: 'post',
    data
  })
}

export function fetchReports () {
  return request({
    url: '/admin/reports',
    method: 'get'
  })
}

export function handleReport (id, data) {
  return request({
    url: `/admin/reports/${id}`,
    method: 'put',
    data
  })
}

