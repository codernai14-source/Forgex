/**
 * 路由配置文件
 * 负责定义应用的路由规则、路由守卫和动态路由注入逻辑
 * @author Forgex Team
 * @version 1.0.0
 */
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { h, ref } from 'vue'
import { usePermissionStore } from '../stores/permission'
import { getRoutes } from '../api/system/route'
import { APPROVAL_ROUTE_BASE, LEGACY_APPROVAL_ROUTE_BASE, approvalRoutePaths } from './approvalRoutePaths'

export const PERSONAL_HOME_PATH = '/workspace/home'

interface LocalModuleRouteDefinition {
  path: string
  component: () => Promise<any>
  meta: Record<string, any>
}

const localModuleRoutes: Record<string, LocalModuleRouteDefinition[]> = {
  approval: [
    {
      path: 'taskConfig/:taskCode/nodes',
      component: () => import('../views/workflow/taskConfig/nodes.vue'),
      meta: { title: 'workflow.taskConfig.nodeConfig', hidden: true }
    },
    {
      path: 'execution/start/:taskCode',
      component: () => import('../views/workflow/execution/startForm.vue'),
      meta: { title: 'workflow.execution.startApproval', hidden: true }
    }
  ]
}

/**
 * 静态路由配�?
 * 定义应用的基础路由，包括登录页、初始化页、工作区和重定向路由
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' }, // 默认重定向到登录�?
  { path: '/login', component: () => import('../views/auth/login/index.vue') }, // 登录�?
  { path: '/init', component: () => import('../views/auth/init-wizard/index.vue') }, // 初始化向导页
  {
    path: '/workspace',
    name: 'Workspace',
    component: () => import('../layouts/MainLayout.vue'), // 主布局组件
    children: [
      {
        path: 'home',
        name: 'PersonalHome',
        component: () => import('../views/home/index.vue'),
        meta: { title: 'layout.personalHomepage' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: 'profile.title', module: 'sys' } // 个人信息�?
      },
      {
        path: 'sys/config',
        name: 'SystemConfig',
        component: () => import('../views/system/config/index.vue'),
        meta: { title: 'system.config.title', module: 'sys' } // 系统配置�?
      }
    ]
  },
  {
    path: '/__legacy-workflow__',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/__legacy-workflow__/taskConfig',
    children: [
      {
        path: 'taskConfig',
        name: 'WorkflowTaskConfig',
        component: () => import('../views/workflow/taskConfig/index.vue'),
        meta: { title: 'workflow.taskConfig.title', module: 'approval' }
      },
      {
        path: 'taskConfig/:taskCode/nodes',
        name: 'WorkflowTaskConfigNodes',
        component: () => import('../views/workflow/taskConfig/nodes.vue'),
        meta: { title: 'workflow.taskConfig.nodeConfig', module: 'approval', hidden: true }
      },
      {
        path: 'execution/start/:taskCode',
        name: 'WorkflowExecutionStartForm',
        component: () => import('../views/workflow/execution/startForm.vue'),
        meta: { title: 'workflow.execution.startApproval', module: 'approval', hidden: true }
      }
    ]
  },
  {
    path: '/redirect',
    name: 'Redirect',
    component: { render: () => h('div') },
    beforeEnter: (to, from, next) => {
      const target = (to.query as any)?.to as string | undefined
      if (target) {
        next(target) // 重定向到目标路径
      } else {
        next('/workspace') // 默认重定向到工作�?
      }
    }
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(), // 使用 HTML5 History 模式
  routes // 注册静态路�?
})

/**
 * 路由恢复状态标�?
 * 用于防止路由恢复过程中出现无限循�?
 */
let isRestoringRoutes = false

/**
 * 全局路由守卫
 * 检查登录状态和动态路由，实现路由拦截和权限控�?
 * @param to 目标路由
 * @param from 源路�?
 * @param next 路由跳转函数
 */
router.beforeEach(async (to, from, next) => {
  console.log('[Guard] Navigating to:', to.path, 'from:', from.path)

  // 获取会话信息
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  const permissionStore = usePermissionStore()

  // 如果访问登录页或初始化页，直接放�?
  if (to.path === '/login' || to.path === '/init') {
    next()
    return
  }

  if (to.path === LEGACY_APPROVAL_ROUTE_BASE) {
    next({
      path: approvalRoutePaths.taskConfigList,
      query: to.query,
      hash: to.hash,
      replace: true
    })
    return
  }

  if (to.path.startsWith(`${LEGACY_APPROVAL_ROUTE_BASE}/`)) {
    next({
      path: `${APPROVAL_ROUTE_BASE}${to.path.slice(LEGACY_APPROVAL_ROUTE_BASE.length)}`,
      query: to.query,
      hash: to.hash,
      replace: true
    })
    return
  }

  // 如果未登录，跳转到登录页
  if (!account || !tenantId) {
    next('/login')
    return
  }

  // 如果动态路由为空且不在恢复过程中，尝试恢复路由
  if (dynamicRoutes.value.length === 0 && !isRestoringRoutes) {
    isRestoringRoutes = true

    try {
      // 优先从缓存恢复（避免不必要的API调用�?
      const cached = permissionStore.restoreRoutesAndModules()

      if (cached.routes.length > 0 || cached.modules.length > 0) {
        console.log('[Guard] Restoring routes from cache')
        
        // 重新注入动态路�?
        await injectDynamicRoutes({
          routes: cached.routes,
          modules: cached.modules
        })

        isRestoringRoutes = false
        // 路由已恢复，重新导航到目标路�?
        next({ ...to, replace: true })
        return
      }

      // 如果缓存为空，尝试从后端获取
      console.log('[Guard] No cached routes, fetching from backend')
      try {
        const payload = await getRoutes({ account, tenantId })
        if (payload && Array.isArray(payload.routes) && Array.isArray(payload.modules)) {
          console.log('[Guard] Routes fetched from backend successfully')
          
          // 存储权限信息
          if (payload.buttons) {
            permissionStore.setPermissions(payload.buttons)
          }
          
          await injectDynamicRoutes(payload)
          isRestoringRoutes = false
          next({ ...to, replace: true })
          return
        }
      } catch (e) {
        console.error('[Guard] Failed to fetch routes from backend:', e)
      }

      // 如果都失败了，跳转到登录�?
      isRestoringRoutes = false
      next('/login')
      return
    } catch (error) {
      console.error('[Guard] Route restoration failed:', error)
      isRestoringRoutes = false
      next('/login')
      return
    }
  }

  // 如果访问 /workspace 根路径，重定向到系统管理主页
  if (to.path === '/workspace' || to.path === '/workspace/') {
    next(PERSONAL_HOME_PATH)
    return
  }

  // 如果已登录且路由已注入，直接放行
  next()
})

export default router

/**
 * 空视图组�?
 * 用于路由组件加载失败时的默认显示
 */
const EmptyView = {
  name: 'RouteEmptyView',
  render: () => h('div', { style: 'padding:16px;color:#9ca3af;' }, 'Page not available yet')
}

/**
 * 模块代码映射�?
 * 将后端的模块代码映射到前端的目录�?
 */
const modulePathMap: Record<string, string> = {
  'sys': 'system',      // sys 模块对应 system 目录
  'system': 'system',   // 兼容完整名称
  /** 审批管理模块编码�?approval，页面组件仍位于 views/workflow */
  'approval': 'workflow',
  // 未来可以添加更多映射，例如：
  // 'prod': 'production',
  // 'qc': 'quality',
}

/**
 * 审批模块菜单使用�?component 键与目录结构（workflow 下多级路径）的静态映射�?
 * <p>与数据库脚本 {@code V2.0.1_审批管理模块与菜�?sql}、{@code V2.0.2_审批工作台菜�?sql} �?component_key 保持一致�?/p>
 */
const approvalWorkflowComponents: Record<string, () => Promise<any>> = {
  ApprovalDashboard: () => import('../views/workflow/dashboard/index.vue'),
  ApprovalTaskConfig: () => import('../views/workflow/taskConfig/index.vue'),
  ApprovalExecutionStart: () => import('../views/workflow/execution/start.vue'),
  ApprovalMyPending: () => import('../views/workflow/myTask/pending.vue'),
  ApprovalMyProcessed: () => import('../views/workflow/myTask/processed.vue'),
  ApprovalMyInitiated: () => import('../views/workflow/myTask/initiated.vue'),
}

const viewModules = import.meta.glob('../views/**/*.vue') as Record<string, () => Promise<any>>

/**
 * 动态导入组�?
 * 约定：组件名格式�?ModulePage，例�?SystemUser, SysDashboard
 * 自动映射到路径：../views/{module}/{page}.vue（单文件结构�?
 *
 * @param componentName 组件名称，例�?"SystemUser", "SysDashboard"
 * @returns 动态导入的组件
 * @throws {Error} 组件加载失败时抛出错�?
 */
function loadComponent(componentName: string, moduleHint?: string, routePathHint?: string) {
  try {
    const normalizedName = String(componentName || '').trim()
    const normalizedModuleHint = String(moduleHint || '').trim().toLowerCase()
    const normalizedRoutePath = String(routePathHint || '').trim()

    if (normalizedName) {
      const approvalLoader = approvalWorkflowComponents[normalizedName]
      if (approvalLoader) {
        return approvalLoader
      }
    }

    const specialComponentMap: Record<string, string> = {}
    if (normalizedName && specialComponentMap[normalizedName]) {
      const mappedPath = specialComponentMap[normalizedName]
      const mappedLoader = viewModules[mappedPath]
      if (mappedLoader) {
        return mappedLoader
      }
    }

    const stableComponentMap: Record<string, string> = {
      SystemRole: '../views/system/role/index.vue',
      SystemRoleMenuGrant: '../views/system/role/MenuGrant.vue',
      SystemRoleUserGrant: '../views/system/role/UserGrant.vue',
    }
    if (normalizedName && stableComponentMap[normalizedName]) {
      const stablePath = stableComponentMap[normalizedName]
      const stableLoader = viewModules[stablePath]
      if (stableLoader) {
        return stableLoader
      }
    }

    const toLowerCamel = (value: string) => {
      if (!value) return value
      return value.charAt(0).toLowerCase() + value.slice(1)
    }
    const toPascalCase = (value: string) => value
      .split(/[-_]/)
      .filter(Boolean)
      .map(part => part.charAt(0).toUpperCase() + part.slice(1))
      .join('')
    const getRouteStaticSegments = (routePath: string) => routePath
      .split('/')
      .map(item => item.trim())
      .filter(item => item && !item.startsWith(':'))
    const resolveModulePath = (moduleCode: string) => modulePathMap[moduleCode] || moduleCode
    const resolveExistingModulePath = (path: string) => {
      if (viewModules[path]) {
        return path
      }
      const target = path.toLowerCase()
      const matched = Object.keys(viewModules).find(key => key.toLowerCase() === target)
      return matched || ''
    }

    let modulePart = ''
    let pagePartRaw = ''

    if (normalizedName.startsWith('System') && normalizedName.length > 6) {
      modulePart = 'system'
      pagePartRaw = normalizedName.slice(6)
    } else if (normalizedName.startsWith('Sys') && normalizedName.length > 3) {
      modulePart = 'sys'
      pagePartRaw = normalizedName.slice(3)
    } else if (normalizedName) {
      const match = normalizedName.match(/^([A-Z][a-zA-Z0-9]*?)([A-Z][a-zA-Z0-9]*)$/)
      if (match) {
        modulePart = match[1].toLowerCase()
        pagePartRaw = match[2]
      } else if (normalizedModuleHint) {
        modulePart = normalizedModuleHint
        pagePartRaw = normalizedName
      }
    }

    if (!modulePart && normalizedModuleHint) {
      modulePart = normalizedModuleHint
    }
    if (!modulePart) {
      return EmptyView
    }

    if ((normalizedModuleHint === 'sys' || normalizedModuleHint === 'system')) {
      const normalizedRoutePathLower = normalizedRoutePath.toLowerCase()
      if (normalizedRoutePathLower.endsWith('/authorization/role') || normalizedRoutePathLower === 'role') {
        const roleLoader = viewModules['../views/system/role/index.vue']
        if (roleLoader) {
          return roleLoader
        }
      }
    }

    const routeStaticSegments = getRouteStaticSegments(normalizedRoutePath)
    const routePageName = routeStaticSegments.length > 0
      ? toPascalCase(routeStaticSegments[routeStaticSegments.length - 1])
      : ''
    if (!pagePartRaw && routePageName) {
      pagePartRaw = routePageName
    }

    const moduleDir = resolveModulePath(modulePart)
    const pathCandidates: string[] = []
    const pushCandidate = (path: string) => {
      if (path && !pathCandidates.includes(path)) {
        pathCandidates.push(path)
      }
    }
    const pushPageCandidates = (subDir: string, pageName: string) => {
      if (!pageName) return
      const variants = Array.from(new Set([pageName, toLowerCamel(pageName)]))
      for (const variant of variants) {
        pushCandidate(`../views/${moduleDir}/${subDir}${variant}/index.vue`)
        pushCandidate(`../views/${moduleDir}/${subDir}${variant}.vue`)
      }
    }

    if (pagePartRaw) {
      pushPageCandidates('', pagePartRaw)
      if (pagePartRaw.startsWith('Role') && pagePartRaw.length > 4) {
        pushPageCandidates('role/', pagePartRaw.slice(4))
      }
    }

    if (routePageName && routePageName !== pagePartRaw) {
      pushPageCandidates('', routePageName)
    }

    if (routeStaticSegments.length > 0) {
      const routePath = routeStaticSegments.join('/')
      const routePathLower = routePath.toLowerCase()
      pushCandidate(`../views/${moduleDir}/${routePath}/index.vue`)
      pushCandidate(`../views/${moduleDir}/${routePath}.vue`)
      if (routePathLower !== routePath) {
        pushCandidate(`../views/${moduleDir}/${routePathLower}/index.vue`)
        pushCandidate(`../views/${moduleDir}/${routePathLower}.vue`)
      }
    }

    for (const candidate of pathCandidates) {
      const resolvedPath = resolveExistingModulePath(candidate)
      if (resolvedPath) {
        return viewModules[resolvedPath]
      }
    }

    const fuzzyPageNames = Array.from(new Set([pagePartRaw, routePageName].filter(Boolean)))
    for (const fuzzyName of fuzzyPageNames) {
      const lowerPage = toLowerCamel(fuzzyName)
      const dirSuffix = `/${moduleDir}/${lowerPage}/index.vue`.toLowerCase()
      const fileSuffix = `/${moduleDir}/${lowerPage}.vue`.toLowerCase()
      const matchedPath = Object.keys(viewModules).find(key => {
        const lowerKey = key.toLowerCase()
        return lowerKey.endsWith(dirSuffix) || lowerKey.endsWith(fileSuffix)
      })
      if (matchedPath) {
        return viewModules[matchedPath]
      }
    }

    return EmptyView
  } catch (error) {
    return EmptyView
  }
}

/**
 * 动态模块列�?
 * 存储从后端获取的模块信息
 */
export const dynamicModules = ref<any[]>([])

/**
 * 动态路由列�?
 * 存储从后端获取的路由信息
 */
export const dynamicRoutes = ref<any[]>([])

function normalizeAuthorizationRoutes(routes: any[]) {
  const cloned = Array.isArray(routes)
    ? JSON.parse(JSON.stringify(routes))
    : []

  const sysRoute = cloned.find((item: any) => String(item?.path || '') === 'sys')
  if (!sysRoute || !Array.isArray(sysRoute.children)) {
    return cloned
  }

  const authorizationCatalog = sysRoute.children.find((item: any) =>
    item?.meta?.type === 'catalog' && String(item?.path || '') === 'authorization',
  )
  if (!authorizationCatalog) {
    return cloned
  }

  authorizationCatalog.children = Array.isArray(authorizationCatalog.children)
    ? authorizationCatalog.children
    : []

  const moduleIndex = sysRoute.children.findIndex((item: any) => String(item?.path || '') === 'module')
  if (moduleIndex === -1) {
    return cloned
  }

  const [moduleMenu] = sysRoute.children.splice(moduleIndex, 1)
  const existedModuleIndex = authorizationCatalog.children.findIndex((item: any) => String(item?.path || '') === 'module')
  if (existedModuleIndex !== -1) {
    authorizationCatalog.children[existedModuleIndex] = moduleMenu
  } else {
    authorizationCatalog.children.push(moduleMenu)
    authorizationCatalog.children.sort((a: any, b: any) => {
      const orderA = Number(a?.meta?.order ?? a?.order ?? 0)
      const orderB = Number(b?.meta?.order ?? b?.order ?? 0)
      return orderA - orderB
    })
  }

  return cloned
}

function normalizeSystemConfigRoutes(routes: any[]) {
  const cloned = Array.isArray(routes)
    ? JSON.parse(JSON.stringify(routes))
    : []

  const sysRoute = cloned.find((item: any) => String(item?.path || '') === 'sys')
  if (!sysRoute || !Array.isArray(sysRoute.children)) {
    return cloned
  }

  groupSystemMenus(sysRoute.children, {
    catalogPath: 'pageTableConfig',
    title: '页表配置',
    icon: 'TableOutlined',
    childPaths: ['tableConfig', 'userTableConfig'],
  })

  groupSystemMenus(sysRoute.children, {
    catalogPath: 'excelConfig',
    title: 'Excel配置',
    icon: 'FileExcelOutlined',
    childPaths: ['excelImportConfig', 'excelExportConfig'],
  })

  return cloned
}

function groupSystemMenus(
  menuList: any[],
  options: {
    catalogPath: string
    title: string
    icon: string
    childPaths: string[]
  },
) {
  const catalogIndex = menuList.findIndex((item: any) => String(item?.path || '') === options.catalogPath)
  const existingCatalog = catalogIndex >= 0 ? menuList[catalogIndex] : null
  const catalogChildren = Array.isArray(existingCatalog?.children) ? existingCatalog.children : []

  const detachedChildren: any[] = []
  let insertIndex = catalogIndex >= 0 ? catalogIndex : menuList.length

  options.childPaths.forEach(childPath => {
    const rootIndex = menuList.findIndex((item: any) => String(item?.path || '') === childPath)
    if (rootIndex !== -1) {
      if (detachedChildren.length === 0) {
        insertIndex = Math.min(insertIndex, rootIndex)
      }
      detachedChildren.push(menuList[rootIndex])
      menuList.splice(rootIndex, 1)
    }
  })

  const mergedChildren = [...catalogChildren]
  detachedChildren.forEach(child => {
    const exists = mergedChildren.some((item: any) => String(item?.path || '') === String(child?.path || ''))
    if (!exists) {
      mergedChildren.push(child)
    }
  })

  if (mergedChildren.length === 0) {
    return
  }

  const catalog = existingCatalog || {
    path: options.catalogPath,
    name: options.catalogPath,
    meta: {
      title: options.title,
      icon: options.icon,
      module: 'sys',
      menuLevel: 1,
      type: 'catalog',
    },
    children: [],
  }

  catalog.meta = {
    ...(catalog.meta || {}),
    title: options.title,
    icon: options.icon,
    module: 'sys',
    menuLevel: 1,
    type: 'catalog',
  }
  catalog.children = mergedChildren

  if (catalogIndex >= 0) {
    const refreshedIndex = menuList.findIndex((item: any) => String(item?.path || '') === options.catalogPath)
    if (refreshedIndex !== -1) {
      menuList[refreshedIndex] = catalog
    }
    return
  }

  menuList.splice(Math.min(insertIndex, menuList.length), 0, catalog)
}

/**
 * 已注入的动态路由名称集�?
 * <p>
 * 用于在重新注入（例如切换语言）时清理旧路由，避免路由记录重复导致页面必须刷新才能生效�?
 * </p>
 */
const injectedRouteNames = new Set<string>()

/**
 * 动态路由注入函�?
 * 根据后端返回的路由数据，动态注册路由到路由实例
 *
 * @param payload 包含模块和路由数据的负载
 * @returns Promise<void>
 */
export async function injectDynamicRoutes(payload: any) {
  const r = router

  // 重新注入前先清理旧的动态路由，避免�?path 的旧路由记录残留
  if (injectedRouteNames.size > 0) {
    for (const name of injectedRouteNames) {
      try {
        const hasRoute = (r as any).hasRoute
        if (typeof hasRoute === 'function') {
          if (hasRoute.call(r, name)) {
            r.removeRoute(name)
          }
        } else {
          r.removeRoute(name as any)
        }
      } catch (_) {}
    }
    injectedRouteNames.clear()
  }

  // 解析模块和路由数�?
  const mods = Array.isArray(payload?.modules) ? payload.modules : []
  const routesPayload = normalizeSystemConfigRoutes(
    normalizeAuthorizationRoutes(Array.isArray(payload?.routes) ? payload.routes : []),
  )

  // 更新动态模块和路由列表
  dynamicModules.value = mods
  dynamicRoutes.value = routesPayload

  // 缓存�?Pinia store（会自动持久化到 localStorage�?
  const permissionStore = usePermissionStore()
  permissionStore.setRoutes(routesPayload)
  permissionStore.setModules(mods)


  const buildDynamicRouteName = (fullPath: string) => {
    const raw = String(fullPath || '')
    const normalized = raw.replace(/^\//, '').replace(/\//g, ':')
    return `dyn:${normalized}`
  }

  // 遍历路由数据，注册动态路�?
  for (const routeItem of routesPayload) {
    const moduleCode = routeItem.path
    const children = Array.isArray(routeItem.children) ? routeItem.children : []
    const registeredModulePaths = new Set<string>()

    // 注册模块下的子路�?
    for (const c of children) {
      const key = c.component
      const childPath = c.path

      // 构建完整路径�?workspace/{moduleCode}/{childPath}
      const fullPath = `${moduleCode}/${childPath}`

      // catalog类型的菜单不注册路由，但需要处理其下的子菜�?
      if (c.meta && c.meta.type === 'catalog') {

        // 处理catalog菜单下的子菜�?
        const catalogChildren = Array.isArray(c.children) ? c.children : []
        for (const subChild of catalogChildren) {
          const subKey = subChild.component
          const subChildPath = subChild.path

          const subComp = loadComponent(subKey, moduleCode, `${childPath}/${subChildPath}`)

          // 构建完整路径�?workspace/{moduleCode}/{childPath}/{subChildPath}
          const subFullPath = `${fullPath}/${subChildPath}`
          const subRouteName = buildDynamicRouteName(subFullPath)



          // 添加子路由到 Workspace 路由�?
          r.addRoute('Workspace', {
            path: subFullPath,
            name: subRouteName,
            component: subComp,
            meta: {
              ...subChild.meta,
              module: moduleCode
            }
          })
          injectedRouteNames.add(subRouteName)
          registeredModulePaths.add(subFullPath)
        }
        // catalog类型菜单本身不需要注册路由，继续处理下一个菜�?
        continue
      }

      // 非catalog类型菜单直接注册路由
      const comp = loadComponent(key, moduleCode, childPath)
      const routeName = buildDynamicRouteName(fullPath)



      // 添加子路由到 Workspace 路由�?
      r.addRoute('Workspace', {
        path: fullPath,
        name: routeName,
        component: comp,
        meta: {
          ...c.meta,
          module: moduleCode
        }
      })
      injectedRouteNames.add(routeName)
      registeredModulePaths.add(fullPath)
    }

    const moduleExtraRoutes = Array.isArray(localModuleRoutes[moduleCode]) ? localModuleRoutes[moduleCode] : []
    for (const extraRoute of moduleExtraRoutes) {
      const fullPath = `${moduleCode}/${extraRoute.path}`
      if (registeredModulePaths.has(fullPath)) {
        continue
      }

      const routeName = buildDynamicRouteName(fullPath)
      r.addRoute('Workspace', {
        path: fullPath,
        name: routeName,
        component: extraRoute.component,
        meta: {
          ...extraRoute.meta,
          module: moduleCode
        }
      })
      injectedRouteNames.add(routeName)
      registeredModulePaths.add(fullPath)
    }

  }

  // 打印所有注册的路由（调试用�?

  r.getRoutes().forEach(route => {
    if (route.path.includes('workspace')) {

    }
  })

}

