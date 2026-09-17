/**
 * 审批轨迹等待时长展示口径。
 *
 * @see ./traceDisplay.mjs
 */

export function isWaitingInstance(instance?: {
  status?: number
  activated?: boolean
}): boolean

export function parseWaitTimestamp(value?: string | number | Date): number

export function splitDuration(durationMs: number): {
  lessThanMinute: boolean
  days?: number
  hours?: number
  minutes?: number
}

export function formatDurationParts(
  parts: { lessThanMinute: boolean; days?: number; hours?: number; minutes?: number },
  t: (key: string, params?: Record<string, unknown>) => string,
): string

export function formatWaitDuration(
  startTime: string | undefined,
  nowMs: number,
  t: (key: string, params?: Record<string, unknown>) => string,
): string

export function formatRemainOrOverdue(
  deadlineTime: string | undefined,
  nowMs: number,
  t: (key: string, params?: Record<string, unknown>) => string,
): { overdue: boolean; text: string } | null

export function resolveWaitStartTime(
  record?: { currentWaitStartTime?: string } | null,
  instances?: Array<{ status?: number; activated?: boolean; waitingSinceTime?: string }>,
): string | undefined

export function resolveDeadlineTime(
  record?: { currentDeadlineTime?: string } | null,
  instances?: Array<{ status?: number; activated?: boolean; deadlineTime?: string }>,
): string | undefined

export function resolveWaitingNames(
  instances?: Array<{ status?: number; activated?: boolean; approverName?: string }>,
): string
