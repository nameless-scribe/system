import request from '@/utils/request'

// 前台/通用：根据字典类型编码获取字典数据列表
export function fetchDictData (typeCode) {
  return request({
    url: `/dict/${typeCode}`,
    method: 'get'
  })
}

// 管理端：字典类型列表
export function fetchDictTypes () {
  return request({
    url: '/admin/dict/types',
    method: 'get'
  })
}

export function createDictType (data) {
  return request({
    url: '/admin/dict/types',
    method: 'post',
    data
  })
}

export function updateDictType (id, data) {
  return request({
    url: `/admin/dict/types/${id}`,
    method: 'put',
    data
  })
}

export function deleteDictType (id) {
  return request({
    url: `/admin/dict/types/${id}`,
    method: 'delete'
  })
}

// 管理端：字典数据管理
export function fetchAllDictData () {
  return request({
    url: '/admin/dict/data',
    method: 'get'
  })
}

export function fetchDictDataByType (typeCode) {
  return request({
    url: `/admin/dict/data/${typeCode}`,
    method: 'get'
  })
}

export function createDictData (data) {
  return request({
    url: '/admin/dict/data',
    method: 'post',
    data
  })
}

export function updateDictData (id, data) {
  return request({
    url: `/admin/dict/data/${id}`,
    method: 'put',
    data
  })
}

export function deleteDictData (id) {
  return request({
    url: `/admin/dict/data/${id}`,
    method: 'delete'
  })
}

