import request from '@/utils/request'

export function fetchPublishedAnnouncements () {
  return request({
    url: '/announcements',
    method: 'get'
  })
}

export function fetchAdminAnnouncements () {
  return request({
    url: '/admin/announcements',
    method: 'get'
  })
}

export function createAnnouncement (data) {
  return request({
    url: '/admin/announcements',
    method: 'post',
    data
  })
}

export function updateAnnouncement (id, data) {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'put',
    data
  })
}

export function deleteAnnouncement (id) {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'delete'
  })
}

