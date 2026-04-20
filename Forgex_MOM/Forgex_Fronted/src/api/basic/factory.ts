import http from '@/api/http'

export interface Factory {
    id: number
    factoryCode: string
    factoryName: string
    factoryType: string
    address: string
    contactPerson: string
    contactPhone: string
    status: number
    sortOrder: number
    remark: string
}

export interface FactoryPageParam {
    pageNum: number
    pageSize: number
    factoryCode?: string
    factoryName?: string
    factoryType?: string
    status?: number
}

export const factoryApi = {
    /**
     * 分页查询工厂列表
     */
    page(params: FactoryPageParam) {
        return http.post('/basic/factory/page', params)
    },

    /**
     * 查询工厂列表（不分页）
     */
    list(params?: { status: number }) {
        return http.post('/basic/factory/list', params || {})
    },

    /**
     * 获取工厂详情
     */
    detail(params: { id: number }) {
        return http.post('/basic/factory/detail', params)
    }
}
