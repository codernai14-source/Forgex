import { existsSync, mkdirSync, readFileSync, readdirSync, statSync, writeFileSync } from 'node:fs'
import { basename, dirname, join, normalize, relative, resolve, sep } from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDirectory = dirname(fileURLToPath(import.meta.url))
const defaultRoot = resolve(scriptDirectory, '..', '..')

export function classifyFindings(findings, baseline = { knownIssues: [] }) {
  const known = new Set(baseline.knownIssues ?? [])
  const baselined = findings.filter(finding => known.has(finding.id))
  const active = findings.filter(finding => !known.has(finding.id))
  return {
    active,
    baselined,
    newErrors: active.filter(finding => finding.severity === 'error'),
  }
}

function walk(directory, predicate, files = []) {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name === '.git' || entry.name === 'target' || entry.name === 'build') continue
    const fullPath = join(directory, entry.name)
    if (entry.isDirectory()) walk(fullPath, predicate, files)
    else if (predicate(fullPath)) files.push(fullPath)
  }
  return files
}

function readUtf8(path) {
  return readFileSync(path, 'utf8').replace(/^\uFEFF/, '')
}

function firstExistingPath(paths) {
  return paths.find(path => existsSync(path)) ?? paths[0]
}

function findMavenModuleName(sourcePath) {
  let directory = dirname(sourcePath)
  while (directory && directory !== dirname(directory)) {
    if (existsSync(join(directory, 'pom.xml'))) return basename(directory)
    directory = dirname(directory)
  }
  return basename(dirname(dirname(dirname(dirname(sourcePath)))))
}

function toRepoPath(root, path) {
  return relative(root, path).split(sep).join('/')
}

function add(findings, id, severity, message) {
  findings.push({ id, severity, message })
}

function discoverPorts(root, findings) {
  const services = {}
  for (const path of walk(join(root, 'Forgex_MOM', 'Forgex_Backend'), file => basename(file) === 'application.yml')) {
    const source = readUtf8(path)
    const port = source.match(/^\s*port:\s*\$\{[^:}]+:(\d+)\}/m)?.[1]
    if (port) services[findMavenModuleName(path)] = port
  }

  const manifest = readUtf8(join(root, 'Forgex_Build', 'manifest', 'services.yml'))
  const entries = [...manifest.matchAll(/serviceId:\s*(\w+)[\s\S]*?modulePath:\s*([^\r\n]+)[\s\S]*?defaultPort:\s*(\d+)/g)]
  for (const [, serviceId, modulePath, manifestPort] of entries) {
    const moduleName = modulePath.trim().split('/').at(-1)
    const sourcePort = services[moduleName]
    if (sourcePort && sourcePort !== manifestPort) {
      add(findings, `PORT:manifest:${serviceId}`, 'error', `${serviceId} defaultPort=${manifestPort}，源码为 ${sourcePort}`)
    }
  }

  const rootReadme = readUtf8(join(root, 'README.md'))
  const gatewayPort = services.Forgex_Gateway
  const documentedGatewayPort = rootReadme.match(/网关：`https?:\/\/localhost:(\d+)`/)?.[1]
  if (gatewayPort && documentedGatewayPort !== gatewayPort) {
    add(findings, 'PORT:README.md:gateway', 'error', `README 网关端口与源码 ${gatewayPort} 不一致`)
  }
}

function discoverModules(root, findings) {
  const settings = readUtf8(join(root, 'Forgex_MOM', 'Forgex_Mobile_Android', 'settings.gradle.kts'))
  const coreCount = [...settings.matchAll(/^include\(":core:/gm)].length
  const androidReadme = readUtf8(join(root, 'Forgex_Doc', '安卓端', 'README.md'))
  const documentedCount = androidReadme.match(/core\/\*`（(\d+) 个）/)?.[1]
  if (!documentedCount || Number(documentedCount) !== coreCount) {
    add(findings, 'MODULES:android:core', 'error', `Android core 模块文档为 ${documentedCount ?? '未标注'}，源码为 ${coreCount}`)
  }

  const manifest = readUtf8(join(root, 'Forgex_Build', 'manifest', 'services.yml'))
  let serviceModules = [...manifest.matchAll(/serviceId:\s*\w+[\s\S]*?modulePath:\s*([^\r\n]+)/g)]
    .map(match => match[1].trim().split('/').at(-1))
  if (serviceModules.length === 0) {
    const pom = readUtf8(join(root, 'Forgex_MOM', 'Forgex_Backend', 'pom.xml'))
    serviceModules = [...pom.matchAll(/<module>(Forgex_(?:Gateway|Auth|Sys|Basic|Job|Workflow|Report|Integration))<\/module>/g)]
      .map(match => match[1])
  }
  const backendReadme = readUtf8(join(root, 'Forgex_Doc', '后端', 'README.md'))
  for (const moduleName of serviceModules) {
    if (!backendReadme.includes(`| ${moduleName} |`)) {
      add(findings, `MODULES:backend:service:${moduleName}`, 'error', `后端 README 未列出服务 ${moduleName}`)
    }
  }
}

function discoverDocumentPaths(root, findings) {
  const docRoot = join(root, 'Forgex_Doc')
  for (const path of walk(docRoot, file => file.endsWith('.md'))) {
    const documentPath = toRepoPath(root, path)
    const source = readUtf8(path)
    const reportMissingPath = (reference, value, target) => {
      if (!target.startsWith(root) || !existsSync(target)) {
        const line = source.slice(0, reference.index).split(/\r?\n/).length
        add(findings, `PATH:${documentPath}:${line}:${value}`, 'warn', `${documentPath}:${line} 引用了不存在的 ${value}`)
      }
    }

    const fullReferences = [...source.matchAll(/`((?:Forgex_(?:MOM|Doc|Build)\/[^`\s,)]+))`/g)]
    for (const reference of fullReferences) {
      const value = reference[1].replace(/[。；，、]+$/, '')
      reportMissingPath(reference, value, normalize(join(root, value)))
    }

    const frontendReferences = [...source.matchAll(/`(src\/(?:api|components|views|router|stores|styles|locales|theme|setup|guide|utils|types)(?:\/[^`\s,)]+)?)`/g)]
    for (const reference of frontendReferences) {
      const value = reference[1].replace(/[。；，、]+$/, '')
      const target = normalize(join(root, 'Forgex_MOM', 'Forgex_Fronted', value))
      reportMissingPath(reference, value, target)
    }
  }
}

function discoverContractDrift(root, findings) {
  const androidContract = readUtf8(join(root, 'Forgex_Doc', '安卓端', '网络层与统一结果.md'))
  const gatewaySource = readUtf8(firstExistingPath([
    join(root, 'Forgex_MOM', 'Forgex_Backend', 'forgex-admin', 'forgex-admin-gateway', 'Forgex_Gateway', 'src', 'main', 'java', 'com', 'forgex', 'gateway', 'filter', 'TenantPropagationGlobalFilter.java'),
    join(root, 'Forgex_MOM', 'Forgex_Backend', 'Forgex_Gateway', 'src', 'main', 'java', 'com', 'forgex', 'gateway', 'filter', 'TenantPropagationGlobalFilter.java'),
  ]))
  const tenantHeader = gatewaySource.match(/HEADER_TENANT_ID\s*=\s*"([^"]+)"/)?.[1]
  if (!tenantHeader || /(?<!X-)Tenant-Id/.test(androidContract) || !androidContract.includes(tenantHeader)) {
    add(findings, 'CONTRACT:android:tenant-header', 'error', 'Android 网络文档未声明 X-Tenant-Id 作为租户头')
  }
  const statusCodeSource = readUtf8(firstExistingPath([
    join(root, 'Forgex_MOM', 'Forgex_Backend', 'forgex-common', 'Forgex_Common_Contract', 'src', 'main', 'java', 'com', 'forgex', 'common', 'web', 'StatusCode.java'),
    join(root, 'Forgex_MOM', 'Forgex_Backend', 'Forgex_Common_Contract', 'src', 'main', 'java', 'com', 'forgex', 'common', 'web', 'StatusCode.java'),
  ]))
  const statusCodes = [...statusCodeSource.matchAll(/public static final int \w+\s*=\s*(\d+);/g)]
    .map(match => Number(match[1]))
    .filter(code => code >= 600 && code < 700)
  for (const code of statusCodes) {
    if (!new RegExp(`\\b${code}\\b`).test(androidContract)) {
      add(findings, `CONTRACT:android:status:${code}`, 'warn', `Android 网络文档未覆盖状态码 ${code}`)
    }
  }
}

function discoverPackagingPaths(root, findings) {
  const path = join(root, 'Forgex_Build', 'collect-artifacts.ps1')
  const source = readUtf8(path)
  const assignment = source.match(/\$docDatabaseUpgradeRoot\s*=\s*Join-Path \$repoRoot "([^"]+)"/)
  const sourcePath = assignment?.[1]?.replaceAll('\\', '/')
  if (!sourcePath || !existsSync(join(root, sourcePath))) {
    add(findings, 'PACKAGE:database-upgrade-source', 'error', `database-upgrade 来源不存在：${sourcePath ?? '未配置'}`)
  }
}

export function collectFindings(root) {
  const findings = []
  discoverPorts(root, findings)
  discoverModules(root, findings)
  discoverDocumentPaths(root, findings)
  discoverContractDrift(root, findings)
  discoverPackagingPaths(root, findings)
  return findings
}

function writeReport(root, result) {
  const reportPath = join(root, 'tools', 'doc-freshness', 'report.md')
  const lines = [
    '# 文档一致性报告',
    '',
    `- 新增 error：${result.newErrors.length}`,
    `- 已基线化问题：${result.baselined.length}`,
    `- 当前 warn：${result.active.filter(finding => finding.severity === 'warn').length}`,
    '',
  ]
  for (const finding of result.active) lines.push(`- [${finding.severity}] ${finding.id}: ${finding.message}`)
  if (result.baselined.length) {
    lines.push('', '## 基线化问题', '')
    for (const finding of result.baselined) lines.push(`- ${finding.id}: ${finding.message}`)
  }
  writeFileSync(reportPath, `${lines.join('\n')}\n`, 'utf8')
  return reportPath
}

function main() {
  const rootArgument = process.argv.find(argument => argument.startsWith('--root='))
  const root = rootArgument ? resolve(rootArgument.slice('--root='.length)) : defaultRoot
  const baselinePath = join(root, 'tools', 'doc-freshness', 'doc-freshness.baseline.json')
  const baseline = existsSync(baselinePath) ? JSON.parse(readUtf8(baselinePath)) : { knownIssues: [] }
  const result = classifyFindings(collectFindings(root), baseline)
  const reportPath = writeReport(root, result)
  console.log(`doc-freshness: ${result.newErrors.length} new error(s), ${result.baselined.length} baselined; report: ${toRepoPath(root, reportPath)}`)
  if (result.newErrors.length) process.exitCode = 1
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) main()
