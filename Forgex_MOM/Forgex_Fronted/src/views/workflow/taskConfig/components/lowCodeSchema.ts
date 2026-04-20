import type { Options, Rule } from '@form-create/ant-design-vue'

export type LowCodeFieldComponent =
  | 'input'
  | 'textarea'
  | 'number'
  | 'select'
  | 'date'
  | 'radio'
  | 'checkbox'
  | 'switch'
  | 'upload'
  | 'divider'
  | 'grid'

export interface LowCodeFieldOption {
  label: string
  value: string
}

export interface LowCodeFieldSchema {
  key: string
  label: string
  component: LowCodeFieldComponent
  required?: boolean
  placeholder?: string
  defaultValue?: string | number | boolean | string[] | Record<string, any> | Array<any>
  options?: LowCodeFieldOption[]
  span?: number
  rows?: number
  min?: number
  max?: number
  help?: string
}

export interface LowCodeFormConfig {
  labelWidth?: number
  labelAlign?: 'left' | 'right' | 'top'
  size?: 'small' | 'middle' | 'large'
  layout?: 'vertical' | 'horizontal' | 'inline'
}

export type FormCreateRule = Rule
export type FormCreateOption = Options

export interface FormCreatePayload {
  version: string
  formCreateVersion: string
  designerType: 'form-create'
  rule: FormCreateRule[]
  option: FormCreateOption
}

export interface LegacyLowCodeFormSchema {
  version?: string
  formCreateVersion?: string
  formConfig?: LowCodeFormConfig
  rules?: Array<Record<string, any>>
  fields?: LowCodeFieldSchema[]
}

export interface LowCodeFormSchema {
  version: string
  formCreateVersion: string
  designerType: 'form-create' | 'legacy'
  formConfig: LowCodeFormConfig
  rule: FormCreateRule[]
  option: FormCreateOption
  fields: LowCodeFieldSchema[]
  legacyFields: LowCodeFieldSchema[]
  legacySchema?: LegacyLowCodeFormSchema
}

const DEFAULT_FORM_OPTION: FormCreateOption = {
  form: {
    layout: 'vertical',
    labelAlign: 'right',
    size: 'middle',
    colon: false,
    labelCol: {
      style: {
        width: '120px',
      },
    },
    wrapperCol: {
      span: 24,
    },
  },
  row: {
    gutter: 16,
  },
  submitBtn: false,
  resetBtn: false,
}

export const DEFAULT_LOW_CODE_FORM_SCHEMA: LowCodeFormSchema = {
  version: '3.0.0',
  formCreateVersion: 'ant-design-vue',
  designerType: 'form-create',
  formConfig: {
    layout: 'vertical',
    labelAlign: 'right',
    labelWidth: 120,
    size: 'middle',
  },
  rule: [],
  option: cloneValue(DEFAULT_FORM_OPTION),
  fields: [],
  legacyFields: [],
}

const supportedComponents: LowCodeFieldComponent[] = [
  'input',
  'textarea',
  'number',
  'select',
  'date',
  'radio',
  'checkbox',
  'switch',
  'upload',
  'divider',
  'grid',
]

const fieldLikeTypes = new Set([
  'input',
  'textarea',
  'number',
  'inputNumber',
  'select',
  'date',
  'datePicker',
  'rangePicker',
  'timePicker',
  'timeRangePicker',
  'radio',
  'checkbox',
  'switch',
  'upload',
  'frame',
  'treeSelect',
  'cascader',
  'autoComplete',
  'slider',
  'rate',
  'inputPassword',
  'search',
])

export function normalizeLowCodeFormSchema(value?: string | LowCodeFormSchema | FormCreatePayload | LegacyLowCodeFormSchema | null): LowCodeFormSchema {
  if (!value) {
    return cloneSchema(DEFAULT_LOW_CODE_FORM_SCHEMA)
  }

  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value

    if (isFormCreatePayload(parsed)) {
      return buildSchemaFromFormCreate(parsed)
    }

    if (Array.isArray(parsed)) {
      return buildLegacySchema({ fields: parsed })
    }

    if (isLegacySchema(parsed)) {
      return buildLegacySchema(parsed)
    }

    return cloneSchema(DEFAULT_LOW_CODE_FORM_SCHEMA)
  } catch {
    return cloneSchema(DEFAULT_LOW_CODE_FORM_SCHEMA)
  }
}

export function stringifyLowCodeFormSchema(schema: LowCodeFormSchema) {
  return JSON.stringify(toStoredLowCodePayload(schema))
}

export function toStoredLowCodePayload(schema: LowCodeFormSchema): FormCreatePayload | LegacyLowCodeFormSchema {
  const normalized = normalizeLowCodeFormSchema(schema)
  if (normalized.designerType === 'form-create') {
    return {
      version: normalized.version,
      formCreateVersion: normalized.formCreateVersion,
      designerType: 'form-create',
      rule: cloneValue(normalized.rule),
      option: cloneValue(normalized.option),
    }
  }

  return cloneValue(normalized.legacySchema || {
    version: normalized.version,
    formCreateVersion: normalized.formCreateVersion,
    formConfig: normalized.formConfig,
    fields: normalized.legacyFields,
  })
}

export function isFormCreatePayload(value: unknown): value is FormCreatePayload {
  if (!value || typeof value !== 'object') {
    return false
  }
  const payload = value as Record<string, any>
  return Array.isArray(payload.rule) && typeof payload.option === 'object'
}

export function isLegacySchema(value: unknown): value is LegacyLowCodeFormSchema {
  if (!value || typeof value !== 'object') {
    return false
  }
  const payload = value as Record<string, any>
  return Array.isArray(payload.fields) || Array.isArray(payload.rules)
}

export function extractLowCodeFieldOptions(raw: string) {
  return normalizeLowCodeFormSchema(raw).fields
    .filter(field => field.key && !['divider', 'grid'].includes(field.component))
    .map(field => ({
      key: field.key,
      label: field.label || field.key,
      component: field.component,
    }))
}

export function buildFormCreatePayload(rule: FormCreateRule[], option?: FormCreateOption): FormCreatePayload {
  return {
    version: '3.0.0',
    formCreateVersion: 'ant-design-vue',
    designerType: 'form-create',
    rule: cloneValue(rule || []),
    option: mergeFormOption(option),
  }
}

function buildSchemaFromFormCreate(payload: FormCreatePayload): LowCodeFormSchema {
  const option = mergeFormOption(payload.option)
  const formConfig = toFormConfig(option)
  const fields = extractFieldsFromRules(payload.rule || [])

  return {
    version: typeof payload.version === 'string' ? payload.version : '3.0.0',
    formCreateVersion: typeof payload.formCreateVersion === 'string' ? payload.formCreateVersion : 'ant-design-vue',
    designerType: 'form-create',
    formConfig,
    rule: cloneValue(payload.rule || []),
    option,
    fields,
    legacyFields: [],
  }
}

function buildLegacySchema(schema: LegacyLowCodeFormSchema): LowCodeFormSchema {
  const fields = normalizeLegacyFields(schema.fields?.length ? schema.fields : schema.rules)
  const option = legacyFormOption(schema.formConfig)

  return {
    version: typeof schema.version === 'string' ? schema.version : '2.0.0',
    formCreateVersion: typeof schema.formCreateVersion === 'string' ? schema.formCreateVersion : 'legacy-vue3-antdv',
    designerType: 'legacy',
    formConfig: {
      layout: schema.formConfig?.layout || 'vertical',
      labelAlign: schema.formConfig?.labelAlign || 'right',
      labelWidth: schema.formConfig?.labelWidth || 120,
      size: schema.formConfig?.size || 'middle',
    },
    rule: legacyFieldsToRules(fields),
    option,
    fields,
    legacyFields: fields,
    legacySchema: {
      version: typeof schema.version === 'string' ? schema.version : '2.0.0',
      formCreateVersion: typeof schema.formCreateVersion === 'string' ? schema.formCreateVersion : 'legacy-vue3-antdv',
      formConfig: {
        layout: schema.formConfig?.layout || 'vertical',
        labelAlign: schema.formConfig?.labelAlign || 'right',
        labelWidth: schema.formConfig?.labelWidth || 120,
        size: schema.formConfig?.size || 'middle',
      },
      fields,
    },
  }
}

function extractFieldsFromRules(rules: FormCreateRule[]): LowCodeFieldSchema[] {
  const fields: LowCodeFieldSchema[] = []

  rules.forEach(rule => {
    collectFields(rule, fields)
  })

  return fields
}

function collectFields(rule: Record<string, any>, fields: LowCodeFieldSchema[]) {
  if (!rule || typeof rule !== 'object') {
    return
  }

  const children = Array.isArray(rule.children) ? rule.children : []
  if (children.length) {
    children.forEach(child => collectFields(child, fields))
  }

  const field = typeof rule.field === 'string' ? rule.field.trim() : ''
  const type = String(rule.type || '').trim()
  const component = normalizeComponent(type)

  if (!field && type === 'divider') {
    fields.push({
      key: `divider_${fields.length + 1}`,
      label: String(rule.title || '').trim() || 'Divider',
      component: 'divider',
      span: 24,
    })
    return
  }

  if (!field || !type || !fieldLikeTypes.has(type)) {
    return
  }

  const props = normalizeRuleProps(rule)
  fields.push({
    key: field,
    label: String(rule.title || field).trim(),
    component,
    required: Boolean(Array.isArray(rule.validate) && rule.validate.some((item: Record<string, any>) => item?.required)),
    placeholder: stringOrUndefined(props.placeholder),
    defaultValue: props.value ?? props.defaultValue,
    options: normalizeOptions(rule.options || props.options),
    span: numberOrUndefined(props.col?.span) || numberOrUndefined(rule.col?.span) || numberOrUndefined(props.span),
    rows: numberOrUndefined(props.rows),
    min: numberOrUndefined(props.min),
    max: numberOrUndefined(props.max),
    help: stringOrUndefined(props.info || rule.info),
  })
}

function normalizeLegacyFields(fields: unknown): LowCodeFieldSchema[] {
  if (!Array.isArray(fields)) {
    return []
  }

  return fields
    .map((item, index) => {
      const field = item as Record<string, any>
      const component = normalizeComponent(field?.component || field?.type)
      const key = String(field?.key || field?.field || `field_${index + 1}`).trim()
      const normalized: LowCodeFieldSchema = {
        key: normalizeFieldKey(key),
        label: String(field?.label || field?.title || key).trim(),
        component,
        required: Boolean(field?.required || field?.validate?.some?.((rule: Record<string, any>) => rule?.required)),
        placeholder: stringOrUndefined(field?.placeholder || field?.props?.placeholder),
        defaultValue: field?.defaultValue ?? field?.value,
        span: numberOrUndefined(field?.span || field?.props?.span) || defaultSpan(component),
        rows: numberOrUndefined(field?.rows || field?.props?.rows),
        min: numberOrUndefined(field?.min || field?.props?.min),
        max: numberOrUndefined(field?.max || field?.props?.max),
        help: stringOrUndefined(field?.help),
        options: normalizeOptions(field?.options),
      }
      return normalized
    })
    .filter(field => field.key)
}

function legacyFieldsToRules(fields: LowCodeFieldSchema[]): FormCreateRule[] {
  return fields.map(field => {
    if (field.component === 'divider') {
      return {
        type: 'divider',
        title: field.label,
      } as FormCreateRule
    }

    return {
      type: legacyComponentToRuleType(field.component),
      field: field.key,
      title: field.label || field.key,
      props: compactObject({
        placeholder: field.placeholder,
        rows: field.rows,
        min: field.min,
        max: field.max,
      }),
      col: field.span ? { span: field.span } : undefined,
      options: field.options,
      value: field.defaultValue,
      validate: field.required
        ? [{ required: true, message: `${field.label || field.key}不能为空`, trigger: 'change' }]
        : [],
      info: field.help,
    } as FormCreateRule
  })
}

function mergeFormOption(option?: FormCreateOption): FormCreateOption {
  const merged = cloneValue(DEFAULT_FORM_OPTION)
  if (!option || typeof option !== 'object') {
    return merged
  }

  const nextOption = cloneValue(option)
  merged.form = {
    ...(merged.form || {}),
    ...(nextOption.form || {}),
    labelCol: {
      ...((merged.form as Record<string, any>)?.labelCol || {}),
      ...((nextOption.form as Record<string, any>)?.labelCol || {}),
    },
    wrapperCol: {
      ...((merged.form as Record<string, any>)?.wrapperCol || {}),
      ...((nextOption.form as Record<string, any>)?.wrapperCol || {}),
    },
  }
  merged.row = {
    ...(merged.row || {}),
    ...(nextOption.row || {}),
  }

  return {
    ...merged,
    ...nextOption,
    form: merged.form,
    row: merged.row,
  }
}

function toFormConfig(option: FormCreateOption): LowCodeFormConfig {
  const form = (option?.form || {}) as Record<string, any>
  const labelStyle = (form.labelCol?.style || {}) as Record<string, any>

  return {
    layout: (form.layout as LowCodeFormConfig['layout']) || 'vertical',
    labelAlign: (form.labelAlign as LowCodeFormConfig['labelAlign']) || 'right',
    labelWidth: parseWidth(labelStyle.width),
    size: (form.size as LowCodeFormConfig['size']) || 'middle',
  }
}

function legacyFormOption(formConfig?: LowCodeFormConfig): FormCreateOption {
  const config = {
    layout: formConfig?.layout || 'vertical',
    labelAlign: formConfig?.labelAlign || 'right',
    labelWidth: formConfig?.labelWidth || 120,
    size: formConfig?.size || 'middle',
  }

  return mergeFormOption({
    form: {
      layout: config.layout,
      labelAlign: config.labelAlign,
      size: config.size,
      colon: false,
      labelCol: {
        style: {
          width: `${config.labelWidth}px`,
        },
      },
      wrapperCol: {
        span: 24,
      },
    },
    row: {
      gutter: 16,
    },
    submitBtn: false,
    resetBtn: false,
  })
}

function normalizeRuleProps(rule: Record<string, any>) {
  const props = (rule.props || {}) as Record<string, any>
  const attrs = (rule.attrs || {}) as Record<string, any>
  return {
    ...attrs,
    ...props,
  }
}

function normalizeOptions(options: unknown): LowCodeFieldOption[] | undefined {
  if (!Array.isArray(options)) {
    return undefined
  }

  const normalized = options
    .map(option => {
      const item = option as Record<string, any>
      return {
        label: String(item?.label || item?.title || item?.name || '').trim(),
        value: String(item?.value ?? item?.label ?? item?.title ?? '').trim(),
      }
    })
    .filter(option => option.label && option.value)

  return normalized.length ? normalized : undefined
}

function normalizeComponent(value: unknown): LowCodeFieldComponent {
  const source = String(value || 'input').trim()

  if (source === 'textarea') return 'textarea'
  if (source === 'inputNumber' || source === 'number') return 'number'
  if (source === 'datePicker' || source === 'rangePicker' || source === 'timePicker' || source === 'timeRangePicker' || source === 'date') return 'date'
  if (source === 'checkbox') return 'checkbox'
  if (source === 'radio') return 'radio'
  if (source === 'switch') return 'switch'
  if (source === 'select' || source === 'treeSelect' || source === 'cascader' || source === 'autoComplete') return 'select'
  if (source === 'upload' || source === 'frame') return 'upload'
  if (source === 'divider') return 'divider'
  if (source === 'row' || source === 'col' || source === 'grid') return 'grid'

  return supportedComponents.includes(source as LowCodeFieldComponent) ? (source as LowCodeFieldComponent) : 'input'
}

function legacyComponentToRuleType(component: LowCodeFieldComponent) {
  if (component === 'number') return 'inputNumber'
  if (component === 'date') return 'datePicker'
  return component
}

function defaultSpan(component: LowCodeFieldComponent) {
  return component === 'textarea' || component === 'upload' || component === 'divider' ? 24 : 12
}

function normalizeFieldKey(value: string) {
  return value
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
}

function parseWidth(value: unknown) {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string') {
    const match = value.match(/(\d+(\.\d+)?)/)
    if (match) {
      return Number(match[1])
    }
  }

  return 120
}

function numberOrUndefined(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return undefined
  }
  const number = Number(value)
  return Number.isFinite(number) ? number : undefined
}

function stringOrUndefined(value: unknown) {
  const text = String(value ?? '').trim()
  return text || undefined
}

function compactObject<T extends Record<string, any>>(value: T) {
  const nextValue: Record<string, any> = {}
  Object.keys(value).forEach(key => {
    if (value[key] !== undefined && value[key] !== '') {
      nextValue[key] = value[key]
    }
  })
  return nextValue
}

function cloneSchema(schema: LowCodeFormSchema): LowCodeFormSchema {
  return cloneValue(schema)
}

function cloneValue<T>(value: T): T {
  return JSON.parse(JSON.stringify(value))
}
