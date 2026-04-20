import type {
  ApiConfigItem,
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiParamConfigItem,
  ApiParamMappingItem,
  IntegrationDirection,
  ParamSourceType,
  ParamDirection,
} from '@/api/system/integration'

export type {
  ApiConfigItem,
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiParamConfigItem,
  ApiParamMappingItem,
  IntegrationDirection,
  ParamSourceType,
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

export interface ApiConfigEditorState {
  mode: 'list' | 'form' | 'param'
  isEdit: boolean
  apiConfig?: ApiConfigItem
}
