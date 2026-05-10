#!/usr/bin/env node
import { readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = dirname(dirname(fileURLToPath(import.meta.url)))
const viewsDir = join(root, 'src/views')

function walk(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    const stat = statSync(full)
    if (stat.isDirectory()) walk(full, out)
    else if (/\.(vue|ts)$/.test(name)) out.push(full)
  }
  return out
}

function removeBalancedConst(source, name) {
  const re = new RegExp(`\\nconst\\s+${name}\\b[^=]*=\\s*`)
  const match = re.exec(source)
  if (!match) return source
  const start = match.index
  let i = match.index + match[0].length
  while (/\s/.test(source[i] || '')) i += 1
  const first = source[i]
  let depth = 0
  let inString = ''
  let inLineComment = false
  let inBlockComment = false
  let end = i
  for (; end < source.length; end += 1) {
    const ch = source[end]
    const next = source[end + 1]
    if (inLineComment) {
      if (ch === '\n') inLineComment = false
      continue
    }
    if (inBlockComment) {
      if (ch === '*' && next === '/') {
        inBlockComment = false
        end += 1
      }
      continue
    }
    if (inString) {
      if (ch === '\\') {
        end += 1
        continue
      }
      if (ch === inString) inString = ''
      continue
    }
    if (ch === '/' && next === '/') {
      inLineComment = true
      end += 1
      continue
    }
    if (ch === '/' && next === '*') {
      inBlockComment = true
      end += 1
      continue
    }
    if (ch === "'" || ch === '"' || ch === '`') {
      inString = ch
      continue
    }
    if (ch === '(' || ch === '{' || ch === '[') depth += 1
    if (ch === ')' || ch === '}' || ch === ']') depth -= 1
    if (depth <= 0 && end > i && (ch === '\n' || ch === ';')) {
      if (first === '(' || first === '{' || first === '[' || depth === 0) {
        break
      }
    }
  }
  return `${source.slice(0, start)}${source.slice(end)}`
}

function fixBrokenTranslateImport(source) {
  let next = source.replace(/import \{\s*import \{ translateLegacyText \} from '@\/utils\/legacyI18n'\s*/g, "import { translateLegacyText } from '@/utils/legacyI18n'\nimport {\n")
  next = next.replace(/import type \{\s*import \{ translateLegacyText \} from '@\/utils\/legacyI18n'\s*/g, "import { translateLegacyText } from '@/utils/legacyI18n'\nimport type {\n")
  return next
}

let changed = 0
for (const file of walk(viewsDir)) {
  let source = readFileSync(file, 'utf8')
  let next = fixBrokenTranslateImport(source)
  next = next
    .replace(/\s+:fallback-config="[^"]*"/g, '')
    .replace(/\s+:dynamic-table-config="[^"]*"/g, '')
  for (const name of ['tableFallbackConfig', 'fallbackConfig', 'dynamicTableConfig']) {
    next = removeBalancedConst(next, name)
  }
  next = next
    .replace(/\nimport type \{ FxTableConfig \} from '@\/api\/system\/tableConfig'/g, '')
    .replace(/\nimport \{ translateLegacyText \} from '@\/utils\/legacyI18n'(?=\n(?:import|const|function|type|interface|enum))/g, match => {
      return next.includes('translateLegacyText(') ? match : ''
    })
  if (next !== source) {
    writeFileSync(file, next, 'utf8')
    changed += 1
  }
}

console.log(JSON.stringify({ changed }, null, 2))
