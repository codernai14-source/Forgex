import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const appSource = await readFile(new URL('../src/App.vue', import.meta.url), 'utf8')
const layoutLocaleSource = await readFile(new URL('../src/locales/zh-CN/layout.ts', import.meta.url), 'utf8')

test('tenant transition reuses the animated bootstrap loading shell', () => {
  assert.match(appSource, /class="fx-runtime-loading fx-bootstrap-shell"/)
  assert.match(appSource, /fx-bootstrap-shell__ring/)
  assert.match(appSource, /fx-bootstrap-shell__text-highlight/)
})

test('tenant transition shows the requested loading message', () => {
  assert.match(appSource, /t\('layout\.loading\.thinking'\)/)
  assert.match(layoutLocaleSource, /thinking:\s*'Forgex正在加载您所需的内容'/)
})
