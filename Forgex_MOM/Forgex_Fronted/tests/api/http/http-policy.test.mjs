import assert from 'node:assert/strict'
import test from 'node:test'
import {
  unwrapBusinessResponse,
  resolveTableSortHeaders,
  createActionDeduper,
} from '../../../src/api/httpPolicy.mts'

test('HTTP policy redirects only the documented session and password-expiry codes', () => {
  assert.deepEqual(unwrapBusinessResponse({ code: 602 }), { kind: 'login' })
  assert.deepEqual(unwrapBusinessResponse({ code: 606 }), { kind: 'password-expired' })
})

test('HTTP policy rejects business failures and unwraps successful data', () => {
  assert.deepEqual(unwrapBusinessResponse({ code: 500, message: 'failed' }), { kind: 'error', payload: { code: 500, message: 'failed' } })
  assert.deepEqual(unwrapBusinessResponse({ code: 200, data: { id: '1' } }), { kind: 'success', data: { id: '1' } })
})

test('HTTP policy converts table sorting fields into headers without leaking the table marker', () => {
  const payload = { __fxTableCode: 'SysUserTable', orderBy: 'createdAt', orderDirection: 'DESC' }

  assert.deepEqual(resolveTableSortHeaders(payload), {
    'X-Fx-Table-Code': 'SysUserTable',
    'X-Fx-Sort-Field': 'createdAt',
    'X-Fx-Sort-Order': 'DESC',
  })
  assert.equal('__fxTableCode' in payload, false)
})

test('HTTP policy shares an in-flight request for the same actionKey', async () => {
  const dedupe = createActionDeduper()
  let calls = 0
  const request = () => new Promise(resolve => {
    calls += 1
    setTimeout(() => resolve('saved'), 1)
  })

  const [first, second] = await Promise.all([
    dedupe.run('save-user', 'drop', request),
    dedupe.run('save-user', 'drop', request),
  ])

  assert.equal(first, 'saved')
  assert.equal(second, 'saved')
  assert.equal(calls, 1)
})
