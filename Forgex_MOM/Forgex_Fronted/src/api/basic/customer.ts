import http from '@/api/http'

export interface Customer {
    id: number
    customerCode: string
    customerName: string
    customerShortName: string
    customerType: string
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
    deliveryTerms: string
    currency: string
    status: number
    remark: string
}

export interface CustomerPageParam {
    pageNum: number
    pageSize: number
    customerCode?: string
    customerName?: string
    customerType?: string
    status?: number
}

export const customerApi = {
    /**
     * 分页查询客户列表
     */
    page(params: CustomerPageParam) {
        return http.post('/basic/customer/page', params)
    },

    /**
     * 查询客户列表（不分页）
     */
    list(params?: { status?: number }) {
        return http.post('/basic/customer/list', params || {})
    },

    /**
     * 获取客户详情
     */
    detail(params: { id: number }) {
        return http.post('/basic/customer/detail', params)
    }
}
