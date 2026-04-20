import { getLocale } from '@/locales'

interface PreserveLongOptions {
  scalarKeys?: string[]
  arrayKeys?: string[]
}

function quoteScalarLongs(raw: string, key: string) {
  const pattern = new RegExp(`("${key}"\\s*:\\s*)(-?\\d{16,})`, 'g')
  return raw.replace(pattern, '$1"$2"')
}

function quoteArrayLongs(raw: string, key: string) {
  const pattern = new RegExp(`("${key}"\\s*:\\s*\\[)([^\\]]*)(\\])`, 'g')
  return raw.replace(pattern, (_, prefix: string, body: string, suffix: string) => {
    const normalizedBody = body.replace(/(-?\d{16,})/g, '"$1"')
    return `${prefix}${normalizedBody}${suffix}`
  })
}

export function parseJsonPreservingLongs<T>(raw: string, options: PreserveLongOptions = {}): T {
  let normalized = raw

  for (const key of options.scalarKeys || []) {
    normalized = quoteScalarLongs(normalized, key)
  }

  for (const key of options.arrayKeys || []) {
    normalized = quoteArrayLongs(normalized, key)
  }

  return JSON.parse(normalized) as T
}

export function normalizeLongId(value: unknown): string {
  return String(value ?? '').trim()
}

export async function postJsonWithLongs<T>(
  url: string,
  payload: unknown,
  options: PreserveLongOptions = {},
): Promise<T> {
  const locale = getLocale()
  const tenantId = sessionStorage.getItem('tenantId')
  const response = await fetch(`/api${url}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Accept-Language': locale,
      'X-Lang': locale,
      ...(tenantId ? { 'X-Tenant-Id': tenantId } : {}),
    },
    body: JSON.stringify(payload ?? {}),
  })

  const raw = await response.text()
  const data = parseJsonPreservingLongs<any>(raw, options)

  if (!response.ok || data?.code !== 200) {
    const message = data?.message || data?.msg || 'Request failed'
    throw new Error(message)
  }

  return data.data as T
}
