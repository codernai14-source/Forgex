import assert from 'node:assert/strict'
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import test from 'node:test'
import { classifyFindings, collectFindings } from '../doc-freshness.mjs'

test('baseline suppresses only its recorded findings', () => {
  const findings = [
    { id: 'PORT:README.md:gateway', severity: 'error', message: 'old port' },
    { id: 'PATH:Forgex_Doc/a.md:12', severity: 'error', message: 'missing path' },
  ]

  const result = classifyFindings(findings, {
    knownIssues: ['PORT:README.md:gateway'],
  })

  assert.equal(result.newErrors.length, 1)
  assert.equal(result.newErrors[0].id, 'PATH:Forgex_Doc/a.md:12')
  assert.equal(result.baselined.length, 1)
})

test('reports a missing frontend src shorthand path from documentation', t => {
  const root = mkdtempSync(join(tmpdir(), 'forgex-doc-freshness-'))
  t.after(() => rmSync(root, { recursive: true, force: true }))
  const write = (path, content = '') => {
    mkdirSync(join(root, path, '..'), { recursive: true })
    writeFileSync(join(root, path), content)
  }

  write('Forgex_Doc/前端/example.md', 'Missing file: `src/api/missing.ts`.')
  write('Forgex_Doc/安卓端/README.md', 'core/*`（0 个）')
  write('Forgex_Doc/安卓端/网络层与统一结果.md', 'X-Tenant-Id')
  write('Forgex_Doc/后端/README.md')
  write('Forgex_MOM/Forgex_Mobile_Android/settings.gradle.kts')
  write('Forgex_MOM/Forgex_Backend/pom.xml')
  write(
    'Forgex_MOM/Forgex_Backend/Forgex_Gateway/src/main/java/com/forgex/gateway/filter/TenantPropagationGlobalFilter.java',
    'HEADER_TENANT_ID = "X-Tenant-Id";'
  )
  write(
    'Forgex_MOM/Forgex_Backend/Forgex_Common_Contract/src/main/java/com/forgex/common/web/StatusCode.java',
    ''
  )
  write('Forgex_Build/manifest/services.yml')
  write(
    'Forgex_Build/collect-artifacts.ps1',
    '$docDatabaseUpgradeRoot = Join-Path $repoRoot "Forgex_Doc/数据库/脚本与修复"'
  )
  mkdirSync(join(root, 'Forgex_Doc/数据库/脚本与修复'), { recursive: true })
  write('README.md')

  const findings = collectFindings(root)

  assert.ok(findings.some(finding => finding.id.includes('src/api/missing.ts')))
})
