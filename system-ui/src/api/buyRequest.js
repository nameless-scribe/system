import request from '@/utils/request'

// 广场浏览
export function fetchBuyRequestSquare (params) {
  return request({
    url: '/shop/buyRequests',
    method: 'get',
    params
  })
}

export function fetchBuyRequestDetail (id) {
  return request({
    url: `/shop/buyRequests/${id}`,
    method: 'get'
  })
}

export function fetchBuyRequestComments (id) {
  return request({
    url: `/shop/buyRequests/${id}/comments`,
    method: 'get'
  })
}

// 用户动作
export function publishBuyRequest (data) {
  return request({
    url: '/user/buyRequests',
    method: 'post',
    data
  })
}

export function addBuyRequestComment (id, content) {
  return request({
    url: `/user/buyRequests/${id}/comments`,
    method: 'post',
    data: { content }
  })
}

export function likeBuyRequest (id) {
  return request({
    url: `/user/buyRequests/${id}/like`,
    method: 'post'
  })
}

export function unlikeBuyRequest (id) {
  return request({
    url: `/user/buyRequests/${id}/unlike`,
    method: 'post'
  })
}

export function buyRequestLiked (id) {
  return request({
    url: `/user/buyRequests/${id}/liked`,
    method: 'get'
  })
}

