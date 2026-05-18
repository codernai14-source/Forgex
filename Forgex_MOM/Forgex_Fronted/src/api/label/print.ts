import http from '@/api/http'

export const labelPrintApi = {
    /**
     * 执行打印
     */
    execute(params: any) {
        return http.post('/basic/label/print/execute', params)
    },

    /**
     * 打印预览
     */
    preview(params: any) {
        return http.post('/basic/label/print/preview', params)
    },

    /**
     * 补打标签
     */
    reprint(params: any) {
        return http.post('/basic/label/record/reprint', params)
    },

    render(params: any) {
        return http.post('/basic/label/print/render', params)
    }
}
