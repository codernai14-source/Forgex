import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = file => fs.readFileSync(path.join(root, file), 'utf8')

test('system config exposes browser tab title and icon fields', () => {
  const source = read('src/api/system/config.ts')
  assert.match(source, /browserTitle:\s*string/)
  assert.match(source, /browserIcon:\s*string/)
  assert.match(source, /browserTitle:\s*'FORGEX_MOM'/)
})

test('icon picker exposes color and size controls', () => {
  const source = read('src/components/common/IconPicker.vue')
  assert.match(source, /iconColor/)
  assert.match(source, /iconSize/)
  assert.match(source, /type="color"/)
  assert.match(source, /a-input-number/)
})

test('incoming message notification composes timely speech text', () => {
  const source = read('src/utils/messageSpeech.ts')
  assert.match(source, /speechSynthesis/)
  assert.match(source, /请及时处理|timely|process/i)
  assert.match(source, /title.*content/s)
})

test('header provides a speaker control', () => {
  const source = read('src/layouts/components/AppHeader.vue')
  assert.match(source, /SoundOutlined|AudioOutlined|speaker/i)
  assert.match(source, /speech|播报/i)
})
