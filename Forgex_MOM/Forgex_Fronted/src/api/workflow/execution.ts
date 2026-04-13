/**
 * 宸ヤ綔娴?- 瀹℃壒鎵ц API
 */
import http from '../http'

/**
 * 瀹℃壒鎵ц DTO
 */
export interface WfExecutionDTO {
  /**
   * 涓婚敭
   */
  id: number

  /**
   * 瀹℃壒浠诲姟閰嶇疆琛?ID
   */
  taskConfigId: number

  /**
   * 瀹℃壒浠诲姟缂栫爜
   */
  taskCode: string

  /**
   * 瀹℃壒浠诲姟鍚嶇О
   */
  taskName: string

  /**
   * 鍙戣捣浜?ID
   */
  initiatorId: number

  /**
   * 鍙戣捣浜哄悕绉?
   */
  initiatorName: string

  /**
   * 褰撳墠鑺傜偣 ID
   */
  currentNodeId?: number

  /**
   * 褰撳墠鑺傜偣鍚嶇О
   */
  currentNodeName?: string

  /**
   * 琛ㄥ崟鍐呭锛圝SON锛?
   */
  formContent: string

  /**
   * 鍙戣捣鏃堕棿
   */
  startTime: string

  /**
   * 缁撴潫鏃堕棿
   */
  endTime?: string

  /**
   * 鐘舵€?
   * 0=鏈鎵?
   * 1=瀹℃壒涓?
   * 2=瀹℃壒瀹屾垚
   * 3=椹冲洖
   */
  status: number

  /**
   * 鐘舵€佹爣绛?
   */
  statusDesc?: string

  /**
   * 绉熸埛 ID
   */
  tenantId: number

  /**
   * 鍒涘缓鏃堕棿
   */
  createTime: string

  /**
   * 鏇存柊鏃堕棿
   */
  updateTime?: string
}

/**
 * 鍙戣捣瀹℃壒鍙傛暟
 */
export interface WfExecutionStartParam {
  /**
   * 瀹℃壒浠诲姟缂栫爜
   */
  taskCode: string

  /**
   * 琛ㄥ崟鍐呭锛圝SON锛?
   */
  formContent: string
}

/**
 * 瀹℃壒澶勭悊鍙傛暟
 */
export interface WfExecutionApproveParam {
  /**
   * 瀹℃壒鎵ц ID
   */
  executionId: number

  /**
   * 瀹℃壒鐘舵€?
   * 1=鍚屾剰
   * 2=涓嶅悓鎰?
   */
  approve状态: number

  /**
   * 瀹℃壒鎰忚
   */
  comment?: string

  /**
   * 椹冲洖绫诲瀷
   * 1=椹冲洖浠诲姟
   * 2=杩斿洖涓婁竴鑺傜偣
   * 锛堜笉鍚屾剰鏃跺繀濉級
   */
  rejectType?: number
}

/**
 * 瀹℃壒鎵ц鏌ヨ鍙傛暟
 */
export interface WfExecutionQueryParam {
  /**
   * 椤电爜
   */
  pageNum?: number

  /**
   * 姣忛〉鏉℃暟
   */
  pageSize?: number

  /**
   * 浠诲姟缂栫爜锛堟ā绯婃煡璇級
   */
  taskCode?: string

  /**
   * 浠诲姟鍚嶇О锛堟ā绯婃煡璇級
   */
  taskName?: string

  /**
   * 鐘舵€?
   * 0=鏈鎵?
   * 1=瀹℃壒涓?
   * 2=瀹℃壒瀹屾垚
   * 3=椹冲洖
   */
  status?: number

  /**
   * 鍙戣捣浜?ID
   */
  initiatorId?: number

  /**
   * 褰撳墠瀹℃壒浜?ID
   */
  currentApproverId?: number

  /**
   * 鎴戝凡澶勭悊锛氬鎵规搷浣滄椂闂翠笅闄愶紙鍚級锛屾牸寮?yyyy-MM-dd HH:mm:ss
   */
  approveTimeBegin?: string

  /**
   * 鎴戝凡澶勭悊锛氬鎵规搷浣滄椂闂翠笂闄愶紙鍚級
   */
  approveTimeEnd?: string
}

/**
 * 瀹℃壒宸ヤ綔鍙伴椤垫眹鎬?
 */
export interface WfDashboardSummaryVO {
  /**
   * 寰呮垜澶勭悊
   */
  pending: WfExecutionDTO[]

  /**
   * 鏄ㄦ棩鎴戝凡澶勭悊
   */
  yesterdayProcessed: WfExecutionDTO[]

  /**
   * 鎶勯€佸緟闃?
   */
  cc: WfExecutionDTO[]
}

/**
 * 瀹℃壒宸ヤ綔鍙拌繎 7 鏃ュ鎵圭粨鏋滆秼鍔块」
 */
export interface WfDashboardWeeklyResultDTO {
  /**
   * 鏃ユ湡锛屾牸寮?yyyy-MM-dd
   */
  date: string

  /**
   * 瀹℃壒閫氳繃鏁伴噺
   */
  approvedCount: number

  /**
   * 椹冲洖鏁伴噺
   */
  rejectedCount: number
}

/**
 * 瀹℃壒宸ヤ綔鍙扮敤鎴峰鎵瑰崰姣旈」
 */
export interface WfDashboardUserShareDTO {
  /**
   * 鍙戣捣浜?ID
   */
  initiatorId: number

  /**
   * 鍙戣捣浜哄悕绉?
   */
  initiatorName: string

  /**
   * 瀹℃壒鍗曟暟閲?
   */
  count: number
}

/**
 * 瀹℃壒宸ヤ綔鍙板浘琛ㄧ粺璁℃暟鎹?
 */
export interface WfDashboardAnalyticsVO {
  /**
   * 杩?7 鏃ュ鎵圭粨鏋滆秼鍔?
   */
  weeklyResults: WfDashboardWeeklyResultDTO[]

  /**
   * 鐢ㄦ埛瀹℃壒鏁伴噺鍗犳瘮
   */
  userShares: WfDashboardUserShareDTO[]
}

/**
 * 鍙戣捣瀹℃壒
 * @param params 鍙戣捣瀹℃壒鍙傛暟
 * @returns 鎵ц ID
 */
export function startExecution(params: WfExecutionStartParam) {
  return http.post<number>('/wf/execution/start', params)
}

/**
 * 瀹℃壒鍚屾剰
 * @param params 瀹℃壒鍙傛暟
 * @returns 鏄惁鎴愬姛
 */
export function approve(params: WfExecutionApproveParam) {
  return http.post<boolean>('/wf/execution/approve', params)
}

/**
 * 瀹℃壒椹冲洖
 * @param params 瀹℃壒鍙傛暟
 * @returns 鏄惁鎴愬姛
 */
export function reject(params: WfExecutionApproveParam) {
  return http.post<boolean>('/wf/execution/reject', params)
}

/**
 * 鎾ら攢瀹℃壒
 * @param params 鍙傛暟锛坋xecutionId锛?
 * @returns 鏄惁鎴愬姛
 */
export function cancelExecution(params: { executionId: number }) {
  return http.post<boolean>('/wf/execution/cancel', params)
}

/**
 * 鑾峰彇瀹℃壒璇︽儏
 * @param params 鍙傛暟锛坋xecutionId锛?
 * @returns 瀹℃壒璇︽儏
 */
export function getExecutionDetail(params: { executionId: number }) {
  return http.post<WfExecutionDTO>('/wf/execution/detail', params)
}

/**
 * 鍒嗛〉鏌ヨ鎴戝彂璧风殑瀹℃壒
 * @param params 鏌ヨ鍙傛暟
 * @returns 鍒嗛〉缁撴灉
 */
export function pageMyInitiated(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/initiated', params)
}

/**
 * 鍒嗛〉鏌ヨ鎴戝緟澶勭悊鐨勫鎵?
 * @param params 鏌ヨ鍙傛暟
 * @returns 鍒嗛〉缁撴灉
 */
export function pageMyPending(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/pending', params)
}

/**
 * 鍒嗛〉鏌ヨ鎴戝凡澶勭悊鐨勫鎵?
 * @param params 鏌ヨ鍙傛暟
 * @returns 鍒嗛〉缁撴灉
 */
export function pageMyProcessed(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/processed', params)
}

/**
 * 鍒嗛〉鏌ヨ鎶勯€佺粰鎴戠殑寰呴槄浠诲姟
 * @param params 鏌ヨ鍙傛暟
 * @returns 鍒嗛〉缁撴灉
 */
export function pageMyCc(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/cc', params)
}

/**
 * 瀹℃壒宸ヤ綔鍙伴椤垫眹鎬伙紙寰呭姙銆佹槰鏃ュ凡澶勭悊銆佹妱閫佸緟闃咃級
 * @returns 姹囨€绘暟鎹?
 */
export function loadDashboardSummary() {
  return http.post<WfDashboardSummaryVO>('/wf/execution/dashboard/summary', {}, { silentError: true } as any)
}

/**
 * 瀹℃壒宸ヤ綔鍙板浘琛ㄧ粺璁℃暟鎹?
 * @returns 杩?7 鏃ュ鎵硅秼鍔夸笌鐢ㄦ埛瀹℃壒鍗犳瘮
 */
export function loadDashboardAnalytics() {
  return http.post<WfDashboardAnalyticsVO>('/wf/execution/dashboard/analytics', {}, { silentError: true } as any)
}
