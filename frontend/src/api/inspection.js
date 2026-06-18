import request from '@/utils/request'

export function getRoutePage(params) {
  return request({
    url: '/inspection/route/page',
    method: 'get',
    params
  })
}

export function getRouteById(id) {
  return request({
    url: '/inspection/route/' + id,
    method: 'get'
  })
}

export function getRouteStations(id) {
  return request({
    url: `/inspection/route/${id}/stations`,
    method: 'get'
  })
}

export function createRoute(data) {
  return request({
    url: '/inspection/route/create',
    method: 'post',
    data
  })
}

export function assignRoute(data) {
  return request({
    url: '/inspection/route/assign',
    method: 'post',
    data
  })
}

export function startRoute(data) {
  return request({
    url: '/inspection/route/start',
    method: 'post',
    data: {
      routeId: data.routeId,
      inspectorId: data.userId || data.inspectorId
    }
  })
}

export function completeRoute(data) {
  return request({
    url: '/inspection/route/complete',
    method: 'post',
    data: {
      routeId: data.routeId
    }
  })
}

export function getMyRoutes(params) {
  return request({
    url: '/inspection/route/myRoutes',
    method: 'get',
    params
  })
}

export function getRouteDetail(id) {
  return request({
    url: `/inspection/route/${id}/detail`,
    method: 'get'
  })
}
