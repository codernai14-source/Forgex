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

    preview(id: number) {
        return http.post('/basic/label/template/preview', { id })
    },

    designDetail(id: number) {
        return http.post('/basic/label/template/design/detail', { id })
    },

    saveDesign(params: any) {
        return http.post('/basic/label/template/design/save', params)
    },

    render(params: any) {
        return http.post('/basic/label/print/render', params)
    }
}
