import { createRouter, createWebHistory } from 'vue-router'
import { ref } from 'vue'
import { usePermissionStore } from '../stores/permission'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/auth/login/index.vue') },
  { path: '/init', component: () => import('../views/auth/init-wizard/index.vue') },
  {
    path: '/workspace',
    name: 'Workspace',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人信息', module: 'sys' }
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
        next(target)
      } else {
        next('/workspace')
      }
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和动态路由
let isRestoringRoutes = false

router.beforeEach(async (to, from, next) => {
  console.log('[Guard] Navigating to:', to.path, 'from:', from.path)
  
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  const permissionStore = usePermissionStore()
  
  console.log('[Guard] Session check - account:', account, 'tenantId:', tenantId)
  console.log('[Guard] Dynamic routes count:', dynamicRoutes.value.length)
  
  // 如果访问登录页或初始化页，直接放行
  if (to.path === '/login' || to.path === '/init') {
    console.log('[Guard] Accessing login/init page, allowing')
    next()
    return
  }
  
  // 如果未登录，跳转到登录页
  if (!account || !tenantId) {
    console.log('[Guard] Not logged in, redirecting to login')
    next('/login')
    return
  }
  
  // 如果动态路由为空且不在恢复过程中，尝试从 Pinia store 恢复
  if (dynamicRoutes.value.length === 0 && !isRestoringRoutes) {
    console.log('[Guard] Dynamic routes empty, trying to restore from Pinia store')
    isRestoringRoutes = true
    
    try {
      const cached = permissionStore.restoreRoutesAndModules()
      
      if (cached.routes.length > 0 && cached.modules.length > 0) {
        console.log('[Guard] Restoring routes from Pinia store:', cached.routes)
        
        console.log('[Guard] Before injectDynamicRoutes')
        // 重新注入动态路由
        await injectDynamicRoutes(router, {
          routes: cached.routes,
          modules: cached.modules
        })
        console.log('[Guard] After injectDynamicRoutes')
        
        console.log('[Guard] Routes restored, redirecting to:', to.fullPath)
        isRestoringRoutes = false
        // 路由已恢复，重新导航到目标路径
        next({ ...to, replace: true })
        return
      } else {
        console.log('[Guard] No cached routes found in Pinia store, redirecting to login')
        isRestoringRoutes = false
        // 没有缓存的路由，需要重新登录
        next('/login')
        return
      }
    } catch (error) {
      console.error('[Guard] Failed to restore routes:', error)
      isRestoringRoutes = false
      next('/login')
      return
    }
  }
  
  // 如果访问 /workspace 根路径，重定向到系统管理主页
  if (to.path === '/workspace' || to.path === '/workspace/') {
    console.log('[Guard] Redirecting to system dashboard')
    next('/workspace/sys/dashboard')
    return
  }
  
  // 如果已登录，直接放行（动态路由已经在登录时注入）
  console.log('[Guard] User logged in, allowing access to:', to.path)
  console.log('[Guard] Current route matched:', router.currentRoute.value.matched.length, 'components')
  next()
})

export default router

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
 */
function loadComponent(componentName: string) {
  try {
    // 解析组件名：支持多种格式
    // SystemUser -> module: System, page: User
    // SysDashboard -> module: Sys, page: Dashboard
    const match = componentName.match(/^([A-Z][a-z]*[A-Z]?[a-z]*)([A-Z][a-z]+)$/)
    
    if (!match) {
      console.warn(`[Route] Invalid component name format: ${componentName}`)
      return EmptyView
    }
    
    const modulePart = match[1].toLowerCase()  // System -> system, Sys -> sys
    const pagePart = match[2].toLowerCase()    // User -> user, Dashboard -> dashboard
    
    // 使用映射表获取实际的目录名
    const moduleDir = modulePathMap[modulePart] || modulePart
    
    const componentPath = `../views/${moduleDir}/${pagePart}/index.vue`
    console.log(`[Route] Loading component: ${componentName} from ${componentPath}`)
    
    // 动态导入组件
    return () => import(/* @vite-ignore */ componentPath)
  } catch (error) {
    console.error(`[Route] Failed to load component: ${componentName}`, error)
    return EmptyView
  }
}

export const dynamicModules = ref<any[]>([])
export const dynamicRoutes = ref<any[]>([])

export async function injectDynamicRoutes(r: ReturnType<typeof createRouter>, payload: any) {
  const mods = Array.isArray(payload?.modules) ? payload.modules : []
  const routesPayload = Array.isArray(payload?.routes) ? payload.routes : []
  dynamicModules.value = mods
  dynamicRoutes.value = routesPayload
  
  // 缓存到 Pinia store（会自动持久化到 localStorage）
  const permissionStore = usePermissionStore()
  permissionStore.setRoutes(routesPayload)
  permissionStore.setModules(mods)
  
  console.log('[Route] Starting to inject dynamic routes...')
  console.log('[Route] Modules:', mods)
  console.log('[Route] Routes payload:', routesPayload)
  
  for (const routeItem of routesPayload) {
    const moduleCode = routeItem.path
    const children = Array.isArray(routeItem.children) ? routeItem.children : []
    
    console.log(`[Route] Processing module: ${moduleCode}`)
    
    // 为每个模块自动注册 dashboard 路由
    const dashboardComponentKey = `${moduleCode.charAt(0).toUpperCase() + moduleCode.slice(1)}Dashboard`
    const dashboardComponent = loadComponent(dashboardComponentKey)
    
    console.log(`[Route] Registering dashboard route: /workspace/${moduleCode}/dashboard (component: ${dashboardComponentKey})`)
    r.addRoute('Workspace', {
      path: `${moduleCode}/dashboard`,
      name: `${moduleCode}-dashboard`,
      component: dashboardComponent,
      meta: {
        title: '主页',
        module: moduleCode
      }
    })
    
    for (const c of children) {
      const key = c.component
      const comp = loadComponent(key)
      const childPath = c.path
      
      // 注册路由：/workspace/{moduleCode}/{childPath}
      const fullPath = `${moduleCode}/${childPath}`
      console.log(`[Route] Registering route: /workspace/${fullPath}`)
      
      r.addRoute('Workspace', {
        path: fullPath,
        name: c.name,
        component: comp,
        meta: {
          ...c.meta,
          module: moduleCode
        }
      })
    }
  }
  
  // 打印所有注册的路由
  console.log('[Route] All registered routes:')
  r.getRoutes().forEach(route => {
    if (route.path.includes('workspace')) {
      console.log(`  - ${route.path} (name: ${route.name})`)
    }
  })
  
  console.log('[Route] Route injection completed')
}
