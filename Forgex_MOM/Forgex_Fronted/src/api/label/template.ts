import http from '@/api/http'

export const labelTemplateApi = {
    page(params: any) {
        return http.post('/basic/label/template/page', params)
    },

    detail(id: number) {
        return http.post('/basic/label/template/detail', { id })
    },

    add(params: any) {
        return http.post('/basic/label/template/add', params)
    },

    update(params: any) {
        return http.post('/basic/label/template/update', params)
    },

    delete(id: number) {
        return http.post('/basic/label/template/delete', { id })
    },

    batchDelete(ids: number[]) {
        return http.post('/basic/label/template/batchDelete', { ids })
    },

    setDefault(id: number, templateType: string) {
        return http.post('/basic/label/template/setDefault', { id, templateType })
    },

    getPlaceholders(tenantId?: number) {
        return http.post('/basic/label/template/placeholders', { tenantId })
    }
}
