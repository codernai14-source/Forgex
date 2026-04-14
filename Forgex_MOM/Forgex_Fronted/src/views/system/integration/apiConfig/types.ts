/**
 * API 接口配置管理类型定义
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-14
 */
import type {
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiConfigDetail,
  ApiConfigItem,
} from '@/api/system/integration'

export type {
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiConfigDetail,
  ApiConfigItem,
}

/**
 * 参数配置项
 */
export interface ApiParameterConfig {
  id?: number
  apiId: number
  paramName: string
  paramType: string
  paramDataType: string
  required: boolean
  defaultValue?: string
  description?: string
  orderNum: number
  enabled: boolean
}
