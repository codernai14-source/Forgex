type LocaleTree = Record<string, any>

export function mergeWithFallback<T extends LocaleTree>(fallback: T, current: LocaleTree): T {
  const result: LocaleTree = Array.isArray(fallback) ? [...fallback] : { ...fallback }

  for (const [key, value] of Object.entries(current || {})) {
    if (
      value &&
      typeof value === 'object' &&
      !Array.isArray(value) &&
      fallback?.[key] &&
      typeof fallback[key] === 'object' &&
      !Array.isArray(fallback[key])
    ) {
      result[key] = mergeWithFallback(fallback[key], value)
    } else {
      result[key] = value
    }
  }

  return result as T
}
