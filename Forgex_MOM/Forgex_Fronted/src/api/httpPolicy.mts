export const loginReloadCodes = [602]
export const passwordExpiredCode = 606

export function unwrapBusinessResponse(payload: any) {
  if (payload?.code === passwordExpiredCode) {
    return { kind: 'password-expired' as const }
  }
  if (loginReloadCodes.includes(payload?.code)) {
    return { kind: 'login' as const }
  }
  if (payload?.code !== 200) {
    return { kind: 'error' as const, payload }
  }
  return { kind: 'success' as const, data: payload?.data }
}

export function resolveTableSortHeaders(payload: any): Record<string, string> {
  if (!payload || typeof payload !== 'object') {
    return {}
  }

  const tableCode = payload.__fxTableCode
  const sortField = payload.sortField || payload.orderBy
  const sortOrder = payload.sortOrder || payload.orderDirection
  if (Object.prototype.hasOwnProperty.call(payload, '__fxTableCode')) {
    delete payload.__fxTableCode
  }
  if (!tableCode || !sortField || !sortOrder) {
    return {}
  }
  return {
    'X-Fx-Table-Code': String(tableCode),
    'X-Fx-Sort-Field': String(sortField),
    'X-Fx-Sort-Order': String(sortOrder),
  }
}

export function createActionDeduper() {
  const pending = new Map<string, Promise<any>>()

  return {
    run<T>(key: string | undefined, mode: 'drop' | 'none' | undefined, request: () => Promise<T>): Promise<T> {
      if (!key || mode === 'none') {
        return request()
      }
      const running = pending.get(key)
      if (running) {
        return running as Promise<T>
      }
      const promise = request().finally(() => {
        if (pending.get(key) === promise) {
          pending.delete(key)
        }
      })
      pending.set(key, promise)
      return promise
    },
  }
}
