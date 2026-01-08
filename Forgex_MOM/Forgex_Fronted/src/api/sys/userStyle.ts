import http from '../http'

export function getUserLayoutStyle(body: any) {
  return http.post('/sys/user-style/get-layout', body)
}

export function saveUserLayoutStyle(body: any) {
  return http.post('/sys/user-style/save-layout', body)
}

