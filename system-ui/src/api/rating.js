import request from '@/utils/request'

// 获取当前用户收到的评价列表
export function fetchReceivedRatings () {
  return request({
    url: '/user/ratings/received',
    method: 'get'
  })
}

// 查询“我对该订单”的评价（判断是否已评价/回显用于编辑）
export function getMyRatingByOrder (orderId) {
  return request({
    url: '/user/ratings/my',
    method: 'get',
    params: { orderId }
  })
}

// 提交评价
export function addRating (data) {
  return request({
    url: '/user/ratings',
    method: 'post',
    data
  })
}

// 更新我对该订单的评价
export function updateMyRating (data) {
  return request({
    url: '/user/ratings',
    method: 'put',
    data
  })
}

// 我发表过的评价列表
export function fetchMyRatings () {
  return request({
    url: '/user/ratings/mine',
    method: 'get'
  })
}

// 删除我发表的一条评价
export function deleteMyRating (id) {
  return request({
    url: `/user/ratings/${id}`,
    method: 'delete'
  })
}
