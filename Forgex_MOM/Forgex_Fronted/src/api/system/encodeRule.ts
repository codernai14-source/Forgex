import http from '../http'

export interface EncodeRuleDetail {
  id?: string
  ruleId?: string
  segmentOrder?: number
  segmentType?: string
  segmentValue?: string
  connector?: string
  isRequired?: boolean
  conditionExpression?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface EncodeRule {
  id?: string
  ruleName?: string
  ruleCode?: string
  module?: string
  prefix?: string
  date表单at?: string
  serialLength?: number
  resetCycle?: string
  currentSerial?: number
  isEnabled?: boolean
  isEnabledText?: string
  sortOrder?: number
  remark?: string
  createTime?: string
  updateTime?: string
  detailList?: EncodeRuleDetail[]
}

export interface EncodeRulePageResult {
  records: EncodeRule[]
  total: number
}

export interface EncodeRuleQuery {
  pageNum?: number
  pageSize?: number
  ruleName?: string
  ruleCode?: string
  module?: string
  isEnabled?: boolean
  sortField?: string
  sortOrder?: string
}

export interface SaveEncodeRuleParam {
  id?: string
  ruleName: string
  ruleCode: string
  module?: string
  isEnabled?: boolean
  remark?: string
  prefix?: string
  date表单at?: string
  serialLength?: number
  resetCycle?: string
  sortOrder?: number
  detailList?: EncodeRuleDetail[]
}

export interface GenerateEncodeParam {
  ruleCode: string
  businessParams?: Record<string, unknown>
}

export async function pageEncodeRules(query: EncodeRuleQuery) {
  return http.post<EncodeRulePageResult>('/sys/encodeRule/page', query)
}

export async function getEncodeRule(id: string) {
  return http.post<EncodeRule>('/sys/encodeRule/get', { id })
}

export async function saveEncodeRule(param: SaveEncodeRuleParam) {
  return http.post('/sys/encodeRule/save', param)
}

export async function deleteEncodeRule(id: string) {
  return http.post('/sys/encodeRule/delete', { id })
}

export async function batchDeleteEncodeRules(ids: string[]) {
  return http.post('/sys/encodeRule/batchDelete', { ids })
}

export async function generateEncode(param: GenerateEncodeParam) {
  return http.post<string>('/sys/encodeRule/generate', param)
}

export async function testEncodeRule(param: SaveEncodeRuleParam) {
  return http.post('/sys/encodeRule/test', param)
}

export const encodeRuleApi = {
  pageEncodeRules,
  getEncodeRule,
  saveEncodeRule,
  deleteEncodeRule,
  batchDeleteEncodeRules,
  generateEncode,
  testEncodeRule,
}
