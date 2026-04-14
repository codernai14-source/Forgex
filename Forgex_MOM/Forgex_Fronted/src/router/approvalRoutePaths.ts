export const APPROVAL_ROUTE_BASE = '/workspace/approval'
export const LEGACY_APPROVAL_ROUTE_BASE = '/workflow'
export const TAB_CLOSE_QUERY_KEY = '__closeTab'

function encodeTaskCode(taskCode: string) {
  return encodeURIComponent(String(taskCode || '').trim())
}

export const approvalRoutePaths = {
  dashboard: `${APPROVAL_ROUTE_BASE}/dashboard`,
  taskConfigList: `${APPROVAL_ROUTE_BASE}/taskConfig`,
  taskConfigNodes: (taskCode: string) => `${APPROVAL_ROUTE_BASE}/taskConfig/${encodeTaskCode(taskCode)}/nodes`,
  executionStartList: `${APPROVAL_ROUTE_BASE}/execution/start`,
  executionStartForm: (taskCode: string) => `${APPROVAL_ROUTE_BASE}/execution/start/${encodeTaskCode(taskCode)}`,
  executionStart表单: (taskCode: string) => `${APPROVAL_ROUTE_BASE}/execution/start/${encodeTaskCode(taskCode)}`,
  myPending: `${APPROVAL_ROUTE_BASE}/my/pending`,
  myProcessed: `${APPROVAL_ROUTE_BASE}/my/processed`,
  myInitiated: `${APPROVAL_ROUTE_BASE}/my/initiated`,
}

export function normalizeApprovalRoutePath(path: unknown, fallback = approvalRoutePaths.taskConfigList) {
  const normalized = String(path || '').trim()
  if (!normalized) {
    return fallback
  }
  if (normalized === LEGACY_APPROVAL_ROUTE_BASE) {
    return approvalRoutePaths.taskConfigList
  }
  if (normalized.startsWith(`${LEGACY_APPROVAL_ROUTE_BASE}/`)) {
    return `${APPROVAL_ROUTE_BASE}${normalized.slice(LEGACY_APPROVAL_ROUTE_BASE.length)}`
  }
  return normalized
}
