import http from '@/api/http'

export const labelTemplateApi = {
    /**
     * 分页查询模板列表
     */
    page(params: any) {
        return http.post('/label/template/page', params)
    },

    /**
     * 查询模板详情
     */
    detail(id: number) {
        return http.post('/label/template/detail', { id })
    },

    /**
     * 新增模板
     */
    add(params: any) {
        return http.post('/label/template/add', params)
    },

    /**
     * 修改模板
     */
    update(params: any) {
        return http.post('/label/template/update', params)
    },

    /**
     * 删除模板
     */
    delete(id: number) {
        return http.post('/label/template/delete', { id })
    },

    /**
     * 批量删除模板
     */
    batchDelete(ids: number[]) {
        return http.post('/label/template/batchDelete', { ids })
    },

    /**
     * 设置默认模板
     */
    setDefault(id: number, templateType: string) {
        return http.post('/label/template/setDefault', { id, templateType })
    },

    /**
     * 获取占位符列表
     */
    getPlaceholders(tenantId?: number) {
        return http.post('/label/template/placeholders', { tenantId })
    }
}
