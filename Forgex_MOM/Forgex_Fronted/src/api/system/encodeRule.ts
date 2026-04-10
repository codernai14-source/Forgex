/**
 * 编码规则管理 API
 * 
 * 提供编码规则的 CRUD 操作、编码生成等功能
 * 
 * @author Forgex
 * @version 1.0.0
 */
import http from '../http'

/**
 * 编码规则明细
 * 
 * 描述编码规则的各个段配置，包括段类型、长度、填充方式等
 */
export interface EncodeRuleDetail {
  /**
   * 明细 ID
   */
  id?: string
  
  /**
   * 规则 ID
   */
  ruleId?: string
  
  /**
   * 段序号（从 1 开始）
   */
  segmentOrder?: number
  
  /**
   * 段类型：FIXED-固定值，DATE-日期，SEQ-序列号，CUSTOM-自定义
   */
  segmentType?: string
  
  /**
   * 段值（固定值或自定义值）
   */
  segmentValue?: string
  
  /**
   * 日期格式（如：YYYY, YYYYMM, YYYYMMDD 等）
   */
  dateFormat?: string
  
  /**
   * 序列号起始值
   */
  seqStart?: number
  
  /**
   * 序列号长度（不足补零）
   */
  seqLength?: number
  
  /**
   * 是否重置序列：0-不重置，1-按年重置，2-按月重置，3-按日重置
   */
  seqResetType?: number
  
  /**
   * 备注
   */
  remark?: string
}

/**
 * 编码规则
 * 
 * 用于定义业务编码的生成规则，支持多段组合
 */
export interface EncodeRule {
  /**
   * 规则 ID
   */
  id?: string
  
  /**
   * 规则名称
   */
  ruleName?: string
  
  /**
   * 规则编码（唯一标识）
   */
  ruleCode?: string
  
  /**
   * 业务类型（用于区分不同业务场景）
   */
  businessType?: string
  
  /**
   * 规则描述
   */
  description?: string
  
  /**
   * 是否启用：0-禁用，1-启用
   */
  status?: number
  
  /**
   * 编码示例（根据规则生成的示例编码）
   */
  exampleCode?: string
  
  /**
   * 规则明细列表
   */
  ruleDetails?: EncodeRuleDetail[]
  
  /**
   * 创建时间
   */
  createTime?: string
  
  /**
   * 更新时间
   */
  updateTime?: string
  
  /**
   * 备注
   */
  remark?: string
}

/**
 * 编码规则查询参数
 */
export interface EncodeRuleQuery {
  /**
   * 页码
   */
  pageNum?: number
  
  /**
   * 每页条数
   */
  pageSize?: number
  
  /**
   * 规则名称（模糊查询）
   */
  ruleName?: string
  
  /**
   * 规则编码（模糊查询）
   */
  ruleCode?: string
  
  /**
   * 业务类型（精确查询）
   */
  businessType?: string
  
  /**
   * 状态：0-禁用，1-启用
   */
  status?: number
  
  /**
   * 排序字段
   */
  sortField?: string
  
  /**
   * 排序方式：asc-升序，desc-降序
   */
  sortOrder?: string
}

/**
 * 保存编码规则参数
 */
export interface SaveEncodeRuleParam {
  /**
   * 规则 ID（新增时不传）
   */
  id?: string
  
  /**
   * 规则名称
   */
  ruleName: string
  
  /**
   * 规则编码
   */
  ruleCode: string
  
  /**
   * 业务类型
   */
  businessType?: string
  
  /**
   * 规则描述
   */
  description?: string
  
  /**
   * 状态：0-禁用，1-启用
   */
  status?: number
  
  /**
   * 规则明细列表
   */
  ruleDetails?: EncodeRuleDetail[]
  
  /**
   * 备注
   */
  remark?: string
}

/**
 * 生成编码参数
 */
export interface GenerateEncodeParam {
  /**
   * 规则编码
   */
  ruleCode: string
  
  /**
   * 业务参数（可选，用于自定义段）
   */
  businessParams?: Record<string, any>
}

/**
 * 生成编码结果
 */
export interface GenerateEncodeResult {
  /**
   * 生成的编码
   */
  encodeCode: string
  
  /**
   * 规则编码
   */
  ruleCode: string
  
  /**
   * 生成时间
   */
  generateTime: string
}

/**
 * 获取编码规则分页列表
 * 
 * 执行步骤：
 * 1. 接收查询参数（包含分页、搜索条件）
 * 2. 调用后端分页接口
 * 3. 返回分页结果
 * 
 * @param query 查询参数
 * @param query.ruleName 规则名称（可选，模糊查询）
 * @param query.ruleCode 规则编码（可选，模糊查询）
 * @param query.businessType 业务类型（可选，精确查询）
 * @param query.status 状态（可选，0-禁用，1-启用）
 * @param query.pageNum 页码，默认 1
 * @param query.pageSize 每页条数，默认 10
 * @returns 分页结果，包含 records（规则列表）和 total（总数）
 * @throws 查询失败时抛出异常
 */
export async function pageEncodeRules(query: EncodeRuleQuery) {
  return http.post('/sys/encodeRule/page', query)
}

/**
 * 获取编码规则详情
 * 
 * @param id 规则 ID
 * @returns 编码规则详情对象
 * @throws 查询失败时抛出异常
 */
export async function getEncodeRule(id: string) {
  return http.post('/sys/encodeRule/detail', { id })
}

/**
 * 保存编码规则（新增或更新）
 * 
 * @param param 保存参数
 * @param param.id 规则 ID（新增时不传）
 * @param param.ruleName 规则名称
 * @param param.ruleCode 规则编码
 * @param param.businessType 业务类型
 * @param param.ruleDetails 规则明细列表
 * @returns 执行结果
 * @throws 保存失败时抛出异常
 */
export async function saveEncodeRule(param: SaveEncodeRuleParam) {
  if (param.id) {
    return http.post('/sys/encodeRule/update', param)
  } else {
    return http.post('/sys/encodeRule/create', param)
  }
}

/**
 * 删除编码规则
 * 
 * @param id 规则 ID
 * @returns 执行结果
 * @throws 删除失败时抛出异常
 */
export async function deleteEncodeRule(id: string) {
  return http.post('/sys/encodeRule/delete', { id })
}

/**
 * 批量删除编码规则
 * 
 * @param ids 规则 ID 列表
 * @returns 执行结果
 * @throws 删除失败时抛出异常
 */
export async function batchDeleteEncodeRules(ids: string[]) {
  return http.post('/sys/encodeRule/batchDelete', { ids })
}

/**
 * 生成编码
 * 
 * 根据指定的编码规则生成业务编码
 * 
 * @param param 生成参数
 * @param param.ruleCode 规则编码
 * @param param.businessParams 业务参数（可选）
 * @returns 生成的编码结果
 * @throws 生成失败时抛出异常
 */
export async function generateEncode(param: GenerateEncodeParam) {
  return http.post('/sys/encodeRule/generate', param)
}

/**
 * 测试编码规则
 * 
 * 根据规则配置测试生成编码示例
 * 
 * @param param 保存参数（包含规则配置）
 * @returns 测试生成的编码示例
 * @throws 测试失败时抛出异常
 */
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
