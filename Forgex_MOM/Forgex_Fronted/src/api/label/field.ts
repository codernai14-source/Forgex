import http from '@/api/http'

export const labelFieldApi = {
  page(params: any) {
    return http.post('/basic/label/field/page', params)
  },
  detail(id: number) {
    return http.post('/basic/label/field/detail', { id })
  },
  add(params: any) {
    return http.post('/basic/label/field/add', params)
  },
  update(params: any) {
    return http.post('/basic/label/field/update', params)
  },
  delete(id: number) {
    return http.post('/basic/label/field/delete', { id })
  },
  batchDelete(ids: number[]) {
    return http.post('/basic/label/field/batchDelete', { ids })
  },
  enable(id: number, isEnabled: boolean) {
    return http.post('/basic/label/field/enable', { id, isEnabled })
  },
  options() {
    return http.post('/basic/label/field/options', {})
  }
}
