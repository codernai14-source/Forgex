import http from '@/api/http'

export const labelTemplateApi = {
    page(params: any) {
        return http.post('/label/template/page', params)
    },

    detail(id: number) {
        return http.post('/label/template/detail', { id })
    },

    add(params: any) {
        return http.post('/label/template/add', params)
    },

    update(params: any) {
        return http.post('/label/template/update', params)
    },

    delete(id: number) {
        return http.post('/label/template/delete', { id })
    },

    batchDelete(ids: number[]) {
        return http.post('/label/template/batchDelete', { ids })
    },

    setDefault(id: number, templateType: string) {
        return http.post('/label/template/setDefault', { id, templateType })
    },

    getPlaceholders(tenantId?: number) {
        return http.post('/label/template/placeholders', { tenantId })
    }
}
