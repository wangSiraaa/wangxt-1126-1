import request from '@/utils/request'

export function getRepairOrderPage(params) {
  return request({
    url: '/repair/order/page',
    method: 'get',
    params
  })
}

export function getRepairOrderById(id) {
  return request({
    url: '/repair/order/' + id,
    method: 'get'
  })
}

export function createRepairOrder(data) {
  return request({
    url: '/repair/order/create',
    method: 'post',
    data
  })
}

export function acceptRepairOrder(data) {
  return request({
    url: '/repair/order/accept',
    method: 'post',
    data
  })
}

export function arriveRepairSite(data) {
  return request({
    url: '/repair/order/arrive',
    method: 'post',
    data
  })
}

export function finishRepairOrder(data) {
  return request({
    url: '/repair/order/finish',
    method: 'post',
    data
  })
}

export function confirmRepairOrder(orderId, data = {}) {
  return request({
    url: '/repair/order/confirm',
    method: 'post',
    data: {
      orderId,
      confirmerId: data.userId || data.confirmerId,
      confirmerName: data.userName || data.confirmerName
    }
  })
}

export function closeRepairOrder(orderId, data = {}) {
  return request({
    url: '/repair/order/close',
    method: 'post',
    data: {
      orderId,
      closerId: data.userId || data.closerId,
      closerName: data.userName || data.closerName,
      closeRemark: data.remark || data.closeRemark
    }
  })
}

export function getMyRepairOrders(params) {
  return request({
    url: '/repair/order/myOrders',
    method: 'get',
    params
  })
}

export function dispatchRepairOrder(orderId, data = {}) {
  return request({
    url: '/repair/order/dispatch',
    method: 'post',
    data: {
      orderId,
      dispatcherId: data.dispatcherId,
      dispatcherName: data.dispatcherName,
      repairTeamId: data.repairTeamId,
      repairTeamName: data.repairTeamName
    }
  })
}

export function getRepairOrderDetail(id) {
  return getRepairOrderById(id)
}

export function acceptOrder(orderId, data = {}) {
  return request({
    url: '/repair/order/accept',
    method: 'post',
    data: {
      orderId,
      repairTeamId: data.repairTeamId,
      repairTeamName: data.repairTeamName,
      repairPerson: data.repairPerson,
      repairPhone: data.repairPhone
    }
  })
}

export function arriveSite(orderId, data = {}) {
  return request({
    url: '/repair/order/arrive',
    method: 'post',
    data: {
      orderId
    }
  })
}

export function finishRepair(orderId, data = {}) {
  return request({
    url: '/repair/order/finish',
    method: 'post',
    data: {
      orderId,
      pressureAfter: data.pressureAfter,
      supplyTempAfter: data.supplyTempAfter,
      returnTempAfter: data.returnTempAfter,
      repairContent: data.repairContent,
      repairMaterial: data.repairMaterial,
      workHours: data.workHours,
      valveOperation: data.valveOperation,
      tempHeatPlan: data.tempHeatPlan,
      estRestoreTime: data.estRestoreTime
    }
  })
}
