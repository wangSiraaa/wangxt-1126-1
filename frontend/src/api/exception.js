import request from '@/utils/request'

export function getExceptionPage(params) {
  return request({
    url: '/inspection/exception/page',
    method: 'get',
    params
  })
}

export function getExceptionById(id) {
  return request({
    url: '/inspection/exception/' + id,
    method: 'get'
  })
}

export function confirmException(exceptionId, data = {}) {
  return request({
    url: '/inspection/exception/confirm',
    method: 'post',
    data: {
      exceptionId,
      handlerId: data.userId || data.handlerId,
      handlerName: data.userName || data.handlerName
    }
  })
}

export function dispatchRepairFromException(data) {
  return request({
    url: '/inspection/exception/dispatchRepair',
    method: 'post',
    data
  })
}

export function dispatchException(exceptionId, data = {}) {
  return request({
    url: '/inspection/exception/dispatchRepair',
    method: 'post',
    data: {
      exceptionId,
      dispatcherId: data.dispatcherId,
      dispatcherName: data.dispatcherName,
      repairTeamId: data.repairTeamId,
      repairTeamName: data.repairTeamName
    }
  })
}

export function autoUpgradeException(data) {
  return request({
    url: '/inspection/exception/autoUpgrade',
    method: 'post',
    data
  })
}

export function closeException(exceptionId, data = {}) {
  return request({
    url: '/inspection/exception/close',
    method: 'post',
    data: {
      exceptionId,
      closerId: data.userId || data.closerId,
      closerName: data.userName || data.closerName,
      closeRemark: data.remark || data.closeRemark
    }
  })
}

export function getExceptionsByRoute(routeId) {
  return request({
    url: '/inspection/exception/listByRoute',
    method: 'get',
    params: { routeId }
  })
}

export function getMyExceptions(params) {
  return request({
    url: '/inspection/exception/myExceptions',
    method: 'get',
    params
  })
}

export function getOrderFlowLogs(orderType, orderId) {
  return request({
    url: '/order/flowLog/list',
    method: 'get',
    params: { orderType, orderId }
  })
}
