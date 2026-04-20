import http from '@/api/http'

export interface Supplier {
    id: number
    supplierCode: string
    supplierName: string
    supplierShortName: string
    supplierType: string
    country: string
    province: string
    city: string
    address: string
    contactPerson: string
    contactPhone: string
    contactEmail: string
    taxNumber: string
    bankAccount: string
    paymentTerms: string
    qualityLevel: string
    certification: string
    status: number
    remark: string
}

export interface SupplierPageParam {
    pageNum: number
    pageSize: number
    supplierCode?: string
    supplierName?: string
    supplierType?: string
    status?: number
}

export const supplierApi = {
    /**
     * 分页查询供应商列表
     */
    page(params: SupplierPageParam) {
        return http.post('/basic/supplier/page', params)
    },

    /**
     * 查询供应商列表（不分页）
     */
    list(params?: { status?: number }) {
        return http.post('/basic/supplier/list', params || {})
    },

    /**
     * 获取供应商详情
     */
    detail(params: { id: number }) {
        return http.post('/basic/supplier/detail', params)
    }
}
