/**
 * 工作流低代码表单 Schema 定义
 *
 * @author Forgex
 * @version 1.0
 * @since 2026-04-15
 */
export type LowCodeFieldComponent = 'input' | 'textarea' | 'number' | 'select' | 'date'

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
  defaultValue?: string | number
  options?: LowCodeFieldOption[]
}

export interface LowCodeFormSchema {
  version: string
  fields: LowCodeFieldSchema[]
}

export const DEFAULT_LOW_CODE_FORM_SCHEMA: LowCodeFormSchema = {
  version: '1.0.0',
  fields: []
}

export function normalizeLowCodeFormSchema(value?: string | null): LowCodeFormSchema {
  if (!value) {
    return { ...DEFAULT_LOW_CODE_FORM_SCHEMA, fields: [] }
  }

  try {
    const parsed = JSON.parse(value)
    if (Array.isArray(parsed)) {
      return {
        version: '1.0.0',
        fields: normalizeFields(parsed)
      }
    }

    return {
      version: typeof parsed?.version === 'string' ? parsed.version : '1.0.0',
      fields: normalizeFields(parsed?.fields)
    }
  } catch (error) {
    return { ...DEFAULT_LOW_CODE_FORM_SCHEMA, fields: [] }
  }
}

export function stringifyLowCodeFormSchema(schema: LowCodeFormSchema) {
  return JSON.stringify({
    version: schema.version || '1.0.0',
    fields: normalizeFields(schema.fields)
  })
}

export function extractLowCodeFieldOptions(raw: string) {
  return normalizeLowCodeFormSchema(raw).fields.map(field => ({
    key: field.key,
    label: field.label || field.key,
    component: field.component
  }))
}

function normalizeFields(fields: unknown): LowCodeFieldSchema[] {
  if (!Array.isArray(fields)) {
    return []
  }

  return fields
    .map(item => {
      const field = item as Record<string, any>
      const component = String(field?.component || 'input') as LowCodeFieldComponent
      const nextField: LowCodeFieldSchema = {
        key: String(field?.key || '').trim(),
        label: String(field?.label || '').trim(),
        component: ['input', 'textarea', 'number', 'select', 'date'].includes(component) ? component : 'input',
        required: Boolean(field?.required),
        placeholder: String(field?.placeholder || '').trim() || undefined,
        defaultValue: field?.defaultValue ?? undefined,
        options: Array.isArray(field?.options)
          ? field.options
              .map((option: Record<string, any>) => ({
                label: String(option?.label || '').trim(),
                value: String(option?.value || '').trim()
              }))
              .filter((option: LowCodeFieldOption) => option.label && option.value)
          : undefined
      }
      return nextField
    })
    .filter(field => field.key)
}
