/**
 * 璺敱閰嶇疆鏂囦欢
 * 璐熻矗瀹氫箟搴旂敤鐨勮矾鐢辫鍒欍€佽矾鐢卞畧鍗拰鍔ㄦ€佽矾鐢辨敞鍏ラ€昏緫
 * @author Forgex Team
 * @version 1.0.0
 */
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { h, ref } from 'vue'
import { use权限Store } from '../stores/permission'
import { getRoutes } from '../api/system/route'
import { APPROVAL_ROUTE_BASE, LEGACY_APPROVAL_ROUTE_BASE, approvalRoutePaths } from './approvalRoutePaths'

export const PERSONAL_HOME_PATH = '/workspace/home'
export const FAVORITE_MANAGEMENT_PATH = '/workspace/home/favorites'

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
 * 闈欐€佽矾鐢遍厤锟?
 * 瀹氫箟搴旂敤鐨勫熀纭€璺敱锛屽寘鎷櫥褰曢〉銆佸垵濮嬪寲椤点€佸伐浣滃尯鍜岄噸瀹氬悜璺敱
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' }, // 榛樿閲嶅畾鍚戝埌鐧诲綍锟?
  { path: '/login', component: () => import('../views/auth/login/index.vue') }, // 鐧诲綍锟?
  { path: '/register', component: () => import('../views/auth/register/index.vue') },
  { path: '/init', component: () => import('../views/auth/init-wizard/index.vue') }, // 鍒濆鍖栧悜瀵奸〉
  {
    path: '/workspace',
    name: 'Workspace',
    component: () => import('../layouts/MainLayout.vue'), // 涓诲竷灞€缁勪欢
    children: [
      {
        path: 'home',
        name: 'PersonalHome',
        component: () => import('../views/home/index.vue'),
        meta: { title: 'layout.personalHomepage' }
      },
      {
        path: 'home/favorites',
        name: 'FavoriteManagement',
        component: () => import('../views/home/index.vue'),
        meta: { title: 'layout.favoriteManagement' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: 'profile.title', module: 'sys' } // 涓汉淇℃伅锟?
      },
      {
        path: 'sys/config',
        name: 'SystemConfig',
        component: () => import('../views/system/config/index.vue'),
        meta: { title: 'system.config.title', module: 'sys' } // 绯荤粺閰嶇疆锟?
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
        name: 'WorkflowExecutionStart表单',
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
        next(target) // 閲嶅畾鍚戝埌鐩爣璺緞
      } else {
        next('/workspace') // 榛樿閲嶅畾鍚戝埌宸ヤ綔锟?
      }
    }
  }
]

/**
 * 鍒涘缓璺敱瀹炰緥
 */
const router = createRouter({
  history: createWebHistory(), // 浣跨敤 HTML5 History 妯″紡
  routes // 娉ㄥ唽闈欐€佽矾锟?
})

/**
 * 璺敱鎭㈠鐘舵€佹爣锟?
 * 鐢ㄤ簬闃叉璺敱鎭㈠杩囩▼涓嚭鐜版棤闄愬惊锟?
 */
let isRestoringRoutes = false

/**
 * 鍏ㄥ眬璺敱瀹堝崼
 * 妫€鏌ョ櫥褰曠姸鎬佸拰鍔ㄦ€佽矾鐢憋紝瀹炵幇璺敱鎷︽埅鍜屾潈闄愭帶锟?
 * @param to 鐩爣璺敱
 * @param from 婧愯矾锟?
 * @param next 璺敱璺宠浆鍑芥暟
 */
router.beforeEach(async (to, from, next) => {
  console.log('[Guard] Navigating to:', to.path, 'from:', from.path)

  // 鑾峰彇浼氳瘽淇℃伅
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  const permissionStore = use权限Store()

  // 濡傛灉璁块棶鐧诲綍椤垫垨鍒濆鍖栭〉锛岀洿鎺ユ斁锟?
  if (to.path === '/login' || to.path === '/register' || to.path === '/init') {
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

  // 濡傛灉鏈櫥褰曪紝璺宠浆鍒扮櫥褰曢〉
  if (!account || !tenantId) {
    next('/login')
    return
  }

  // 濡傛灉鍔ㄦ€佽矾鐢变负绌轰笖涓嶅湪鎭㈠杩囩▼涓紝灏濊瘯鎭㈠璺敱
  if (dynamicRoutes.value.length === 0 && !isRestoringRoutes) {
    isRestoringRoutes = true

    try {
      // 浼樺厛浠庣紦瀛樻仮澶嶏紙閬垮厤涓嶅繀瑕佺殑API璋冪敤锟?
      const cached = permissionStore.restoreRoutesAndModules()

      if (cached.routes.length > 0 || cached.modules.length > 0) {
        console.log('[Guard] Restoring routes from cache')
        
        // 閲嶆柊娉ㄥ叆鍔ㄦ€佽矾锟?
        await injectDynamicRoutes({
          routes: cached.routes,
          modules: cached.modules
        })

        isRestoringRoutes = false
        // 璺敱宸叉仮澶嶏紝閲嶆柊瀵艰埅鍒扮洰鏍囪矾锟?
        next({ ...to, replace: true })
        return
      }

      // 濡傛灉缂撳瓨涓虹┖锛屽皾璇曚粠鍚庣鑾峰彇
      console.log('[Guard] No cached routes, fetching from backend')
      try {
        const payload = await getRoutes({ account, tenantId })
        if (payload && Array.isArray(payload.routes) && Array.isArray(payload.modules)) {
          console.log('[Guard] Routes fetched from backend successfully')
          
          // 瀛樺偍鏉冮檺淇℃伅
          if (payload.buttons) {
            permissionStore.set权限s(payload.buttons)
          }
          
          await injectDynamicRoutes(payload)
          isRestoringRoutes = false
          next({ ...to, replace: true })
          return
        }
      } catch (e) {
        console.error('[Guard] Failed to fetch routes from backend:', e)
      }

      // 濡傛灉閮藉け璐ヤ簡锛岃烦杞埌鐧诲綍锟?
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

  // 濡傛灉璁块棶 /workspace 鏍硅矾寰勶紝閲嶅畾鍚戝埌绯荤粺绠＄悊涓婚〉
  if (to.path === '/workspace' || to.path === '/workspace/') {
    next(PERSONAL_HOME_PATH)
    return
  }

  // 濡傛灉宸茬櫥褰曚笖璺敱宸叉敞鍏ワ紝鐩存帴鏀捐
  next()
})

export default router

/**
 * 绌鸿鍥剧粍锟?
 * 鐢ㄤ簬璺敱缁勪欢鍔犺浇澶辫触鏃剁殑榛樿鏄剧ず
 */
const EmptyView = {
  name: 'RouteEmptyView',
  render: () => h('div', { style: 'padding:16px;color:#9ca3af;' }, 'Page not available yet')
}

/**
 * 妯″潡浠ｇ爜鏄犲皠锟?
 * 灏嗗悗绔殑妯″潡浠ｇ爜鏄犲皠鍒板墠绔殑鐩綍锟?
 */
const modulePathMap: Record<string, string> = {
  'sys': 'system',      // sys 妯″潡瀵瑰簲 system 鐩綍
  'system': 'system',   // 鍏煎瀹屾暣鍚嶇О
  /** 瀹℃壒绠＄悊妯″潡缂栫爜锟?approval锛岄〉闈㈢粍浠朵粛浣嶄簬 views/workflow */
  'approval': 'workflow',
  // 鏈潵鍙互娣诲姞鏇村鏄犲皠锛屼緥濡傦細
  // 'prod': 'production',
  // 'qc': 'quality',
}

/**
 * 瀹℃壒妯″潡鑿滃崟浣跨敤锟?component 閿笌鐩綍缁撴瀯锛坵orkflow 涓嬪绾ц矾寰勶級鐨勯潤鎬佹槧灏勶拷?
 * <p>涓庢暟鎹簱鑴氭湰 {@code V2.0.1_瀹℃壒绠＄悊妯″潡涓庤彍锟?sql}銆亄@code V2.0.2_瀹℃壒宸ヤ綔鍙拌彍锟?sql} 锟?component_key 淇濇寔涓€鑷达拷?/p>
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
 * 鍔ㄦ€佸鍏ョ粍锟?
 * 绾﹀畾锛氱粍浠跺悕鏍煎紡锟?ModulePage锛屼緥锟?SystemUser, SysDashboard
 * 鑷姩鏄犲皠鍒拌矾寰勶細../views/{module}/{page}.vue锛堝崟鏂囦欢缁撴瀯锟?
 *
 * @param componentName 缁勪欢鍚嶇О锛屼緥锟?"SystemUser", "SysDashboard"
 * @returns 鍔ㄦ€佸鍏ョ殑缁勪欢
 * @throws {Error} 缁勪欢鍔犺浇澶辫触鏃舵姏鍑洪敊锟?
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
 * 鍔ㄦ€佹ā鍧楀垪锟?
 * 瀛樺偍浠庡悗绔幏鍙栫殑妯″潡淇℃伅
 */
export const dynamicModules = ref<any[]>([])

/**
 * 鍔ㄦ€佽矾鐢卞垪锟?
 * 瀛樺偍浠庡悗绔幏鍙栫殑璺敱淇℃伅
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
    title: '椤佃〃閰嶇疆',
    icon: 'TableOutlined',
    childPaths: ['tableConfig', 'userTableConfig'],
  })

  groupSystemMenus(sysRoute.children, {
    catalogPath: 'excelConfig',
    title: 'Excel閰嶇疆',
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
 * 宸叉敞鍏ョ殑鍔ㄦ€佽矾鐢卞悕绉伴泦锟?
 * <p>
 * 鐢ㄤ簬鍦ㄩ噸鏂版敞鍏ワ紙渚嬪鍒囨崲璇█锛夋椂娓呯悊鏃ц矾鐢憋紝閬垮厤璺敱璁板綍閲嶅瀵艰嚧椤甸潰蹇呴』鍒锋柊鎵嶈兘鐢熸晥锟?
 * </p>
 */
const injectedRouteNames = new Set<string>()

/**
 * 鍔ㄦ€佽矾鐢辨敞鍏ュ嚱锟?
 * 鏍规嵁鍚庣杩斿洖鐨勮矾鐢辨暟鎹紝鍔ㄦ€佹敞鍐岃矾鐢卞埌璺敱瀹炰緥
 *
 * @param payload 鍖呭惈妯″潡鍜岃矾鐢辨暟鎹殑璐熻浇
 * @returns Promise<void>
 */
export async function injectDynamicRoutes(payload: any) {
  const r = router

  // 閲嶆柊娉ㄥ叆鍓嶅厛娓呯悊鏃х殑鍔ㄦ€佽矾鐢憋紝閬垮厤锟?path 鐨勬棫璺敱璁板綍娈嬬暀
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

  // 瑙ｆ瀽妯″潡鍜岃矾鐢辨暟锟?
  const mods = Array.isArray(payload?.modules) ? payload.modules : []
  const routesPayload = normalizeAuthorizationRoutes(Array.isArray(payload?.routes) ? payload.routes : [])

  // 鏇存柊鍔ㄦ€佹ā鍧楀拰璺敱鍒楄〃
  dynamicModules.value = mods
  dynamicRoutes.value = routesPayload

  // 缂撳瓨锟?Pinia store锛堜細鑷姩鎸佷箙鍖栧埌 localStorage锟?
  const permissionStore = use权限Store()
  permissionStore.setRoutes(routesPayload)
  permissionStore.setModules(mods)


  const buildDynamicRouteName = (fullPath: string) => {
    const raw = String(fullPath || '')
    const normalized = raw.replace(/^\//, '').replace(/\//g, ':')
    return `dyn:${normalized}`
  }

  // 閬嶅巻璺敱鏁版嵁锛屾敞鍐屽姩鎬佽矾锟?
  for (const routeItem of routesPayload) {
    const moduleCode = routeItem.path
    const children = Array.isArray(routeItem.children) ? routeItem.children : []
    const registeredModulePaths = new Set<string>()

    // 娉ㄥ唽妯″潡涓嬬殑瀛愯矾锟?
    for (const c of children) {
      const key = c.component
      const childPath = c.path

      // 鏋勫缓瀹屾暣璺緞锟?workspace/{moduleCode}/{childPath}
      const fullPath = `${moduleCode}/${childPath}`

      // catalog绫诲瀷鐨勮彍鍗曚笉娉ㄥ唽璺敱锛屼絾闇€瑕佸鐞嗗叾涓嬬殑瀛愯彍锟?
      if (c.meta && c.meta.type === 'catalog') {

        // 澶勭悊catalog鑿滃崟涓嬬殑瀛愯彍锟?
        const catalogChildren = Array.isArray(c.children) ? c.children : []
        for (const subChild of catalogChildren) {
          const subKey = subChild.component
          const subChildPath = subChild.path

          const subComp = loadComponent(subKey, moduleCode, `${childPath}/${subChildPath}`)

          // 鏋勫缓瀹屾暣璺緞锟?workspace/{moduleCode}/{childPath}/{subChildPath}
          const subFullPath = `${fullPath}/${subChildPath}`
          const subRouteName = buildDynamicRouteName(subFullPath)



          // 娣诲姞瀛愯矾鐢卞埌 Workspace 璺敱锟?
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
        // catalog绫诲瀷鑿滃崟鏈韩涓嶉渶瑕佹敞鍐岃矾鐢憋紝缁х画澶勭悊涓嬩竴涓彍锟?
        continue
      }

      // 闈瀋atalog绫诲瀷鑿滃崟鐩存帴娉ㄥ唽璺敱
      const comp = loadComponent(key, moduleCode, childPath)
      const routeName = buildDynamicRouteName(fullPath)



      // 娣诲姞瀛愯矾鐢卞埌 Workspace 璺敱锟?
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

  // 鎵撳嵃鎵€鏈夋敞鍐岀殑璺敱锛堣皟璇曠敤锟?

  r.getRoutes().forEach(route => {
    if (route.path.includes('workspace')) {

    }
  })

}

