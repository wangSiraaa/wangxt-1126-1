import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/sys/user/login',
    method: 'post',
    data
  })
}

export function getUserList() {
  return request({
    url: '/sys/user/list',
    method: 'get'
  })
}

export function getUserListByRole(roleCode) {
  return request({
    url: '/sys/user/listByRole',
    method: 'get',
    params: { roleCode }
  })
}
