/**
 * 工作区路径规范化与菜单全路径组装。
 */

/**
 * 去掉查询串、哈希和尾部斜杠，得到可比较的工作区路径。
 *
 * @param path 原始路径
 * @returns 规范化路径
 */
export function normalizeWorkspacePath(path: string) {
  const raw = String(path || '').trim().replace(/\\/g, '/')
  const withoutQuery = raw.split('?')[0].split('#')[0]
  if (!withoutQuery) {
    return ''
  }
  return withoutQuery.length > 1 && withoutQuery.endsWith('/')
    ? withoutQuery.slice(0, -1)
    : withoutQuery
}

/**
 * 按模块编码和相对菜单 path 组装工作区全路径。
 *
 * @param moduleCode 模块编码
 * @param parentSegments 父级路径片段
 * @param menuPath 菜单 path
 * @returns 全路径与片段
 */
export function buildModuleMenuPath(moduleCode: string, parentSegments: string[], menuPath: string) {
  const normalizedPath = String(menuPath || '').trim()
  if (!normalizedPath) {
    return {
      fullPath: '',
      segments: parentSegments,
    }
  }
  if (normalizedPath.startsWith('/')) {
    return {
      fullPath: normalizeWorkspacePath(normalizedPath),
      segments: normalizedPath.split('/').filter(Boolean),
    }
  }
  const currentSegments = normalizedPath.split('/').filter(Boolean)
  const segments = [...parentSegments, ...currentSegments]
  return {
    fullPath: normalizeWorkspacePath(`/workspace/${moduleCode}/${segments.join('/')}`),
    segments,
  }
}
