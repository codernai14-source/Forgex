import test from 'node:test'
import assert from 'node:assert/strict'

import {
  idsEqual,
  resolvePendingActions,
  resolveReceiverId,
} from './pendingActionModel.mjs'

const allPermissions = new Set([
  'wf:execution:approve',
  'wf:execution:reject',
  'wf:execution:addSign',
  'wf:execution:transfer',
  'wf:execution:delegate',
])

test('resolvePendingActions always follows approve and reject permissions', () => {
  const actions = resolvePendingActions({}, permission => allPermissions.has(permission))

  assert.deepEqual(actions, ['approve', 'reject'])
})

test('resolvePendingActions exposes configured node actions', () => {
  const actions = resolvePendingActions({
    allowAddSign: true,
    allowTransfer: false,
    allowDelegate: true,
  }, permission => allPermissions.has(permission))

  assert.deepEqual(actions, ['approve', 'reject', 'addSign', 'delegate'])
})

test('resolvePendingActions hides node actions without button permission', () => {
  const permissions = new Set(['wf:execution:approve', 'wf:execution:transfer'])
  const actions = resolvePendingActions({
    allowAddSign: true,
    allowTransfer: true,
    allowDelegate: true,
  }, permission => permissions.has(permission))

  assert.deepEqual(actions, ['approve', 'transfer'])
})

test('idsEqual matches a Long identifier without converting it to Number', () => {
  const longId = '1993479637244170253'

  assert.equal(idsEqual(longId, longId), true)
  assert.equal(idsEqual(longId, '1993479637244170254'), false)
})

test('resolveReceiverId preserves a Long identifier as the original string', () => {
  const longId = '1993479637244170253'

  assert.equal(resolveReceiverId([longId]), longId)
})
