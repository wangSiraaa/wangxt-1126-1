import request from '@/utils/request'

export function getComplaintPage(params) {
  return request({
    url: '/cold/complaint/page',
    method: 'get',
    params
  })
}

export function getComplaintById(id) {
  return request({
    url: '/cold/complaint/' + id,
    method: 'get'
  })
}

export function createComplaint(data) {
  return request({
    url: '/cold/complaint/create',
    method: 'post',
    data
  })
}

export function processComplaint(data) {
  return request({
    url: '/cold/complaint/process',
    method: 'post',
    data
  })
}

export function dispatchRepairFromComplaint(data) {
  return request({
    url: '/cold/complaint/dispatchRepair',
    method: 'post',
    data
  })
}

export function dispatchComplaint(complaintId, data = {}) {
  return request({
    url: '/cold/complaint/dispatchRepair',
    method: 'post',
    data: {
      complaintId,
      dispatcherId: data.dispatcherId,
      dispatcherName: data.dispatcherName,
      repairTeamId: data.repairTeamId,
      repairTeamName: data.repairTeamName,
      faultType: data.faultType,
      faultLevel: data.faultLevel || 'WARNING',
      faultDesc: data.remark || data.faultDesc
    }
  })
}

export function finishComplaintRepair(data) {
  return request({
    url: '/cold/complaint/finishRepair',
    method: 'post',
    data
  })
}

export function visitComplaint(data) {
  return request({
    url: '/cold/complaint/visit',
    method: 'post',
    data
  })
}

export function closeComplaint(complaintId, data = {}) {
  return request({
    url: '/cold/complaint/close',
    method: 'post',
    data: {
      complaintId,
      closerId: data.userId || data.closerId,
      closerName: data.userName || data.closerName,
      closeRemark: data.remark || data.closeRemark
    }
  })
}

export function getComplaintDetail(id) {
  return getComplaintById(id)
}
