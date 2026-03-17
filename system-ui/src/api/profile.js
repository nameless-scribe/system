import request from '@/utils/request'

export function fetchProfile () {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

export function saveProfile (data) {
  return request({
    url: '/user/profile',
    method: 'post',
    data
  })
}

