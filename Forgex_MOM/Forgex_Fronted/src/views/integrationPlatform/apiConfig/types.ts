import type {
  ApiConfigItem,
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiOutboundTargetItem,
  ApiOutboundTargetSubmit,
  ApiParamConfigItem,
  ApiParamMappingItem,
  ApiInvokeMode,
  IntegrationDirection,
  ParamDirection,
  ParamSourceType,
} from '@/api/system/integration'

export type {
  ApiConfigItem,
  ApiConfigQuery,
  ApiConfigSubmit,
  ApiOutboundTargetItem,
  ApiOutboundTargetSubmit,
  ApiParamConfigItem,
  ApiParamMappingItem,
  ApiInvokeMode,
  IntegrationDirection,
  ParamDirection,
  ParamSourceType,
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
