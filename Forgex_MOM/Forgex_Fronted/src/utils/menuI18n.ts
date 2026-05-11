import router, { dynamicModules, dynamicRoutes } from '@/router'
import i18n from '@/locales'
import { resolveI18nText } from '@/utils/i18n'
import { translateLegacyText } from '@/utils/legacyI18n'

const ROUTE_TITLE_FALLBACK_KEYS: Record<string, string> = {
  'workflow.execution.startApproval': 'workflow.execution.startTitle',
  'integration.title': 'integration.title',
  'integration.home.title': 'integration.home.title',
}

function normalizeMenuPath(path: string) {
  return String(path || '').split('?')[0]
}

function translateMenuKey(key: string) {
  const globalI18n = i18n.global as any
  if (typeof globalI18n.te === 'function' && globalI18n.te(key)) {
    return String(globalI18n.t(key))
  }

  const fallbackKey = ROUTE_TITLE_FALLBACK_KEYS[key]
  if (fallbackKey && typeof globalI18n.te === 'function' && globalI18n.te(fallbackKey)) {
    return String(globalI18n.t(fallbackKey))
  }

  return key
}

function resolveRouteMetaTitle(path: string) {
  const normalizedPath = normalizeMenuPath(path)
  if (!normalizedPath) {
    return ''
  }

  const resolved = router.resolve(normalizedPath)
  const matchedRouteWithTitle = [...resolved.matched].reverse().find(item => item.meta && item.meta.title)
  if (matchedRouteWithTitle?.meta?.title) {
    return String(matchedRouteWithTitle.meta.title)
  }

  const match = router.getRoutes().find(item => item.path === normalizedPath)
  if (match?.meta?.title) {
    return String(match.meta.title)
  }

  return ''
}

function resolveDynamicRouteTitle(path: string) {
  const normalizedPath = normalizeMenuPath(path)
  const clean = normalizedPath.replace(/^\/workspace\//, '')
  const parts = clean.split('/').filter(Boolean)
  if (parts.length < 2) {
    return ''
  }

  const moduleCode = parts[0]
  const childPath = parts[1]
  const routes = Array.isArray(dynamicRoutes.value) ? dynamicRoutes.value : []
  const topRoute = routes.find((item: any) => item?.path === moduleCode || item?.meta?.module === moduleCode)
  if (!topRoute || !Array.isArray(topRoute.children)) {
    return ''
  }

  const child = topRoute.children.find((item: any) => String(item?.path || '') === childPath)
  if (child?.meta?.title) {
    return String(child.meta.title)
  }
  if (child?.name) {
    return String(child.name)
  }

  return ''
}

export function resolveMenuTitle(rawTitle: unknown): string {
  if (rawTitle === undefined || rawTitle === null || rawTitle === '') {
    return ''
  }

  if (typeof rawTitle === 'object' && !Array.isArray(rawTitle)) {
    return resolveI18nText(rawTitle, '')
  }

  const title = String(rawTitle)
  const trimmed = title.trim()
  if (!trimmed) {
    return ''
  }

  if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
    return resolveI18nText(trimmed, title)
  }

  if (trimmed.includes('.')) {
    const translated = translateMenuKey(trimmed)
    if (translated !== trimmed) {
      return translated
    }
  }

  const resolvedText = resolveI18nText(title, title)
  if (resolvedText !== title) {
    return resolvedText
  }

  const legacyTranslated = translateLegacyText(title)
  return legacyTranslated || title
}

export function resolveModuleDisplayName(moduleCode: string, rawName?: unknown): string {
  const normalizedCode = String(moduleCode || '').trim()
  const normalizedName = rawName === undefined || rawName === null ? '' : String(rawName).trim()
  const moduleTitleKey = normalizedCode ? `${normalizedCode}.title` : ''

  if (moduleTitleKey) {
    const translated = resolveMenuTitle(moduleTitleKey)
    if (translated && translated !== moduleTitleKey) {
      return translated
    }
  }

  if (normalizedName) {
    const translatedName = resolveMenuTitle(normalizedName)
    if (translatedName && translatedName !== normalizedName) {
      return translatedName
    }
    return translateLegacyText(normalizedName)
  }

  return normalizedCode
}

export function resolveMenuDisplayName(options: {
  path?: string
  title?: unknown
  moduleCode?: string
  moduleName?: unknown
}) {
  const normalizedPath = normalizeMenuPath(String(options.path || ''))
  const routeTitle = normalizedPath ? resolveRouteMetaTitle(normalizedPath) || resolveDynamicRouteTitle(normalizedPath) : ''
  if (routeTitle) {
    const translatedRouteTitle = resolveMenuTitle(routeTitle)
    if (translatedRouteTitle) {
      return translatedRouteTitle
    }
  }

  const translatedTitle = resolveMenuTitle(options.title)
  if (translatedTitle) {
    return translatedTitle
  }

  if (normalizedPath) {
    const clean = normalizedPath.replace(/^\/workspace\//, '')
    const moduleCode = options.moduleCode || clean.split('/').filter(Boolean)[0] || ''
    const modules = Array.isArray(dynamicModules.value) ? dynamicModules.value : []
    const matchedModule = modules.find((item: any) => String(item?.code || '') === String(moduleCode || ''))
    if (matchedModule) {
      return resolveModuleDisplayName(String(matchedModule.code || moduleCode), matchedModule.name)
    }
  }

  return translateLegacyText(String(options.title || normalizedPath || ''))
}
