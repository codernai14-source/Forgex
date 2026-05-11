/**
 * 璺敱閰嶇疆鏂囦欢
 * 璐熻矗瀹氫箟搴旂敤璺敱瑙勫垯銆佽矾鐢卞畧鍗拰鍔ㄦ€佽矾鐢辨敞鍏ラ€昏緫銆?
 * @author Forgex Team
 * @version 1.0.0
 */
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { h, ref } from 'vue'
import { usePermissionStore } from '../stores/permission'
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
    },
    {
      path: 'governance/compensation',
      component: () => import('../views/workflow/governance/compensation/index.vue'),
      meta: { title: 'workflow.execution.compensationCenter', hidden: true }
    }
  ]
}

/**
 * 闈欐€佽矾鐢遍厤缃?
 * 瀹氫箟搴旂敤鍩虹璺敱锛屽寘鎷櫥褰曢〉銆佸垵濮嬪寲椤点€佸伐浣滃尯鍜岄噸瀹氬悜璺敱銆?
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' }, // 榛樿閲嶅畾鍚戝埌鐧诲綍椤?
  { path: '/login', component: () => import('../views/auth/login/index.vue') }, // 鐧诲綍椤?
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
        meta: { title: 'profile.title', module: 'sys' } // 涓汉淇℃伅椤?
      },
      {
        path: 'sys/config',
        name: 'SystemConfig',
        component: () => import('../views/system/config/index.vue'),
        meta: { title: 'system.config.title', module: 'sys' }
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
        name: 'WorkflowExecutionStart琛ㄥ崟',
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
        next('/workspace') // 榛樿閲嶅畾鍚戝埌宸ヤ綔鍖?
      }
    }
  }
]

/**
 * 鍒涘缓璺敱瀹炰緥
 */
const router = createRouter({
  history: createWebHistory(), // 娴ｈ法鏁?HTML5 History 妯″紡
  routes // 娉ㄥ唽闈欐€佽矾鐢?
})

/**
 * 璺敱鎭㈠鐘舵€佹爣璁?
 * 鐢ㄤ簬闃叉璺敱鎭㈠杩囩▼涓嚭鐜版棤闄愬惊鐜€?
 */
let isRestoringRoutes = false

/**
 * 鍏ㄥ眬璺敱瀹堝崼
 * 妫€鏌ョ櫥褰曠姸鎬佸拰鍔ㄦ€佽矾鐢憋紝瀹炵幇璺敱鎷︽埅鍜屾潈闄愭帶鍒躲€?
 * @param to 鐩爣璺敱
 * @param from 鏉ユ簮璺敱
 * @param next 璺敱璺宠浆鍑芥暟
 */
router.beforeEach(async (to, from, next) => {
  console.log('[Guard] Navigating to:', to.path, 'from:', from.path)

  // 鑾峰彇浼氳瘽淇℃伅
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  const permissionStore = usePermissionStore()

  // 濡傛灉璁块棶鐧诲綍椤垫垨鍒濆鍖栭〉锛岀洿鎺ユ斁琛屻€?
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

  // 濡傛灉鏈櫥褰曪紝璺宠浆鍒扮櫥褰曢〉銆?
  if (!account || !tenantId) {
    next('/login')
    return
  }

  // 濡傛灉鍔ㄦ€佽矾鐢变负绌轰笖涓嶅湪鎭㈠杩囩▼涓紝灏濊瘯鎭㈠璺敱銆?
  if (dynamicRoutes.value.length === 0 && !isRestoringRoutes) {
    isRestoringRoutes = true

    try {
      // 浼樺厛浠庣紦瀛樻仮澶嶏紝閬垮厤涓嶅繀瑕佺殑 API 璋冪敤銆?
      const cached = permissionStore.restoreRoutesAndModules()

      if (cached.routes.length > 0 || cached.modules.length > 0) {
        console.log('[Guard] Restoring routes from cache')
        
        // 閲嶆柊娉ㄥ叆鍔ㄦ€佽矾鐢便€?
        await injectDynamicRoutes({
          routes: cached.routes,
          modules: cached.modules
        })

        isRestoringRoutes = false
        // 璺敱宸叉仮澶嶏紝閲嶆柊瀵艰埅鍒扮洰鏍囪矾鐢便€?
        next({ ...to, replace: true })
        return
      }

      // 濡傛灉缂撳瓨涓虹┖锛屽皾璇曚粠鍚庣鑾峰彇銆?
      console.log('[Guard] No cached routes, fetching from backend')
      try {
        const payload = await getRoutes({ account, tenantId })
        if (payload && Array.isArray(payload.routes) && Array.isArray(payload.modules)) {
          console.log('[Guard] Routes fetched from backend successfully')
          
          // 鐎涙ê鍋嶉弶鍐娣団剝浼?
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

      // 濡傛灉閮藉け璐ヤ簡锛岃烦杞埌鐧诲綍椤点€?
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

  // 婵″倹鐏夌拋鍧楁６ /workspace 鏍硅矾寰勶紝閲嶅畾鍚戝埌绯荤粺绠＄悊涓婚〉
  if (to.path === '/workspace' || to.path === '/workspace/') {
    next(PERSONAL_HOME_PATH)
    return
  }

  // 濡傛灉宸茬櫥褰曚笖璺敱宸叉敞鍏ワ紝鐩存帴鏀捐
  next()
})

export default router

/**
 * 绌鸿鍥剧粍浠?
 * 鐢ㄤ簬璺敱缁勪欢鍔犺浇澶辫触鏃剁殑榛樿鏄剧ず銆?
 */
const EmptyView = {
  name: 'RouteEmptyView',
  render: () => h('div', { style: 'padding:16px;color:#9ca3af;' }, 'Page not available yet')
}

/**
 * 妯″潡浠ｇ爜鏄犲皠
 * 灏嗗悗绔ā鍧椾唬鐮佹槧灏勫埌鍓嶇鐩綍銆?
 */
const modulePathMap: Record<string, string> = {
  'sys': 'system',
  'system': 'system',
  'basic': 'basic',
  'approval': 'workflow',
  'integration': 'integrationPlatform',
  'label': 'label',
}

/**
 * 瀹℃壒妯″潡鑿滃崟 component key 涓庣洰褰曠粨鏋勭殑闈欐€佹槧灏勩€?
 * 涓庢暟鎹簱鑴氭湰涓殑 component_key 淇濇寔涓€鑷淬€?
 */
const approvalWorkflowComponents: Record<string, () => Promise<any>> = {
  ApprovalDashboard: () => import('../views/workflow/dashboard/index.vue'),
  ApprovalTaskConfig: () => import('../views/workflow/taskConfig/index.vue'),
  ApprovalExecutionStart: () => import('../views/workflow/execution/start.vue'),
  ApprovalMyPending: () => import('../views/workflow/myTask/pending.vue'),
  ApprovalMyProcessed: () => import('../views/workflow/myTask/processed.vue'),
  ApprovalMyInitiated: () => import('../views/workflow/myTask/initiated.vue'),
  ApprovalCompensationCenter: () => import('../views/workflow/governance/compensation/index.vue'),
}

const viewModules = import.meta.glob('../views/**/*.vue') as Record<string, () => Promise<any>>

/**
 * 鍔ㄦ€佸鍏ョ粍浠躲€?
 * 绾﹀畾缁勪欢鍚嶆牸寮忎负 ModulePage锛屼緥濡?SystemUser銆丼ysDashboard銆?
 * 鑷姩鏄犲皠鍒?../views/{module}/{page}.vue銆?
 *
 * @param componentName 缁勪欢鍚嶇О锛屼緥濡?"SystemUser"銆?SysDashboard"
 * @returns 鍔ㄦ€佸鍏ョ殑缁勪欢
 * @throws {Error} 缁勪欢鍔犺浇澶辫触鏃舵姏鍑洪敊璇?
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

    const specialComponentMap: Record<string, string> = {
      LabelTemplate: '../views/label/template/index.vue',
      LabelPrint: '../views/label/print/index.vue',
      LabelRecord: '../views/label/record/index.vue',
      LabelBinding: '../views/label/binding/index.vue',
    }
    if (normalizedName && specialComponentMap[normalizedName]) {
      const mappedPath = specialComponentMap[normalizedName]
      const mappedLoader = viewModules[mappedPath]
      if (mappedLoader) {
        return mappedLoader
      }
    }

    const stableComponentMap: Record<string, string> = {
      BasicDashboard: '../views/basic/dashboard/index.vue',
      BasicCustomer: '../views/basic/customer/index.vue',
      BasicSupplier: '../views/basic/supplier/index.vue',
      BasicEncodeRule: '../views/basic/encodeRule/index.vue',
      BasicMaterial: '../views/basic/material/index.vue',
      BasicUnit: '../views/basic/unit/index.vue',
      BasicMaterialRaw: '../views/basic/material/index.vue',
      BasicMaterialSemiFinished: '../views/basic/material/index.vue',
      BasicMaterialFinished: '../views/basic/material/index.vue',
      SystemDashboard: '../views/system/dashboard/index.vue',
      SystemAndroidVersion: '../views/system/androidVersion/index.vue',
      SystemNotice: '../views/system/notice/index.vue',
      SystemPosition: '../views/system/position/index.vue',
      SystemRole: '../views/system/role/index.vue',
      SystemRoleMenuGrant: '../views/system/role/MenuGrant.vue',
      SystemRoleUserGrant: '../views/system/role/UserGrant.vue',
      ApprovalDashboard: '../views/workflow/dashboard/index.vue',
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
 * 鍔ㄦ€佹ā鍧楀垪琛ㄣ€?
 * 瀛樺偍浠庡悗绔幏鍙栫殑妯″潡淇℃伅
 */
export const dynamicModules = ref<any[]>([])

/**
 * 鍔ㄦ€佽矾鐢卞垪琛ㄣ€?
 * 瀛樺偍浠庡悗绔幏鍙栫殑璺敱淇℃伅
 */
export const dynamicRoutes = ref<any[]>([])

/**
 * 宸叉敞鍏ョ殑鍔ㄦ€佽矾鐢卞悕绉伴泦鍚堛€?
 * <p>
 * 鐢ㄤ簬閲嶆柊娉ㄥ叆鏃舵竻鐞嗘棫璺敱锛岄伩鍏嶈矾鐢辫褰曢噸澶嶅鑷撮〉闈㈤渶瑕佸埛鏂版墠鐢熸晥銆?
 * </p>
 */
const injectedRouteNames = new Set<string>()

/**
 * 鍔ㄦ€佽矾鐢辨敞鍏ュ嚱鏁般€?
 * 鏍规嵁鍚庣杩斿洖鐨勮矾鐢辨暟鎹紝鍔ㄦ€佹敞鍐岃矾鐢卞埌璺敱瀹炰緥銆?
 *
 * @param payload 鍖呭惈妯″潡鍜岃矾鐢辨暟鎹殑璐熻浇
 * @returns Promise<void>
 */
export async function injectDynamicRoutes(payload: any) {
  const r = router

  // 閲嶆柊娉ㄥ叆鍓嶅厛娓呯悊鏃х殑鍔ㄦ€佽矾鐢憋紝閬垮厤鏃ц矾鐢辫褰曟畫鐣欍€?
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

  // 瑙ｆ瀽妯″潡鍜岃矾鐢辨暟鎹€?
  const mods = Array.isArray(payload?.modules) ? payload.modules : []
  const routesPayload = Array.isArray(payload?.routes)
    ? JSON.parse(JSON.stringify(payload.routes))
    : []

  // 鏇存柊鍔ㄦ€佹ā鍧楀拰璺敱鍒楄〃銆?
  dynamicModules.value = mods
  dynamicRoutes.value = routesPayload

  // 缂撳瓨鍒?Pinia store锛屼細鑷姩鎸佷箙鍖栧埌 localStorage銆?
  const permissionStore = usePermissionStore()
  permissionStore.setRoutes(routesPayload)
  permissionStore.setModules(mods)


  const buildDynamicRouteName = (fullPath: string) => {
    const raw = String(fullPath || '')
    const normalized = raw.replace(/^\//, '').replace(/\//g, ':')
    return `dyn:${normalized}`
  }

  // 閬嶅巻璺敱鏁版嵁锛屾敞鍐屽姩鎬佽矾鐢便€?
  for (const routeItem of routesPayload) {
    const moduleCode = routeItem.path
    const children = Array.isArray(routeItem.children) ? routeItem.children : []
    const registeredModulePaths = new Set<string>()

    const registerMenuRoutes = (menuItems: any[] = [], parentPath = '') => {
      for (const c of menuItems) {
        const childPath = String(c?.path || '').replace(/^\/+|\/+$/g, '')
        if (!childPath) {
          continue
        }

        const relativePath = parentPath ? `${parentPath}/${childPath}` : childPath
        const fullPath = `${moduleCode}/${relativePath}`
        const menuType = c?.meta?.type || c?.type

        if (menuType === 'catalog') {
          registerMenuRoutes(Array.isArray(c.children) ? c.children : [], relativePath)
          continue
        }

        const routeName = buildDynamicRouteName(fullPath)
        r.addRoute('Workspace', {
          path: fullPath,
          name: routeName,
          component: loadComponent(c.component, moduleCode, relativePath),
          meta: {
            ...c.meta,
            module: moduleCode
          }
        })
        injectedRouteNames.add(routeName)
        registeredModulePaths.add(fullPath)

        registerMenuRoutes(Array.isArray(c.children) ? c.children : [], relativePath)
      }
    }

    registerMenuRoutes(children)

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

  // 閬嶅巻鎵€鏈夊凡娉ㄥ唽璺敱锛岃皟璇曟椂鍙湪姝ゅ杈撳嚭銆?

  r.getRoutes().forEach(route => {
    if (route.path.includes('workspace')) {

    }
  })

}

