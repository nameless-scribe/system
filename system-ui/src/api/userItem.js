import request from '@/utils/request'

export function fetchMyItems () {
  return request({
    url: '/user/items',
    method: 'get'
  })
}

export function createMyItem (data) {
  return request({
    url: '/user/items',
    method: 'post',
    data
  })
}

export function updateMyItem (id, data) {
  return request({
    url: `/user/items/${id}`,
    method: 'put',
    data
  })
}

export function offMyItem (id) {
  return request({
    url: `/user/items/${id}/off`,
    method: 'put'
  })
}

export function onMyItem (id) {
  return request({
    url: `/user/items/${id}/on`,
    method: 'put'
  })
}

export function deleteMyItem (id) {
  return request({
    url: `/user/items/${id}`,
    method: 'delete'
  })
}

