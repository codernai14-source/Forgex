import http from '../http'

export function getLoginCaptcha() {
  return http.get('/sys/config/login-captcha')
}

export function putLoginCaptcha(body: any) {
  return http.put('/sys/config/login-captcha', body)
}

