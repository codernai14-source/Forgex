import { createRouter, createWebHistory } from 'vue-router'
import { ref } from 'vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/auth/login/index.vue') },
  { path: '/init', component: () => import('../views/auth/init-wizard/index.vue') },
  {
    path: '/workspace',
    name: 'Workspace',
    component: () => import('../layouts/MainLayout.vue'),
    children: []
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
router.beforeEach(async (to, from, next) => {
  console.log('[Guard] Navigating to:', to.path, 'from:', from.path)
  
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  
  console.log('[Guard] Session check - account:', account, 'tenantId:', tenantId)
  console.log('[Guard] Dynamic routes count:', dynamicRoutes.value.length)
  
  // 如果访问登录页或初始化页，直接放行
  if (to.path === '/login' || to.path === '/init') {
    next()
    return
  }
  
  // 如果未登录，跳转到登录页
  if (!account || !tenantId) {
    console.log('[Guard] Not logged in, redirecting to login')
    next('/login')
    return
  }
  
  // 如果已登录但动态路由未加载，尝试从 localStorage 恢复
  if (dynamicRoutes.value.length === 0) {
    console.log('[Guard] Dynamic routes not loaded, trying to restore from cache')
    const cachedRoutes = localStorage.getItem('fx-dynamic-routes')
    const cachedModules = localStorage.getItem('fx-dynamic-modules')
    
    if (cachedRoutes && cachedModules) {
      try {
        const routes = JSON.parse(cachedRoutes)
        const modules = JSON.parse(cachedModules)
        await injectDynamicRoutes(router, { routes, modules })
        console.log('[Guard] Routes restored from cache, re-navigating')
        // 重新导航到目标路由
        next({ ...to, replace: true })
        return
      } catch (e) {
        console.error('Failed to restore dynamic routes:', e)
      }
    }
    
    // 如果无法恢复动态路由，跳转到登录页
    console.log('[Guard] Cannot restore routes, redirecting to login')
    next('/login')
    return
  }
  
  console.log('[Guard] All checks passed, proceeding')
  next()
})

export default router

const EmptyView = { template: '<div style="padding:16px;color:#9ca3af;">暂无页面，请稍后</div>' }

const componentMap: Record<string, any> = {
  SystemDashboard: () => import('../views/system/dashboard/index.vue'),
  SystemUser: () => import('../views/system/user/index.vue'),
  SystemRole: () => import('../views/system/role/index.vue'),
  SystemModule: () => import('../views/system/module/index.vue'),
  SystemMenu: () => import('../views/system/menu/index.vue')
}

export const dynamicModules = ref<any[]>([])
export const dynamicRoutes = ref<any[]>([])

export async function injectDynamicRoutes(r: ReturnType<typeof createRouter>, payload: any) {
  const mods = Array.isArray(payload?.modules) ? payload.modules : []
  const routesPayload = Array.isArray(payload?.routes) ? payload.routes : []
  dynamicModules.value = mods
  dynamicRoutes.value = routesPayload
  
  // 缓存到 localStorage
  try {
    localStorage.setItem('fx-dynamic-routes', JSON.stringify(routesPayload))
    localStorage.setItem('fx-dynamic-modules', JSON.stringify(mods))
  } catch (e) {
    console.error('Failed to cache dynamic routes:', e)
  }
  
  console.log('[Route] Starting to inject dynamic routes...')
  console.log('[Route] Modules:', mods)
  console.log('[Route] Routes payload:', routesPayload)
  
  for (const routeItem of routesPayload) {
    const moduleCode = routeItem.path
    const children = Array.isArray(routeItem.children) ? routeItem.children : []
    
    console.log(`[Route] Processing module: ${moduleCode}`)
    
    for (const c of children) {
      const key = c.component
      const comp = componentMap[key] || EmptyView
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
}
