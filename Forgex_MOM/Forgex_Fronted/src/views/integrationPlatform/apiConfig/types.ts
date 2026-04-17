import type {
  ApiConfigItem,
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiParamConfigItem,
  ApiParamMappingItem,
  IntegrationDirection,
  ParamDirection,
} from '@/api/system/integration'

export type {
  ApiConfigItem,
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiParamConfigItem,
  ApiParamMappingItem,
  IntegrationDirection,
  ParamDirection,
}

export interface ParamMappingRow {
  id?: number
  sourceFieldPath: string
  targetFieldPath: string
  sourceType?: string
  targetType?: string
  required?: boolean
  remark?: string
}
