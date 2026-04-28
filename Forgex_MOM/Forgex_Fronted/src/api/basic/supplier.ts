import http from '@/api/http'

/**
 * 供应商详情信息。
 */
export interface SupplierDetail {
  id?: number
  supplierId?: number
  legalRepresentative?: string
  registeredCapital?: number | string
  establishmentDate?: string
  enterpriseNature?: string
  industryCategory?: string
  registeredAddress?: string
  businessAddress?: string
  email?: string
  taxNumber?: string
  bankName?: string
  bankAccount?: string
  invoiceType?: string
  defaultTaxRate?: number | string
}

/**
 * 供应商联系人。
 */
export interface SupplierContact {
  id?: number
  supplierId?: number
  contactName?: string
  contactPhone?: string
  contactPosition?: string
  contactEmail?: string
}

/**
 * 供应商资质。
 */
export interface SupplierQualification {
  id?: number
  supplierId?: number
  qualificationType?: string
  certificateNo?: string
  issueDate?: string
  expireDate?: string
  attachment?: string
  valid?: boolean
}

/**
 * 供应商聚合对象。
 */
export interface Supplier {
  id?: number
  tenantId?: number
  supplierCode: string
  autoGenerateCode?: boolean
  supplierFullName: string
  supplierShortName?: string
  logoUrl?: string
  englishName?: string
  currentAddress?: string
  primaryContact?: string
  contactPhone?: string
  cooperationStatus?: string
  creditLevel?: string
  riskLevel?: string
  supplierLevel?: string
  relatedTenantCode?: string
  hasRelatedTenant?: boolean
  reviewStatus?: number
  remark?: string
  createTime?: string
  updateTime?: string
  detail?: SupplierDetail
  contactList?: SupplierContact[]
  qualificationList?: SupplierQualification[]

  /** 兼容旧页面展示字段。 */
  supplierName?: string
  contactPerson?: string
  status?: number
}

/**
 * 供应商分页查询参数。
 */
export interface SupplierPageParam {
  pageNum: number
  pageSize: number
  supplierCode?: string
  supplierName?: string
  supplierFullName?: string
  cooperationStatus?: string
  creditLevel?: string
  riskLevel?: string
  supplierLevel?: string
  relatedTenantCode?: string
  reviewStatus?: number
  status?: number
}

/**
 * 供应商同步调用参数。
 */
export interface SupplierThirdPartyInvoke {
  apiCode?: string
  tenantId?: number
  payload?: Record<string, any>
}

/**
 * 供应商同步结果。
 */
export interface SupplierThirdPartySyncResult {
  totalCount: number
  createdCount: number
  updatedCount: number
  failedCount: number
  failedSupplierCodes: string[]
}

function withCompatFields<T extends Supplier>(supplier: T): T {
  if (!supplier) {
    return supplier
  }
  return {
    ...supplier,
    supplierName: supplier.supplierFullName,
    contactPerson: supplier.primaryContact,
    status: supplier.cooperationStatus === '2' ? 1 : 0,
  }
}

function normalizePageResult(result: any) {
  const records = Array.isArray(result?.records)
    ? result.records.map((item: Supplier) => withCompatFields(item))
    : []
  return {
    ...result,
    records,
  }
}

export const supplierApi = {
  /**
   * 分页查询供应商。
   */
  async page(params: SupplierPageParam) {
    const result = await http.post('/basic/supplier/page', params)
    return normalizePageResult(result)
  },

  /**
   * 查询供应商列表。
   */
  async list(params?: Partial<SupplierPageParam>) {
    const result = await http.post('/basic/supplier/list', params || {})
    return Array.isArray(result) ? result.map((item: Supplier) => withCompatFields(item)) : []
  },

  /**
   * 获取供应商聚合详情。
   */
  async detail(params: { id: number }) {
    const result = await http.post('/basic/supplier/detail', params)
    return withCompatFields(result)
  },

  /**
   * 新增供应商。
   */
  create(data: Supplier) {
    return http.post('/basic/supplier/create', data)
  },

  /**
   * 修改供应商。
   */
  update(data: Supplier) {
    return http.post('/basic/supplier/update', data)
  },

  /**
   * 删除供应商。
   */
  delete(id: number) {
    return http.post('/basic/supplier/delete', { id })
  },

  /**
   * 生成供应商租户。
   */
  generateTenant(id: number) {
    return http.post<string>('/basic/supplier/generate-tenant', { id })
  },

  /**
   * 发起供应商资质审查。
   */
  startReview(supplierId: number, selectedApprovers?: number[]) {
    return http.post<number>('/basic/supplier/review/start', { supplierId, selectedApprovers })
  },

  /**
   * 同步供应商到第三方。
   */
  syncThirdParty(request?: SupplierThirdPartyInvoke) {
    return http.post<SupplierThirdPartySyncResult>('/basic/supplier/sync-third-party', request || { payload: {} })
  },

  /**
   * 导入供应商 Excel。
   */
  import(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<SupplierThirdPartySyncResult>('/basic/supplier/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  /**
   * 下载导入模板。
   */
  importTemplate() {
    return http.post('/basic/supplier/import-template', {}, { responseType: 'blob' })
  },

  /**
   * 导出供应商。
   */
  export(params?: Partial<SupplierPageParam>) {
    return http.post('/basic/supplier/export', params || {}, { responseType: 'blob' })
  },
}
