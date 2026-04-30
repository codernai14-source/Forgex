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
    tenantId: number
    createTime: string
    updateTime: string
    createBy: string
    updateBy: string
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
    page(params: MaterialPageParam) {
        return http.post('/basic/material/page', params)
    },

    list(params?: { status?: number; materialType?: string }) {
        return http.post('/basic/material/list', params || {})
    },

    detail(params: { id: number }) {
        return http.post('/basic/material/detail', params)
    },

    create(data: any) {
        return http.post('/basic/material/create', data)
    },

    update(data: any) {
        return http.post('/basic/material/update', data)
    },

    delete(params: { id: number }) {
        return http.post('/basic/material/delete', params)
    },

    batchDelete(params: { ids: number[] }) {
        return http.post('/basic/material/batchDelete', params)
    }
}
