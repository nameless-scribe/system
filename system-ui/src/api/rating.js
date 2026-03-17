import request from '@/utils/request'

// 获取当前用户收到的评价列表
export function fetchReceivedRatings () {
  return request({
    url: '/user/ratings/received',
    method: 'get'
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
