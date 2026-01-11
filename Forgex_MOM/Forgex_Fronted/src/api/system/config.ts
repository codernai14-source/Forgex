import http from '../http'

/**
 * 获取登录验证码配置
 * @returns 配置信息
 */
export function getLoginCaptcha() {
  return http.get('/sys/config/login-captcha')
}

/**
 * 更新登录验证码配置
 * @param body 配置数据
 * @returns 结果
 */
export function putLoginCaptcha(body: any) {
  return http.put('/sys/config/login-captcha', body)
}
