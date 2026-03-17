import request from '@/utils/request'

export function fetchShopGoods (params) {
  return request({
    url: '/shop/goods',
    method: 'get',
    params
  })
}

export function fetchShopGoodsDetail (id) {
  return request({
    url: `/shop/goods/${id}`,
    method: 'get'
  })
}

// 商品历史成交评论
export function fetchGoodsComments (id, size = 10) {
  return request({
    url: `/shop/goods/${id}/comments`,
    method: 'get',
    params: { size }
  })
}

// 卖家店铺信息（基础信息+信誉+在售商品）
export function fetchSellerShopInfo (sellerId, goodsSize = 6) {
  return request({
    url: `/shop/sellers/${sellerId}/shopInfo`,
    method: 'get',
    params: { goodsSize }
  })
}

