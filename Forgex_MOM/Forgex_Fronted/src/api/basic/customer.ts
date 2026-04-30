import http from '@/api/http'

export interface CustomerContact {
  id?: number
  customerId?: number
  contactName?: string
  contactPosition?: string
  contactPhone?: string
}

export interface CustomerInvoice {
  id?: number
  customerId?: number
  invoiceFullName?: string
  taxNumber?: string
  registeredAddress?: string
  registeredPhone?: string
  bankName?: string
  bankAccount?: string
  invoiceRequired?: boolean
}

export interface CustomerExtra {
  id?: number
  customerId?: number
  officialWebsite?: string
  switchboardPhone?: string
  officialEmailDomain?: string
  faxNumber?: string
  socialMediaAccount?: string
  equityPenetrationLevel?: number
  holdingRelationFlag?: string
  relatedEnterpriseIds?: string
  groupCustomerLevel?: string
  channelPartnerLevel?: string
  cooperationAuthStartDate?: string
  cooperationAuthEndDate?: string
  nationalIndustryCode?: string
  customIndustryCategory?: string
  registeredCapital?: number | string
  registeredCapitalCurrency?: string
  paidInCapital?: number | string
  paidInCapitalCurrency?: string
  businessTermStart?: string
  businessTermEnd?: string
  registrationAuthority?: string
  businessScope?: string
}

export interface Customer {
  id?: number
  customerCode?: string
  autoGenerateCode?: boolean
  customerShortName?: string
  customerFullName?: string
  customerName?: string
  customerValueLevel?: string
  customerCreditLevel?: string
  actualBusinessAddress?: string
  businessStatus?: string
  collectionAddress?: string
  shippingAddress?: string
  approvalStatus?: number
  isRelatedTenant?: boolean
  hasRelatedTenant?: boolean
  relatedTenantCode?: string
  transportMode?: string
  paymentTerms?: string
  country?: string
  enterpriseNature?: string
  status?: number
  remark?: string
  contactList?: CustomerContact[]
  invoice?: CustomerInvoice
  extra?: CustomerExtra
}

export interface CustomerPageParam {
  pageNum: number
  pageSize: number
  customerCode?: string
  customerName?: string
  customerFullName?: string
  customerValueLevel?: string
  customerCreditLevel?: string
  businessStatus?: string
  approvalStatus?: number
  isRelatedTenant?: boolean
  status?: number
}

export const customerApi = {
  page(params: CustomerPageParam) {
    return http.post('/basic/customer/page', params)
  },
  list(params?: Partial<CustomerPageParam>) {
    return http.post<Customer[]>('/basic/customer/list', params || {})
  },
  detail(params: { id: number }) {
    return http.post<Customer>('/basic/customer/detail', params)
  },
  create(data: Customer) {
    return http.post('/basic/customer/create', data)
  },
  update(data: Customer) {
    return http.post('/basic/customer/update', data)
  },
  delete(id: number) {
    return http.post('/basic/customer/delete', { id })
  },
  generateTenant(id: number) {
    return http.post<string>('/basic/customer/generate-tenant', { id })
  },
  startApproval(customerId: number, selectedApprovers?: number[]) {
    return http.post<number>('/basic/customer/approval/start', { customerId, selectedApprovers })
  },
}
