import request from '@/utils/request'

export function fetchFavorites () {
  return request({
    url: '/user/favorites',
    method: 'get'
  })
}

export function addFavorite (goodsId) {
  return request({
    url: '/user/favorites',
    method: 'post',
    data: { goodsId }
  })
}

export function removeFavorite (goodsId) {
  return request({
    url: '/user/favorites',
    method: 'delete',
    params: { goodsId }
  })
}

export function favoriteExists (goodsId) {
  return request({
    url: '/user/favorites/exists',
    method: 'get',
    params: { goodsId }
  })
}

export function fetchFavoriteCount () {
  return request({
    url: '/user/favorites/count',
    method: 'get'
  })
}

export function removeFavoriteBatch (goodsIds) {
  return request({
    url: '/user/favorites/batch',
    method: 'delete',
    data: goodsIds
  })
}

export function cleanupInvalidFavorites () {
  return request({
    url: '/user/favorites/cleanupInvalid',
    method: 'post'
  })
}

