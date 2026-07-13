import http from '@/api/http'

export interface Material {
    id?: number
    materialCode: string
    autoGenerateCode?: boolean
    materialName: string
    materialType: string
    materialCategory?: string
    specification?: string
    unit?: string
    brand?: string
    imageUrl?: string
    orderNum?: number
    extendJson?: string
    extendList?: MaterialExtend[]
    extendValueList?: MaterialExtendFieldValue[]
    remark?: string
    description?: string
    status?: number
    approvalStatus?: string
    tenantId?: number
    createTime?: string
    updateTime?: string
    createBy?: string
    updateBy?: string
}

export interface MaterialExtend {
    id?: number
    materialId?: number
    module?: string
    moduleLabel?: string
    fieldName?: string
    fieldLabel?: string
    fieldType?: string
    fieldTypeLabel?: string
    fieldOptions?: string
    required?: number
    validationRule?: string
    defaultValue?: string
    orderNum?: number
    extendJson?: string
    remark?: string
    status?: number
}

export interface MaterialExtendFieldValue {
    module: string
    values: Record<string, any>
}

export interface MaterialExtendConfig {
    id?: number
    module: string
    moduleName?: string
    materialType: string
    fieldName: string
    fieldLabel: string
    fieldType: string
    fieldTypeName?: string
    fieldOptions?: string
    options?: Array<{ label: string; value: string }>
    required?: number
    validationRule?: string
    defaultValue?: string
    orderNum?: number
    remark?: string
    status?: number
    createTime?: string
    updateTime?: string
}

export interface MaterialExtendSchema {
    id?: number
    module: string
    moduleName?: string
    materialType: string
    schemaJson?: string
    version?: number
    status?: number
    fields: MaterialExtendConfig[]
}

export interface MaterialExtendView {
    module: string
    moduleName: string
    materialType: string
    extendId?: number
    extendJson?: string
    fields: MaterialExtendViewField[]
    unknownValues?: Record<string, any>
}

export interface MaterialExtendViewField {
    configId?: number
    fieldName: string
    fieldLabel: string
    fieldType: string
    fieldTypeName?: string
    options?: Array<{ label: string; value: string }>
    required?: number
    defaultValue?: string
    orderNum?: number
    value?: any
    displayValue?: string
}

export interface MaterialDetail {
    baseInfo?: Material
    extendList?: MaterialExtend[]
    extendViewList?: MaterialExtendView[]
}

export interface MaterialThirdPartyInvoke {
    apiCode?: string
    tenantId?: number
    payload?: Record<string, any>
}

export interface MaterialThirdPartySyncResult {
    totalCount: number
    createdCount: number
    updatedCount: number
    failedCount: number
    failedMaterialCodes: string[]
}

export interface MaterialPageParam {
    pageNum: number
    pageSize: number
    materialCode?: string
    materialName?: string
    materialType?: string
    materialCategory?: string
    unit?: string
    brand?: string
    status?: number
    approvalStatus?: string
}

export interface MaterialExtendSchemaQuery {
    pageNum?: number
    pageSize?: number
    module?: string
    materialType?: string
}

export const materialApi = {
    page(params: MaterialPageParam) {
        return http.post('/basic/material/page', params)
    },

    list(params?: { status?: number; materialType?: string }) {
        return http.post('/basic/material/list', params || {})
    },

    detail(params: { id: number }) {
        return http.post<MaterialDetail>('/basic/material/detail', params)
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
    },

    syncThirdParty(request?: MaterialThirdPartyInvoke) {
        return http.post<MaterialThirdPartySyncResult>('/basic/material/sync-third-party', request || { payload: {} })
    },

    pullFromThirdParty(request?: MaterialThirdPartyInvoke) {
        return http.post<MaterialThirdPartySyncResult>('/basic/material/pull-from-third-party', request || { payload: {} })
    },

    extendSchemaPage(params: MaterialExtendSchemaQuery) {
        return http.post('/basic/material/extend-schema/page', params)
    },

    extendSchema(params: MaterialExtendSchemaQuery) {
        return http.post<MaterialExtendSchema>('/basic/material/extend-schema/schema', params)
    },

    extendFieldDetail(params: { id: number }) {
        return http.post<MaterialExtendConfig>('/basic/material/extend-schema/detail', params)
    },

    createExtendField(data: MaterialExtendConfig) {
        return http.post<number>('/basic/material/extend-schema/create', data)
    },

    updateExtendField(data: MaterialExtendConfig) {
        return http.post('/basic/material/extend-schema/update', data)
    },

    deleteExtendField(params: { id: number }) {
        return http.post('/basic/material/extend-schema/delete', params)
    },

    updateExtendFieldStatus(params: { id: number; status: number }) {
        return http.post('/basic/material/extend-schema/status', params)
    },

    sortExtendFields(items: Array<{ id: number; orderNum: number }>) {
        return http.post('/basic/material/extend-schema/sort', { items })
    }
}
