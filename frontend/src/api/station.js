import request from '@/utils/request'

export function getStationPage(params) {
  return request({
    url: '/station/page',
    method: 'get',
    params
  })
}

export function getStationList(params) {
  return request({
    url: '/station/list',
    method: 'get',
    params
  })
}

export function getStationById(id) {
  return request({
    url: '/station/' + id,
    method: 'get'
  })
}

export function saveStation(data) {
  return request({
    url: '/station/save',
    method: 'post',
    data
  })
}

export function deleteStation(id) {
  return request({
    url: '/station/' + id,
    method: 'delete'
  })
}

export function getStationMetricHistory(id, params) {
  return request({
    url: `/station/${id}/metricHistory`,
    method: 'get',
    params
  })
}
