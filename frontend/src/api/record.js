import request from '@/utils/request'

export function getRecordPage(params) {
  return request({
    url: '/inspection/record/page',
    method: 'get',
    params
  })
}

export function getRecordById(id) {
  return request({
    url: '/inspection/record/' + id,
    method: 'get'
  })
}

export function submitRecord(data) {
  return request({
    url: '/inspection/record/submit',
    method: 'post',
    data
  })
}

export function submitInspectionRecord(data) {
  return submitRecord(data)
}

export function getRecordsByRoute(routeId) {
  return request({
    url: '/inspection/record/listByRoute',
    method: 'get',
    params: { routeId }
  })
}
