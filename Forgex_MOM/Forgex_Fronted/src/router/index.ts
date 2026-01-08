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
  
  // 如果动态路由为空，尝试从 localStorage 恢复
  if (dynamicRoutes.value.length === 0) {
    console.log('[Guard] Dynamic routes empty, trying to restore from localStorage')
    try {
      const cachedRoutes = localStorage.getItem('fx-dynamic-routes')
      const cachedModules = localStorage.getItem('fx-dynamic-modules')
      
      if (cachedRoutes && cachedModules) {
        const routesPayload = JSON.parse(cachedRoutes)
        const modulesPayload = JSON.parse(cachedModules)
        
        console.log('[Guard] Restoring routes from cache:', routesPayload)
        
        // 重新注入动态路由
        await injectDynamicRoutes(router, {
          routes: routesPayload,
          modules: modulesPayload
        })
        
        console.log('[Guard] Routes restored, redirecting to:', to.fullPath)
        // 路由已恢复，重新导航到目标路径
        next({ ...to, replace: true })
        return
      } else {
        console.log('[Guard] No cached routes found, redirecting to login')
        // 没有缓存的路由，需要重新登录
        next('/login')
        return
      }
    } catch (error) {
      console.error('[Guard] Failed to restore routes:', error)
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
  console.log('[Guard] User logged in, allowing access')
  next()
})

export default router

const EmptyView = { template: '<div style="padding:16px;color:#9ca3af;">暂无页面，请稍后</div>' }

const componentMap: Record<string, any> = {
  SystemDashboard: () => import('../views/system/dashboard/index.vue'),
  SystemUser: () => import('../views/system/user/index.vue'),
  SystemRole: () => import('../views/system/role/index.vue'),
  SystemModule: () => import('../views/system/module/index.vue'),
  SystemMenu: () => import('../views/system/menu/index.vue'),
  SystemDepartment: () => import('../views/system/department/index.vue'),
  SystemPosition: () => import('../views/system/position/index.vue')
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
    
    // 为每个模块自动注册 dashboard 路由
    const dashboardComponentKey = `${moduleCode.charAt(0).toUpperCase() + moduleCode.slice(1)}Dashboard`
    const dashboardComponent = componentMap[dashboardComponentKey] || componentMap['SystemDashboard']
    
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
  
  // 等待路由注册完成
  await r.isReady()
  
  // 打印所有注册的路由
  console.log('[Route] All registered routes:')
  r.getRoutes().forEach(route => {
    if (route.path.includes('workspace')) {
      console.log(`  - ${route.path} (name: ${route.name})`)
    }
  })
  
  console.log('[Route] Route injection completed')
}
