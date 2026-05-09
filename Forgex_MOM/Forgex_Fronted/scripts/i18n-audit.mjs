#!/usr/bin/env node
import { execFileSync } from 'node:child_process'
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { dirname, join, relative, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import vm from 'node:vm'
import { createRequire } from 'node:module'
import ts from 'typescript'

const root = dirname(dirname(fileURLToPath(import.meta.url)))
const srcDir = join(root, 'src')
const localeDir = join(srcDir, 'locales')
const langs = ['zh-CN', 'en-US', 'zh-TW', 'ja-JP', 'ko-KR']
const dbLangs = ['zh-CN', 'en-US', 'zh-TW', 'ja-JP', 'ko-KR']
const errors = []
const warnings = []
const moduleCache = new Map()
const require = createRequire(import.meta.url)
const auditDir = join(root, 'audit-reports')
const officialViewHardcoded = []
const excludedHardcoded = []
const ignoredViewPatterns = [
  /^views\/_test\//,
  /^views\/EnglishTest\.vue$/,
  /^views\/ThemeTest\.vue$/,
]

function walk(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    const stat = statSync(full)
    if (stat.isDirectory()) {
      if (['node_modules', 'dist', '.vite'].includes(name)) continue
      walk(full, out)
    } else {
      out.push(full)
    }
  }
  return out
}

function flatten(obj, prefix = '', out = new Set()) {
  if (!obj || typeof obj !== 'object' || Array.isArray(obj)) {
    if (prefix) out.add(prefix)
    return out
  }
  for (const [key, value] of Object.entries(obj)) {
    const next = prefix ? `${prefix}.${key}` : key
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      flatten(value, next, out)
    } else {
      out.add(next)
    }
  }
  return out
}

function stripComments(line, state) {
  let out = ''
  for (let i = 0; i < line.length; i += 1) {
    const ch = line[i]
    const next = line[i + 1]
    if (state.vueBlock) {
      if (ch === '-' && next === '-' && line[i + 2] === '>') {
        state.vueBlock = false
        i += 2
      }
      continue
    }
    if (state.block) {
      if (ch === '*' && next === '/') {
        state.block = false
        i += 1
      }
      continue
    }
    if (ch === '<' && next === '!' && line.slice(i, i + 4) === '<!--') {
      state.vueBlock = true
      i += 3
      continue
    }
    if (ch === '/' && next === '*') {
      state.block = true
      i += 1
      continue
    }
    if (ch === '/' && next === '/') {
      break
    }
    out += ch
  }
  return out
}

function isOfficialView(rel) {
  return rel.startsWith('views/') && !ignoredViewPatterns.some(pattern => pattern.test(rel))
}

function stripKnownI18nCalls(code) {
  let next = code
  const callPatterns = [
    /\$tl\(\s*(['"`])(?:\\.|(?!\1).)*\1\s*\)/g,
    /translateLegacyText\(\s*(['"`])(?:\\.|(?!\1).)*\1\s*\)/g,
    /translateLegacyContent\(\s*(['"`])(?:\\.|(?!\1).)*\1\s*\)/g,
  ]
  for (const pattern of callPatterns) {
    next = next.replace(pattern, '')
  }
  next = next.replace(/\b(?:const|let|var|function|class|interface|type|enum|extends|implements|import|export)\s+[A-Za-z0-9_]*[\u4e00-\u9fff][A-Za-z0-9_\u4e00-\u9fff]*/g, '')
  return next
}

function resolveTsModule(request, parentDir) {
  const base = request.startsWith('.') ? resolve(parentDir, request) : request
  const candidates = [
    base,
    `${base}.ts`,
    join(base, 'index.ts'),
  ]
  for (const candidate of candidates) {
    if (existsSync(candidate) && statSync(candidate).isFile()) {
      return candidate
    }
  }
  throw new Error(`Cannot resolve module ${request} from ${parentDir}`)
}

function loadTsModule(file) {
  const full = resolve(file)
  if (moduleCache.has(full)) return moduleCache.get(full).exports

  const source = readFileSync(full, 'utf8')
  const output = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020,
      esModuleInterop: true,
      skipLibCheck: true,
    },
    fileName: full,
  }).outputText

  const module = { exports: {} }
  moduleCache.set(full, module)
  const localRequire = request => {
    if (!request.startsWith('.')) {
      return require(request)
    }
    return loadTsModule(resolveTsModule(request, dirname(full)))
  }
  const runner = vm.runInThisContext(
    `(function(exports, require, module, __filename, __dirname) { ${output}\n})`,
    { filename: full },
  )
  runner(module.exports, localRequire, module, full, dirname(full))
  return module.exports
}

function loadLocale(lang) {
  const modulePath = join(localeDir, lang, 'index.ts')
  const mod = loadTsModule(modulePath)
  return mod.default || mod
}

function relativeSet(lang) {
  return new Set(
    walk(join(localeDir, lang))
      .filter(file => file.endsWith('.ts'))
      .map(file => relative(join(localeDir, lang), file).replace(/\\/g, '/')),
  )
}

for (const lang of langs) {
  if (!existsSync(join(localeDir, lang))) {
    errors.push(`Missing locale directory: ${lang}`)
  }
}

const baseFiles = relativeSet('zh-CN')
for (const lang of langs.filter(lang => lang !== 'zh-CN')) {
  const files = relativeSet(lang)
  for (const file of baseFiles) {
    if (file === 'mergeWithFallback.ts') continue
    if (!files.has(file)) errors.push(`${lang} missing locale file ${file}`)
  }
  for (const file of files) {
    if (!baseFiles.has(file)) warnings.push(`${lang} has extra locale file ${file}`)
  }
}

const baseLocale = loadLocale('zh-CN')
const baseKeys = flatten(baseLocale)
for (const lang of langs.filter(lang => lang !== 'zh-CN')) {
  const keys = flatten(loadLocale(lang))
  for (const key of baseKeys) {
    if (!keys.has(key)) errors.push(`${lang} missing key ${key}`)
  }
}

const chineseHardcoded = []
for (const file of walk(srcDir)) {
  if (!/\.(vue|ts|tsx)$/.test(file)) continue
  const rel = relative(srcDir, file).replace(/\\/g, '/')
  if (rel.startsWith('locales/') || rel.includes('/types.')) continue
  const content = readFileSync(file, 'utf8')
  const lines = content.split(/\r?\n/)
  const commentState = { block: false, vueBlock: false }
  lines.forEach((line, index) => {
    const code = stripComments(line, commentState)
    const codeWithoutI18n = stripKnownI18nCalls(code)
    const trimmed = codeWithoutI18n.trim()
    if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('*')) return
    if (/^\s*(console\.(log|warn|error|info|debug)|throw\s+new\s+Error)\b/.test(trimmed)) return
    if (/^[A-Za-z0-9_.$()[\]{}<>=!,'":\-\s|/]+$/.test(trimmed)) return
    if (/^[A-Za-z0-9_.$()[\]{}<>=!,'":\-\s|/]+[\u4e00-\u9fff][A-Za-z0-9_.$()[\]{}<>=!,'":\-\s|/]*$/.test(trimmed) && !/[<>'"`]/.test(trimmed)) {
      return
    }
    if (/^(import|export)\s/.test(trimmed)) return
    if (/^(const|let|var|function|class|interface|type|enum)\s+[A-Za-z0-9_$]+$/.test(trimmed)) return
    if (/[\u4e00-\u9fff]/.test(codeWithoutI18n)) {
      const item = `${rel}:${index + 1}: ${trimmed.slice(0, 160)}`
      chineseHardcoded.push(item)
      if (isOfficialView(rel)) {
        officialViewHardcoded.push(item)
      } else if (rel.startsWith('views/')) {
        excludedHardcoded.push(item)
      }
    }
  })
}

if (chineseHardcoded.length) {
  warnings.push(`Chinese hardcoded text candidates outside locale files: ${chineseHardcoded.length}`)
  warnings.push(...chineseHardcoded.slice(0, 120))
}

if (!existsSync(auditDir)) {
  mkdirSync(auditDir, { recursive: true })
}
writeFileSync(join(auditDir, 'official-view-hardcoded.txt'), `${officialViewHardcoded.join('\n')}\n`, 'utf8')
writeFileSync(join(auditDir, 'excluded-view-hardcoded.txt'), `${excludedHardcoded.join('\n')}\n`, 'utf8')
if (officialViewHardcoded.length) {
  warnings.push(`Official view hardcoded text candidates: ${officialViewHardcoded.length}`)
  warnings.push(`Full official view report: ${relative(root, join(auditDir, 'official-view-hardcoded.txt')).replace(/\\/g, '/')}`)
}

function runMysql(sql, database) {
  const args = [
    '--host=127.0.0.1',
    '--user=root',
    '--password=123456',
    '--default-character-set=utf8mb4',
    '--batch',
    '--skip-column-names',
  ]
  if (database) args.push(database)
  args.push('--execute', sql)
  return execFileSync('mysql', args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim()
}

if (process.env.SKIP_DB_AUDIT !== '1') {
  try {
    const jsonChecks = [
      ['forgex_admin', 'sys_menu', 'name_i18n_json', 'name_i18n_json IS NOT NULL AND name_i18n_json <> ""'],
      ['forgex_admin', 'sys_c_menu', 'name_i18n_json', 'name_i18n_json IS NOT NULL AND name_i18n_json <> ""'],
      ['forgex_admin', 'sys_dict', 'dict_value_i18n_json', 'dict_value_i18n_json IS NOT NULL AND dict_value_i18n_json <> ""'],
      ['forgex_common', 'fx_i18n_message', 'text_i18n_json', 'text_i18n_json IS NOT NULL AND text_i18n_json <> ""'],
      ['forgex_common', 'fx_table_config', 'table_name_i18n_json', 'table_name_i18n_json IS NOT NULL AND table_name_i18n_json <> ""'],
      ['forgex_common', 'fx_table_column_config', 'title_i18n_json', 'title_i18n_json IS NOT NULL AND title_i18n_json <> ""'],
    ]
    for (const [db, table, column, where] of jsonChecks) {
      const missingExpr = dbLangs.map(lang => `JSON_UNQUOTE(JSON_EXTRACT(${column}, '$."${lang}"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(${column}, '$."${lang}"')) = ''`).join(' OR ')
      const count = runMysql(`SELECT COUNT(*) FROM ${table} WHERE ${where} AND JSON_VALID(${column}) = 1 AND (${missingExpr});`, db)
      if (Number(count) > 0) warnings.push(`${db}.${table}.${column} rows missing five-language JSON values: ${count}`)
    }
    const responseCount = runMysql(`SELECT COUNT(*) FROM sys_response_message_template WHERE lang NOT IN ('${dbLangs.join("','")}');`, 'forgex_common')
    if (Number(responseCount) > 0) warnings.push(`forgex_common.sys_response_message_template rows with unsupported lang: ${responseCount}`)
  } catch (error) {
    warnings.push(`Database audit skipped or failed: ${error.message}`)
  }
}

for (const warning of warnings) console.warn(`[i18n-audit] WARN ${warning}`)
for (const error of errors) console.error(`[i18n-audit] ERROR ${error}`)

if (errors.length) {
  process.exit(1)
}

console.log(`[i18n-audit] Passed with ${warnings.length} warning(s).`)
