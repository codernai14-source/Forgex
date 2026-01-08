import http from '../http'

export function listUsers(body: any) {
  return http.post('/sys/user/list', body)
}

export function createUser(body: any) {
  return http.post('/sys/user/create', body)
}

export function updateUser(body: any) {
  return http.post('/sys/user/update', body)
}

export function deleteUser(body: any) {
  return http.post('/sys/user/delete', body)
}

