import http from '@/api/http'

export const labelRecordApi = {
    /**
     * 分页查询打印记录列表
     */
    page(params: any) {
        return http.post('/basic/label/record/page', params)
    },

    /**
     * 查询打印记录详情
     */
    detail(id: number) {
        return http.post('/basic/label/record/detail', { id })
    }
}
