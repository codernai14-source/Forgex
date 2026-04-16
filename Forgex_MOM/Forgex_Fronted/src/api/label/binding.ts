import http from '@/api/http'

export const labelBindingApi = {
    /**
     * 分页查询绑定关系列表
     */
    page(params: any) {
        return http.post('/label/template/binding/page', params)
    },

    /**
     * 新增绑定关系
     */
    add(params: any) {
        return http.post('/label/template/binding/add', params)
    },

    /**
     * 删除绑定关系
     */
    delete(id: number) {
        return http.post('/label/template/binding/delete', { id })
    },

    /**
     * 智能匹配模板（打印核心）
     */
    match(params: any) {
        return http.post('/label/template/match', params)
    }
}
