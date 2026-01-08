import http from '../http'

export function listModules(body: any) {
  return http.post('/sys/module/list', body)
}

