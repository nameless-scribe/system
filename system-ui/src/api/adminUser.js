import request from '@/utils/request'

export function fetchUsers () {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

export function createUser (data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

export function updateUser (id, data) {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser (id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

export function changeUserStatus (id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}
