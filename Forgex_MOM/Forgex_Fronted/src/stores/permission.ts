/**
 * 权限 Store
 *
 * @description 管理用户权限、按钮权限、动态路由和模块缓存。
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const ROUTE_CACHE_VERSION = '20260422-user-third-party-menu-v1'

export const use权限Store = defineStore('permission', () => {
  // ============ State ============

  /**
   * 按钮权限列表。
   * 例如：['sys:user:add', 'sys:user:edit', 'sys:user:delete']
   */
  const permissions = ref<string[]>([])

  /**
   * 路由权限列表。
   */
  const routes = ref<any[]>([])

  /**
   * 模块列表。
   */
  const modules = ref<any[]>([])

  // ============ Getters ============

  /**
   * 是否拥有指定权限。
   */
  const has权限 = computed(() => {
    return (permKey: string) => {
      return permissions.value.includes(permKey)
    }
  })

  /**
   * 是否拥有任意一个权限。
   */
  const hasAny权限 = computed(() => {
    return (permKeys: string[]) => {
      return permKeys.some(key => permissions.value.includes(key))
    }
  })

  /**
   * 是否拥有全部权限。
   */
  const hasAll权限s = computed(() => {
    return (permKeys: string[]) => {
      return permKeys.every(key => permissions.value.includes(key))
    }
  })

  // ============ 操作 ============

  /**
   * 设置权限列表。
   */
  function set权限s(perms: string[]) {
    permissions.value = perms

    // 保存到 sessionStorage。
    sessionStorage.setItem('permissions', JSON.stringify(perms))
  }

  /**
   * 添加权限。
   */
  function add权限(perm: string) {
    if (!permissions.value.includes(perm)) {
      permissions.value.push(perm)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }

  /**
   * 移除权限。
   */
  function remove权限(perm: string) {
    const index = permissions.value.indexOf(perm)
    if (index > -1) {
      permissions.value.splice(index, 1)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }

  /**
   * 设置路由列表。
   */
  function setRoutes(routeList: any[]) {
    routes.value = routeList

    // 持久化到 localStorage。
    try {
      localStorage.setItem('fx-dynamic-routes', JSON.stringify(routeList))
      localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
    } catch (error) {
      console.error('Failed to cache routes to localStorage:', error)
    }
  }

  /**
   * 设置模块列表。
   */
  function setModules(moduleList: any[]) {
    modules.value = moduleList

    // 持久化到 localStorage。
    try {
      localStorage.setItem('fx-dynamic-modules', JSON.stringify(moduleList))
      localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
    } catch (error) {
      console.error('Failed to cache modules to localStorage:', error)
    }
  }

  /**
   * 从 localStorage 恢复路由和模块。
   */
  function restoreRoutesAndModules() {
    try {
      const cacheVersion = localStorage.getItem('fx-dynamic-routes-version')
      if (cacheVersion !== ROUTE_CACHE_VERSION) {
        localStorage.removeItem('fx-dynamic-routes')
        localStorage.removeItem('fx-dynamic-modules')
        localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
        routes.value = []
        modules.value = []
        return {
          routes: [],
          modules: []
        }
      }

      const cachedRoutes = localStorage.getItem('fx-dynamic-routes')
      const cachedModules = localStorage.getItem('fx-dynamic-modules')

      if (cachedRoutes) {
        routes.value = JSON.parse(cachedRoutes)
      }

      if (cachedModules) {
        modules.value = JSON.parse(cachedModules)
      }

      return {
        routes: routes.value,
        modules: modules.value
      }
    } catch (error) {
      console.error('Failed to restore routes and modules from localStorage:', error)
      return {
        routes: [],
        modules: []
      }
    }
  }

  /**
   * 清空全部权限和动态缓存。
   */
  function clear权限s() {
    permissions.value = []
    routes.value = []
    modules.value = []

    // 清理 sessionStorage。
    sessionStorage.removeItem('permissions')

    // 清理 localStorage 中的动态路由缓存。
    localStorage.removeItem('fx-dynamic-routes')
    localStorage.removeItem('fx-dynamic-modules')
    localStorage.removeItem('fx-dynamic-routes-version')
  }

  /**
   * 从 sessionStorage 恢复权限。
   */
  function restoreFromSession() {
    const permsStr = sessionStorage.getItem('permissions')
    if (permsStr) {
      try {
        permissions.value = JSON.parse(permsStr)
      } catch (error) {
        console.error('Failed to parse permissions from sessionStorage:', error)
        permissions.value = []
      }
    }
  }

  // ============ 初始化 ============

  // 页面加载时尝试从 sessionStorage 恢复。
  restoreFromSession()

  return {
    // State
    permissions,
    routes,
    modules,

    // Getters
    has权限,
    hasAny权限,
    hasAll权限s,

    // 操作
    set权限s,
    add权限,
    remove权限,
    setRoutes,
    setModules,
    clear权限s,
    restoreFromSession,
    restoreRoutesAndModules
  }
}, {
  // 持久化配置
  // persist: {
  //   key: 'forgex-permission',
  //   storage: sessionStorage,
  //   paths: ['permissions', 'routes', 'modules']
  // }
})
