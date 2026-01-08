import http from '../http'

export function getInitStatus() {
  return http.get('/sys/init/status')
}

export function applyInit(body: any) {
  return http.post('/sys/init/apply', body)
}

