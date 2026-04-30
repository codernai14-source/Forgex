import http from '@/api/http'

export interface Currency {
  id?: number
  currencyCode: string
  currencyNumCode?: string
  currencyNameCn?: string
  currencyNameEn?: string
  currencySymbol?: string
  decimalDigits?: number
  isBaseCurrency?: boolean
  countryRegion?: string
  status?: number
  remark?: string
}

export interface RateType {
  id?: number
  rateTypeCode: string
  rateTypeName?: string
  businessScene?: string
  isDefault?: boolean
  status?: number
}

export interface ExchangeRate {
  id?: number
  sourceCurrencyCode: string
  targetCurrencyCode: string
  rateTypeCode: string
  effectiveDate?: string
  exchangeRate?: number | string
  expireDate?: string
  approveStatus?: number
  approveUser?: string
  approveTime?: string
  orgId?: number
  remark?: string
}

export interface PageParam {
  pageNum: number
  pageSize: number
  [key: string]: any
}

export const currencyApi = {
  page(params: PageParam) {
    return http.post('/basic/currency/page', params)
  },
  list(params?: Partial<PageParam>) {
    return http.post<Currency[]>('/basic/currency/list', params || {})
  },
  detail(id: number) {
    return http.post<Currency>('/basic/currency/detail', { id })
  },
  create(data: Currency) {
    return http.post('/basic/currency/create', data)
  },
  update(data: Currency) {
    return http.post('/basic/currency/update', data)
  },
  delete(id: number) {
    return http.post('/basic/currency/delete', { id })
  },
  setBase(id: number) {
    return http.post('/basic/currency/set-base', { id })
  },
  enable(id: number) {
    return http.post('/basic/currency/enable', { id })
  },
  disable(id: number) {
    return http.post('/basic/currency/disable', { id })
  },
}

export const rateTypeApi = {
  page(params: PageParam) {
    return http.post('/basic/currency/rate-type/page', params)
  },
  list(params?: Partial<PageParam>) {
    return http.post<RateType[]>('/basic/currency/rate-type/list', params || {})
  },
  create(data: RateType) {
    return http.post('/basic/currency/rate-type/create', data)
  },
  update(data: RateType) {
    return http.post('/basic/currency/rate-type/update', data)
  },
  delete(id: number) {
    return http.post('/basic/currency/rate-type/delete', { id })
  },
  setDefault(id: number) {
    return http.post('/basic/currency/rate-type/set-default', { id })
  },
}

export const exchangeRateApi = {
  page(params: PageParam) {
    return http.post('/basic/currency/exchange-rate/page', params)
  },
  detail(id: number) {
    return http.post<ExchangeRate>('/basic/currency/exchange-rate/detail', { id })
  },
  create(data: ExchangeRate) {
    return http.post('/basic/currency/exchange-rate/create', data)
  },
  update(data: ExchangeRate) {
    return http.post('/basic/currency/exchange-rate/update', data)
  },
  delete(id: number) {
    return http.post('/basic/currency/exchange-rate/delete', { id })
  },
  startApproval(rateId: number, selectedApprovers?: number[]) {
    return http.post('/basic/currency/exchange-rate/approval/start', { rateId, selectedApprovers })
  },
  current(data: Partial<ExchangeRate> & { rateDate?: string }) {
    return http.post<ExchangeRate>('/basic/currency/exchange-rate/current', data)
  },
  logPage(params: PageParam) {
    return http.post('/basic/currency/exchange-rate/log/page', params)
  },
}
