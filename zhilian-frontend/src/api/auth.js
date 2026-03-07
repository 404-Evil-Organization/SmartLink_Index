import request from '@/utils/request'

// 登录接口
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 注册接口
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}