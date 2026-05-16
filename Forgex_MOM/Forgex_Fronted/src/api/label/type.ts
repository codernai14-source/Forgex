import http from '@/api/http'

export const labelTypeApi = {
  page(params: any) {
    return http.post('/basic/label/type/page', params)
  },
  detail(id: number) {
    return http.post('/basic/label/type/detail', { id })
  },
  add(params: any) {
    return http.post('/basic/label/type/add', params)
  },
  update(params: any) {
    return http.post('/basic/label/type/update', params)
  },
  delete(id: number) {
    return http.post('/basic/label/type/delete', { id })
  },
  enable(id: number, isEnabled: boolean) {
    return http.post('/basic/label/type/enable', { id, isEnabled })
  },
  options() {
    return http.post('/basic/label/type/options', {})
  }
}
