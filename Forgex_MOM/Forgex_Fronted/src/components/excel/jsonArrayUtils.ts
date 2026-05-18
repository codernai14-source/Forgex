export interface JsonArrayEditorOption {
  value: string
  label: string
  _key: string
}

export interface ParsedJsonArrayResult {
  raw: string
  valid: boolean
  options: JsonArrayEditorOption[]
  serializedJson: string
  formattedJson: string
}

function normalizeString(value: unknown) {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value)
}

export function createJsonArrayOption(partial?: Partial<JsonArrayEditorOption>): JsonArrayEditorOption {
  return {
    value: normalizeString(partial?.value),
    label: normalizeString(partial?.label),
    _key: partial?._key || `json-option-${Date.now()}-${Math.random()}`,
  }
}

export function serializeJsonArrayOptions(options: Array<Partial<JsonArrayEditorOption>>) {
  const cleanOptions = options
    .map(item => ({
      value: normalizeString(item.value),
      label: normalizeString(item.label),
    }))
    .filter(item => item.value.trim() || item.label.trim())

  return JSON.stringify(cleanOptions)
}

export function parseJsonArrayValue(rawValue?: string | null): ParsedJsonArrayResult {
  const raw = String(rawValue || '').trim()
  if (!raw) {
    return {
      raw: '[]',
      valid: true,
      options: [],
      serializedJson: '[]',
      formattedJson: '[]',
    }
  }

  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return {
        raw,
        valid: false,
        options: [],
        serializedJson: '[]',
        formattedJson: raw,
      }
    }

    const options = parsed.map((item, index) => {
      if (item && typeof item === 'object' && !Array.isArray(item)) {
        return createJsonArrayOption({
          value: (item as Record<string, unknown>).value,
          label: (item as Record<string, unknown>).label,
          _key: `json-option-${index}-${Date.now()}`,
        })
      }

      return createJsonArrayOption({
        value: item,
        label: item,
        _key: `json-option-${index}-${Date.now()}`,
      })
    })

    const serializedJson = serializeJsonArrayOptions(options)
    return {
      raw,
      valid: true,
      options,
      serializedJson,
      formattedJson: JSON.stringify(JSON.parse(serializedJson), null, 2),
    }
  } catch (_) {
    return {
      raw,
      valid: false,
      options: [],
      serializedJson: '[]',
      formattedJson: raw,
    }
  }
}
