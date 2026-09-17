import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const viewSource = await readFile(new URL('../src/views/system/dict/index.vue', import.meta.url), 'utf8')
const apiSource = await readFile(new URL('../src/api/system/dict.ts', import.meta.url), 'utf8')

test('dictionary status radio values are normalized to numbers for edit echo', () => {
  assert.match(viewSource, /normalizeStatusValue\(option\.value\)/)
  assert.match(viewSource, /const statusOptions = computed\(/)
})

test('dictionary management exposes a cache refresh action', () => {
  assert.match(viewSource, /刷新缓存/)
  assert.match(viewSource, /clearDictCache\(\)/)
  assert.match(apiSource, /refreshDictCache/)
})
