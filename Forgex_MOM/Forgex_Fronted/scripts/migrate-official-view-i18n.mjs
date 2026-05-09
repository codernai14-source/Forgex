#!/usr/bin/env node
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { dirname, join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = dirname(dirname(fileURLToPath(import.meta.url)))
const srcDir = join(root, 'src')
const viewsDir = join(srcDir, 'views')
const ignoredViewPatterns = [
  /^_test\//,
  /^EnglishTest\.vue$/,
  /^ThemeTest\.vue$/,
]

const staticAttrs = [
  'label',
  'title',
  'placeholder',
  'description',
  'header',
  'message',
  'tab',
  'empty-text',
  'ok-text',
  'cancel-text',
  'checked-children',
  'un-checked-children',
  'module-name',
  'content',
  'tooltip',
  'aria-label',
  'alt',
]

function walk(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    const stat = statSync(full)
    if (stat.isDirectory()) {
      walk(full, out)
    } else if (name.endsWith('.vue') || name.endsWith('.ts')) {
      out.push(full)
    }
  }
  return out
}

function isOfficialView(file) {
  const rel = relative(viewsDir, file).replace(/\\/g, '/')
  return !ignoredViewPatterns.some(pattern => pattern.test(rel))
}

function escapeSingle(value) {
  return value.replace(/\\/g, '\\\\').replace(/'/g, "\\'")
}

function hasChinese(value) {
  return /[\u4e00-\u9fff]/.test(value)
}

function isWrappedCall(source, quoteStart, names) {
  const before = source.slice(0, quoteStart)
  return names.some(name => before.endsWith(`${name}(`) || before.endsWith(`${name}( `))
}

function replaceChineseSingleStrings(source, wrapper, namesToSkip = [wrapper]) {
  let out = ''
  for (let i = 0; i < source.length; i += 1) {
    const ch = source[i]
    if (ch !== "'") {
      out += ch
      continue
    }

    let j = i + 1
    let value = ''
    let closed = false
    while (j < source.length) {
      const current = source[j]
      if (current === '\\') {
        value += current + (source[j + 1] ?? '')
        j += 2
        continue
      }
      if (current === "'") {
        closed = true
        break
      }
      value += current
      j += 1
    }

    if (!closed) {
      out += ch
      continue
    }

    if (hasChinese(value) && !isWrappedCall(source, i, namesToSkip)) {
      out += `${wrapper}('${value}')`
    } else {
      out += `'${value}'`
    }
    i = j
  }
  return out
}

function transformTemplate(template) {
  let next = replaceChineseSingleStrings(template, '$tl', ['$tl', 'translateLegacyText'])

  for (const attr of staticAttrs) {
    const re = new RegExp(`(\\s)${attr}="([^"]*[\\u4e00-\\u9fff][^"]*)"`, 'g')
    next = next.replace(re, (_, space, value) => {
      if (value.includes('{{') || value.includes('$tl(')) {
        return `${space}${attr}="${value}"`
      }
      return `${space}:${attr}="$tl('${escapeSingle(value)}')"`
    })
  }

  next = next.replace(/>([^<>{}]*[\u4e00-\u9fff][^<>{}]*)</g, (all, value) => {
    if (value.includes('$tl(')) {
      return all
    }
    const leading = value.match(/^\s*/)?.[0] ?? ''
    const trailing = value.match(/\s*$/)?.[0] ?? ''
    const core = value.trim()
    if (!core || core.includes('\n\n')) {
      return all
    }
    return `>${leading}{{ $tl('${escapeSingle(core)}') }}${trailing}<`
  })

  next = next.replace(/(\{\{\s*[^{}?]+)\?\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'\s*:\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'(\s*\}\})/g, (_, prefix, left, right, suffix) => {
    return `${prefix}? $tl('${left}') : $tl('${right}')${suffix}`
  })

  return next
}

function transformScript(script) {
  let next = script
  const callNames = ['translateLegacyText']

  next = next.replace(/\b(message\.(?:success|error|warning|info|loading))\(\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'\s*\)/g, (_, call, value) => {
    return `${call}(translateLegacyText('${value}'))`
  })

  next = next.replace(/\b(message\.(?:success|error|warning|info|loading))\(\s*`([^`]*[\u4e00-\u9fff][^`]*)`\s*\)/g, (_, call, value) => {
    return `${call}(translateLegacyText(\`${value}\`))`
  })

  next = next.replace(/\b(okText|cancelText):\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'/g, (_, key, value) => {
    return `${key}: translateLegacyText('${value}')`
  })

  next = next.replace(/\b(title|label|tableName|content|description|placeholder|emptyText|header|name):\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'/g, (_, key, value) => {
    return `${key}: translateLegacyText('${value}')`
  })

  next = next.replace(/\b(title|label|tableName|content|description|placeholder|emptyText|header|name):\s*`([^`]*[\u4e00-\u9fff][^`]*)`/g, (_, key, value) => {
    return `${key}: translateLegacyText(\`${value}\`)`
  })

  next = next.replace(/\b(message):\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'/g, (_, key, value) => {
    return `${key}: translateLegacyText('${value}')`
  })

  next = next.replace(/\breturn\s+'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'/g, (_, value) => {
    return `return translateLegacyText('${value}')`
  })

  next = next.replace(/\?\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'\s*:\s*'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'/g, (_, left, right) => {
    return `? translateLegacyText('${left}') : translateLegacyText('${right}')`
  })

  next = next.replace(/(=\s*)'((?:\\.|[^'])+[\u4e00-\u9fff](?:\\.|[^'])*)'/g, (_, prefix, value) => {
    return `${prefix}translateLegacyText('${value}')`
  })

  if (next.includes('translateLegacyText(') && !next.includes('@/utils/legacyI18n')) {
    if (next.startsWith('<script')) {
      next = insertImportIntoVueScript(next)
    } else {
      next = insertImportIntoTs(next)
    }
  }

  return next
}

function findImportBlockEnd(source) {
  const lines = source.split(/\n/)
  let endLine = 0
  let inImport = false
  let braceDepth = 0
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i]
    if (!inImport && !line.startsWith('import ')) {
      break
    }
    if (line.startsWith('import ')) {
      inImport = true
    }
    if (inImport) {
      braceDepth += (line.match(/{/g) || []).length
      braceDepth -= (line.match(/}/g) || []).length
      if (/\bfrom\s+['"][^'"]+['"]/.test(line) && braceDepth <= 0) {
        endLine = i + 1
        inImport = false
      }
    }
  }
  return lines.slice(0, endLine).join('\n').length + (endLine > 0 ? 1 : 0)
}

function insertImportIntoTs(source) {
  const importLine = "import { translateLegacyText } from '@/utils/legacyI18n'\n"
  const end = findImportBlockEnd(source)
  return `${source.slice(0, end)}${importLine}${source.slice(end)}`
}

function insertImportIntoVueScript(source) {
  const openMatch = source.match(/^<script\b[^>]*>\s*/)
  if (!openMatch) {
    return source
  }
  const openEnd = openMatch[0].length
  const bodyEnd = source.lastIndexOf('</script>')
  const body = bodyEnd >= 0 ? source.slice(openEnd, bodyEnd) : source.slice(openEnd)
  const nextBody = insertImportIntoTs(body)
  return bodyEnd >= 0
    ? `${source.slice(0, openEnd)}${nextBody}${source.slice(bodyEnd)}`
    : `${source.slice(0, openEnd)}${nextBody}`
}

function transformVue(source) {
  let next = source
  const templateStart = next.indexOf('<template')
  const scriptStart = next.search(/<script\b/)
  const templateEndSearchTo = scriptStart >= 0 ? scriptStart : next.length
  const templateEnd = next.lastIndexOf('</template>', templateEndSearchTo)
  if (templateStart >= 0 && templateEnd >= templateStart) {
    const end = templateEnd + '</template>'.length
    next = `${next.slice(0, templateStart)}${transformTemplate(next.slice(templateStart, end))}${next.slice(end)}`
  }
  return next.replace(/<script\b[^>]*>[\s\S]*?<\/script>/, match => transformScript(match))
}

let changed = 0
for (const file of walk(viewsDir)) {
  if (!isOfficialView(file) || !existsSync(file)) {
    continue
  }
  const source = readFileSync(file, 'utf8')
  const next = file.endsWith('.vue') ? transformVue(source) : transformScript(source)
  if (next !== source) {
    writeFileSync(file, next, 'utf8')
    changed += 1
  }
}

console.log(JSON.stringify({ changed }, null, 2))
