import test from 'node:test'
import assert from 'node:assert/strict'

import {
  formatRemainOrOverdue,
  formatWaitDuration,
  isWaitingInstance,
  resolveDeadlineTime,
  resolveWaitStartTime,
  resolveWaitingNames,
  splitDuration,
} from './traceDisplay.mjs'

function t(key, params = {}) {
  const dict = {
    'workflow.myTask.duration.day': `${params.n}天`,
    'workflow.myTask.duration.hour': `${params.n}小时`,
    'workflow.myTask.duration.minute': `${params.n}分钟`,
    'workflow.myTask.duration.lessThanMinute': '不足1分钟',
    'workflow.myTask.remaining': `剩余 ${params.duration}`,
    'workflow.myTask.overdue': `已超时 ${params.duration}`,
  }
  return dict[key] || key
}

test('splitDuration formats day hour minute and less than one minute', () => {
  assert.deepEqual(splitDuration(30 * 1000), { lessThanMinute: true })
  assert.deepEqual(splitDuration((19 * 24 * 60 + 3 * 60 + 12) * 60 * 1000), {
    lessThanMinute: false,
    days: 19,
    hours: 3,
    minutes: 12,
  })
})

test('formatWaitDuration uses node wait start instead of whole-flow start', () => {
  const now = Date.UTC(2026, 8, 11, 10, 0, 0)
  const nodeArrive = new Date(now - (50 * 24 + 3) * 60 * 60 * 1000).toISOString()
  const flowStart = new Date(now - (133 * 24) * 60 * 60 * 1000).toISOString()
  const waited = formatWaitDuration(nodeArrive, now, t)
  const wholeFlow = formatWaitDuration(flowStart, now, t)

  assert.equal(waited, '50天 3小时')
  assert.notEqual(waited, wholeFlow)
})

test('formatWaitDuration returns dash for invalid start and less than a minute for future', () => {
  const now = Date.UTC(2026, 8, 11, 10, 0, 0)
  assert.equal(formatWaitDuration('', now, t), '-')
  assert.equal(formatWaitDuration(new Date(now + 30 * 1000).toISOString(), now, t), '不足1分钟')
})

test('formatRemainOrOverdue stays empty without deadline and marks overdue in red path', () => {
  const now = Date.UTC(2026, 8, 11, 10, 0, 0)
  assert.equal(formatRemainOrOverdue(undefined, now, t), null)
  assert.deepEqual(formatRemainOrOverdue(new Date(now + 5 * 60 * 60 * 1000).toISOString(), now, t), {
    overdue: false,
    text: '剩余 5小时',
  })
  assert.deepEqual(formatRemainOrOverdue(new Date(now - 2 * 24 * 60 * 60 * 1000).toISOString(), now, t), {
    overdue: true,
    text: '已超时 2天',
  })
})

test('inactive sequential instance does not count as waiting', () => {
  assert.equal(isWaitingInstance({ status: 0, activated: false }), false)
  assert.equal(isWaitingInstance({ status: 0, activated: true }), true)
  assert.equal(isWaitingInstance({ status: 1, activated: true }), false)
})

test('resolveWaitStartTime prefers execution currentWaitStartTime over instance createTime', () => {
  const record = { currentWaitStartTime: '2026-08-01 09:00:00' }
  const instances = [
    { status: 0, activated: true, waitingSinceTime: '2026-08-10 09:00:00', createTime: '2026-07-01 08:00:00' },
  ]
  assert.equal(resolveWaitStartTime(record, instances), '2026-08-01 09:00:00')
  assert.equal(resolveWaitStartTime(null, instances), '2026-08-10 09:00:00')
})

test('resolveDeadlineTime and waiting names only use activated pending instances', () => {
  const instances = [
    { status: 0, activated: true, approverName: '张经理', deadlineTime: '2026-09-12 10:00:00' },
    { status: 0, activated: false, approverName: '李主管', deadlineTime: '2026-09-01 10:00:00' },
  ]
  assert.equal(resolveDeadlineTime(null, instances), '2026-09-12 10:00:00')
  assert.equal(resolveWaitingNames(instances), '张经理')
})
