/**
 * 路由配置文件
 * 负责定义应用的路由规则、路由守卫和动态路由注入逻辑
 * @author Forgex Team
 * @version 1.0.0
 */
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { ref } from 'vue'
import { usePermissionStore } from '../stores/permission'
import { getRoutes } from '../api/system/route'

/**
 * 静态路由配置
 * 定义应用的基础路由，包括登录页、初始化页、工作区和重定向路由
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' }, // 默认重定向到登录页
  { path: '/login', component: () => import('../views/auth/login/index.vue') }, // 登录页
  { path: '/init', component: () => import('../views/auth/init-wizard/index.vue') }, // 初始化向导页
  {
    path: '/workspace',
    name: 'Workspace',
    component: () => import('../layouts/MainLayout.vue'), // 主布局组件
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人信息', module: 'sys' } // 个人信息页
      },
      {
        path: 'sys/config',
        name: 'SystemConfig',
        component: () => import('../views/system/config/index.vue'),
        meta: { title: '系统配置', module: 'sys' } // 系统配置页
      }
    ]
  },
  {
    path: '/redirect',
    name: 'Redirect',
    component: { template: '<div />' },
    beforeEnter: (to, from, next) => {
      const target = (to.query as any)?.to as string | undefined
      if (target) {
        next(target) // 重定向到目标路径
      } else {
        next('/workspace') // 默认重定向到工作区
      }
    }
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(), // 使用 HTML5 History 模式
  routes // 注册静态路由
})

/**
 * 路由恢复状态标识
 * 用于防止路由恢复过程中出现无限循环
 */
let isRestoringRoutes = false

/**
 * 全局路由守卫
 * 检查登录状态和动态路由，实现路由拦截和权限控制
 * @param to 目标路由
 * @param from 源路由
 * @param next 路由跳转函数
 */
router.beforeEach(async (to, from, next) => {
  console.log('[Guard] Navigating to:', to.path, 'from:', from.path)

  // 获取会话信息
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  const permissionStore = usePermissionStore()


  // 如果访问登录页或初始化页，直接放行
  if (to.path === '/login' || to.path === '/init') {

    next()
    return
  }

  // 如果未登录，跳转到登录页
  if (!account || !tenantId) {

    next('/login')
    return
  }

  // 如果动态路由为空且不在恢复过程中，尝试从 Pinia store 恢复
  if (dynamicRoutes.value.length === 0 && !isRestoringRoutes) {

    isRestoringRoutes = true

    try {
      try {

        const payload = await getRoutes({ account, tenantId })
        if (payload && Array.isArray(payload.routes) && Array.isArray(payload.modules) && payload.routes.length > 0) {

          await injectDynamicRoutes(payload)
          isRestoringRoutes = false
          next({ ...to, replace: true })
          return
        }
      } catch (e) {

      }

      const cached = permissionStore.restoreRoutesAndModules()

      if (cached.routes.length > 0 && cached.modules.length > 0) {



        // 重新注入动态路由
        await injectDynamicRoutes({
          routes: cached.routes,
          modules: cached.modules
        })

        isRestoringRoutes = false
        // 路由已恢复，重新导航到目标路径
        next({ ...to, replace: true })
        return
      } else {

        isRestoringRoutes = false
        // 没有缓存的路由，需要重新登录
        next('/login')
        return
      }
    } catch (error) {

      isRestoringRoutes = false
      next('/login')
      return
    }
  }

  // 如果访问 /workspace 根路径，重定向到系统管理主页
  if (to.path === '/workspace' || to.path === '/workspace/') {

    next('/workspace/sys/dashboard')
    return
  }

  // 如果已登录，直接放行（动态路由已经在登录时注入）
  next()
})

export default router

/**
 * 空视图组件
 * 用于路由组件加载失败时的默认显示
 */
const EmptyView = { template: '<div style="padding:16px;color:#9ca3af;">暂无页面，请稍后</div>' }

/**
 * 模块代码映射表
 * 将后端的模块代码映射到前端的目录名
 */
const modulePathMap: Record<string, string> = {
  'sys': 'system',      // sys 模块对应 system 目录
  'system': 'system',   // 兼容完整名称
  // 未来可以添加更多映射，例如：
  // 'prod': 'production',
  // 'qc': 'quality',
}

/**
 * 动态导入组件
 * 约定：组件名格式为 ModulePage，例如 SystemUser, SysDashboard
 * 自动映射到路径：../views/{module}/{page}/index.vue
 *
 * @param componentName 组件名称，例如 "SystemUser", "SysDashboard"
 * @returns 动态导入的组件
 * @throws {Error} 组件加载失败时抛出错误
 */
function loadComponent(componentName: string) {
  try {
    // 解析组件名（优先支持 System*/Sys* 的多单词页面）
    // - SystemUser -> module: system, page: User
    // - SystemExcelExportConfig -> module: system, page: ExcelExportConfig
    // - SysDashboard -> module: sys, page: Dashboard
    let modulePart = ''
    let pagePartRaw = ''

    if (componentName.startsWith('System') && componentName.length > 6) {
      // 处理 System 前缀的组件名
      modulePart = 'system'
      pagePartRaw = componentName.slice(6)
    } else if (componentName.startsWith('Sys') && componentName.length > 3) {
      // 处理 Sys 前缀的组件名
      modulePart = 'sys'
      pagePartRaw = componentName.slice(3)
    } else {
      // 处理其他格式的组件名
      const match = componentName.match(/^([A-Z][a-z]*[A-Z]?[a-z]*)([A-Z][a-z]+)$/)
      if (!match) {

        return EmptyView
      }
      modulePart = match[1].toLowerCase()
      pagePartRaw = match[2]
    }

    // 将页面名称转换为小写开头的驼峰命名
    const pagePart = pagePartRaw.charAt(0).toLowerCase() + pagePartRaw.slice(1)

    // 使用映射表获取实际的目录名
    const moduleDir = modulePathMap[modulePart] || modulePart

    // 构建组件路径
    const componentPath = `../views/${moduleDir}/${pagePart}/index.vue`


    // 动态导入组件
    return () => import(/* @vite-ignore */ componentPath)
  } catch (error) {

    return EmptyView
  }
}

/**
 * 动态模块列表
 * 存储从后端获取的模块信息
 */
export const dynamicModules = ref<any[]>([])

/**
 * 动态路由列表
 * 存储从后端获取的路由信息
 */
export const dynamicRoutes = ref<any[]>([])

/**
 * 已注入的动态路由名称集合
 * <p>
 * 用于在重新注入（例如切换语言）时清理旧路由，避免路由记录重复导致页面必须刷新才能生效。
 * </p>
 */
const injectedRouteNames = new Set<string>()

/**
 * 动态路由注入函数
 * 根据后端返回的路由数据，动态注册路由到路由实例
 *
 * @param payload 包含模块和路由数据的负载
 * @returns Promise<void>
 */
export async function injectDynamicRoutes(payload: any) {
  const r = router

  // 重新注入前先清理旧的动态路由，避免同 path 的旧路由记录残留
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

  // 解析模块和路由数据
  const mods = Array.isArray(payload?.modules) ? payload.modules : []
  const routesPayload = Array.isArray(payload?.routes) ? payload.routes : []

  // 更新动态模块和路由列表
  dynamicModules.value = mods
  dynamicRoutes.value = routesPayload

  // 缓存到 Pinia store（会自动持久化到 localStorage）
  const permissionStore = usePermissionStore()
  permissionStore.setRoutes(routesPayload)
  permissionStore.setModules(mods)


  const buildDynamicRouteName = (fullPath: string) => {
    const raw = String(fullPath || '')
    const normalized = raw.replace(/^\//, '').replace(/\//g, ':')
    return `dyn:${normalized}`
  }

  // 遍历路由数据，注册动态路由
  for (const routeItem of routesPayload) {
    const moduleCode = routeItem.path
    const children = Array.isArray(routeItem.children) ? routeItem.children : []

    // 如果后端未提供 dashboard，才自动补充一个默认 dashboard 路由
    const hasDashboard = children.some((c: any) => String(c?.path || '') === 'dashboard')
    if (!hasDashboard) {
      const dashboardComponentKey = `${moduleCode.charAt(0).toUpperCase() + moduleCode.slice(1)}Dashboard`
      const dashboardComponent = loadComponent(dashboardComponentKey)
      const dashboardPath = `${moduleCode}/dashboard`
      const dashboardName = buildDynamicRouteName(dashboardPath)

      r.addRoute('Workspace', {
        path: dashboardPath,
        name: dashboardName,
        component: dashboardComponent,
        meta: {
          title: 'common.home',
          module: moduleCode
        }
      })
      injectedRouteNames.add(dashboardName)
    }

    // 注册模块下的子路由
    for (const c of children) {
      const key = c.component
      const childPath = c.path

      // 构建完整路径：/workspace/{moduleCode}/{childPath}
      const fullPath = `${moduleCode}/${childPath}`

      // catalog类型的菜单不注册路由，但需要处理其下的子菜单
      if (c.meta && c.meta.type === 'catalog') {

        // 处理catalog菜单下的子菜单
        const catalogChildren = Array.isArray(c.children) ? c.children : []
        for (const subChild of catalogChildren) {
          const subKey = subChild.component
          const subChildPath = subChild.path

          const subComp = loadComponent(subKey)

          // 构建完整路径：/workspace/{moduleCode}/{childPath}/{subChildPath}
          const subFullPath = `${fullPath}/${subChildPath}`
          const subRouteName = buildDynamicRouteName(subFullPath)



          // 添加子路由到 Workspace 路由下
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
        }
        // catalog类型菜单本身不需要注册路由，继续处理下一个菜单
        continue
      }

      // 非catalog类型菜单直接注册路由
      const comp = loadComponent(key)
      const routeName = buildDynamicRouteName(fullPath)



      // 添加子路由到 Workspace 路由下
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
    }

  }

  // 打印所有注册的路由（调试用）

  r.getRoutes().forEach(route => {
    if (route.path.includes('workspace')) {

    }
  })

}
