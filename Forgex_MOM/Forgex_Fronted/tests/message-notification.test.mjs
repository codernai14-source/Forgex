import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const source = await readFile(new URL('../src/components/Notification/MessageNotification.vue', import.meta.url), 'utf8')

test('custom message events use their detail payload without creating a duplicate notification', () => {
  assert.match(source, /function handleMessageReceivedEvent\(event: Event\)[\s\S]*?\.detail/)
  assert.match(source, /handleIncomingMessage\(detail, false\)/)
})

test('empty message payloads are ignored instead of rendering icon-only notifications', () => {
  assert.match(source, /if \(!message \|\| typeof message !== 'object'\) return/)
  assert.match(source, /if \(!title && !content\) return/)
})
