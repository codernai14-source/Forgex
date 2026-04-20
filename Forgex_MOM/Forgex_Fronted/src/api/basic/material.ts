import http from '@/api/http'

export interface Material {
    id: number
    materialCode: string
    materialName: string
    materialType: string
    materialCategory: string
    specification: string
    unit: string
    brand: string
    imageUrl: string
    orderNum: number
    extendJson: string
    remark: string
    description: string
    status: number
    approvalStatus: string
}

export interface MaterialPageParam {
    pageNum: number
    pageSize: number
    materialCode?: string
    materialName?: string
    materialType?: string
    materialCategory?: string
    status?: number
    approvalStatus?: string
}

export const materialApi = {
    /**
     * 分页查询物料列表
     */
    page(params: MaterialPageParam) {
        return http.post('/basic/material/page', params)
    },

    /**
     * 查询物料列表（不分页）
     */
    list(params?: { status?: number }) {
        return http.post('/basic/material/list', params || {})
    },

    /**
     * 获取物料详情
     */
    detail(params: { id: number }) {
        return http.post('/basic/material/detail', params)
    }
}
