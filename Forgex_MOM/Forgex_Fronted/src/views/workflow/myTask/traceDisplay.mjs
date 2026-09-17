/**
 * 审批轨迹等待时长展示口径。
 *
 * 后端只下发起点时间，前端按本地时钟刷新「已等待 / 剩余 / 已超时」，
 * 避免详情弹窗停留期间数字不再变化。
 *
 * 主口径 A：now - currentWaitStartTime（当前节点最新 execution_detail.create_time）。
 * 辅口径 B：waitingSinceTime（该审批人 pending 的 wf_my_task.create_time）。
 */

const PENDING_STATUS = 0
const MINUTE_MS = 60 * 1000
const HOUR_MS = 60 * MINUTE_MS
const DAY_MS = 24 * HOUR_MS

/**
 * 判断审批实例是否为当前激活待办。
 *
 * @param {object} [instance] 审批实例
 * @returns {boolean} 是否正在等待
 */
export function isWaitingInstance(instance) {
  if (!instance) {
    return false
  }
  return instance.status === PENDING_STATUS && instance.activated !== false
}

/**
 * 解析毫秒时间戳。非法值返回 NaN。
 *
 * @param {string|number|Date} [value] 时间
 * @returns {number} 毫秒时间戳
 */
export function parseWaitTimestamp(value) {
  if (value == null || value === '') {
    return Number.NaN
  }
  const timestamp = new Date(value).getTime()
  return Number.isNaN(timestamp) ? Number.NaN : timestamp
}

/**
 * 拆分时长为天/小时/分钟。不足 1 分钟单独标记。
 *
 * @param {number} durationMs 时长毫秒
 * @returns {{ lessThanMinute: boolean, days?: number, hours?: number, minutes?: number }}
 */
export function splitDuration(durationMs) {
  if (!Number.isFinite(durationMs) || durationMs < MINUTE_MS) {
    return { lessThanMinute: true }
  }
  const days = Math.floor(durationMs / DAY_MS)
  const hours = Math.floor((durationMs % DAY_MS) / HOUR_MS)
  const minutes = Math.floor((durationMs % HOUR_MS) / MINUTE_MS)
  return { lessThanMinute: false, days, hours, minutes }
}

/**
 * 将拆分后的时长格式化为「X天 X小时 X分钟」。
 *
 * @param {{ lessThanMinute: boolean, days?: number, hours?: number, minutes?: number }} parts 时长部件
 * @param {(key: string, params?: Record<string, unknown>) => string} t i18n 函数
 * @returns {string} 可读时长
 */
export function formatDurationParts(parts, t) {
  if (!parts || parts.lessThanMinute) {
    return t('workflow.myTask.duration.lessThanMinute')
  }
  const tokens = []
  if (parts.days) {
    tokens.push(t('workflow.myTask.duration.day', { n: parts.days }))
  }
  if (parts.hours) {
    tokens.push(t('workflow.myTask.duration.hour', { n: parts.hours }))
  }
  if (parts.minutes) {
    tokens.push(t('workflow.myTask.duration.minute', { n: parts.minutes }))
  }
  return tokens.length ? tokens.join(' ') : t('workflow.myTask.duration.lessThanMinute')
}

/**
 * 格式化已等待时长。起点无效返回 `-`，未来时刻按不足 1 分钟处理。
 *
 * @param {string} [startTime] 等待起点
 * @param {number} nowMs 当前本地时间
 * @param {(key: string, params?: Record<string, unknown>) => string} t i18n 函数
 * @returns {string} 已等待文案
 */
export function formatWaitDuration(startTime, nowMs, t) {
  const startMs = parseWaitTimestamp(startTime)
  if (Number.isNaN(startMs)) {
    return '-'
  }
  return formatDurationParts(splitDuration(nowMs - startMs), t)
}

/**
 * 格式化剩余或已超时。未配置截止时间时返回空，由调用方决定是否展示。
 *
 * @param {string} [deadlineTime] 截止时间
 * @param {number} nowMs 当前本地时间
 * @param {(key: string, params?: Record<string, unknown>) => string} t i18n 函数
 * @returns {{ overdue: boolean, text: string } | null}
 */
export function formatRemainOrOverdue(deadlineTime, nowMs, t) {
  const deadlineMs = parseWaitTimestamp(deadlineTime)
  if (Number.isNaN(deadlineMs)) {
    return null
  }
  if (deadlineMs >= nowMs) {
    return {
      overdue: false,
      text: t('workflow.myTask.remaining', {
        duration: formatDurationParts(splitDuration(deadlineMs - nowMs), t),
      }),
    }
  }
  return {
    overdue: true,
    text: t('workflow.myTask.overdue', {
      duration: formatDurationParts(splitDuration(nowMs - deadlineMs), t),
    }),
  }
}

/**
 * 解析节点等待起点：优先执行单 currentWaitStartTime，缺失时回退激活待办 waitingSinceTime。
 *
 * @param {object} [record] 执行单
 * @param {object[]} [instances] 审批实例
 * @returns {string | undefined} 等待起点
 */
export function resolveWaitStartTime(record, instances) {
  if (record?.currentWaitStartTime) {
    return record.currentWaitStartTime
  }
  const waiting = (instances || []).filter(isWaitingInstance)
    .map(item => item.waitingSinceTime)
    .filter(Boolean)
    .sort()
  return waiting[0]
}

/**
 * 解析当前激活待办最早截止时间。
 *
 * @param {object} [record] 执行单
 * @param {object[]} [instances] 审批实例
 * @returns {string | undefined} 截止时间
 */
export function resolveDeadlineTime(record, instances) {
  if (record?.currentDeadlineTime) {
    return record.currentDeadlineTime
  }
  const deadlines = (instances || []).filter(isWaitingInstance)
    .map(item => item.deadlineTime)
    .filter(Boolean)
    .sort()
  return deadlines[0]
}

/**
 * 汇总当前等待人姓名，会签多人用顿号拼接，不拆成多条轨迹。
 *
 * @param {object[]} [instances] 审批实例
 * @returns {string} 等待人姓名
 */
export function resolveWaitingNames(instances) {
  return (instances || [])
    .filter(isWaitingInstance)
    .map(item => item.approverName)
    .filter(Boolean)
    .join('、')
}
