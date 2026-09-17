/**
 * Permission Store
 *
 * @description Manages user permissions, route permissions, module cache, and related state.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const ROUTE_CACHE_VERSION = '20260823-live-permission-refresh-v1'

export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<string[]>([])
  const routes = ref<any[]>([])
  const modules = ref<any[]>([])

  const hasPermission = computed(() => {
    return (permKey: string) => permissions.value.includes(permKey)
  })

  const hasAnyPermission = computed(() => {
    return (permKeys: string[]) => permKeys.some(key => permissions.value.includes(key))
  })

  const hasAllPermissions = computed(() => {
    return (permKeys: string[]) => permKeys.every(key => permissions.value.includes(key))
  })

  function setPermissions(perms: string[]) {
    permissions.value = perms
    sessionStorage.setItem('permissions', JSON.stringify(perms))
  }

  function addPermission(perm: string) {
    if (!permissions.value.includes(perm)) {
      permissions.value.push(perm)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }

  function removePermission(perm: string) {
    const index = permissions.value.indexOf(perm)
    if (index > -1) {
      permissions.value.splice(index, 1)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }

  function setRoutes(routeList: any[]) {
    routes.value = routeList
    try {
      localStorage.setItem('fx-dynamic-routes', JSON.stringify(routeList))
      localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
    } catch (error) {
      console.error('Failed to cache routes to localStorage:', error)
    }
  }

  function setModules(moduleList: any[]) {
    modules.value = moduleList
    try {
      localStorage.setItem('fx-dynamic-modules', JSON.stringify(moduleList))
      localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
    } catch (error) {
      console.error('Failed to cache modules to localStorage:', error)
    }
  }

  function restoreRoutesAndModules() {
    try {
      const cacheVersion = localStorage.getItem('fx-dynamic-routes-version')
      if (cacheVersion !== ROUTE_CACHE_VERSION) {
        localStorage.removeItem('fx-dynamic-routes')
        localStorage.removeItem('fx-dynamic-modules')
        localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
        routes.value = []
        modules.value = []
        return { routes: [], modules: [] }
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
      return { routes: [], modules: [] }
    }
  }

  function clearPermissions() {
    permissions.value = []
    routes.value = []
    modules.value = []
    sessionStorage.removeItem('permissions')
    localStorage.removeItem('fx-dynamic-routes')
    localStorage.removeItem('fx-dynamic-modules')
    localStorage.removeItem('fx-dynamic-routes-version')
  }

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

  restoreFromSession()

  return {
    permissions,
    routes,
    modules,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    setPermissions,
    addPermission,
    removePermission,
    setRoutes,
    setModules,
    clearPermissions,
    restoreFromSession,
    restoreRoutesAndModules
  }
}, {
  // persist: {
  //   key: 'forgex-permission',
  //   storage: sessionStorage,
  //   paths: ['permissions', 'routes', 'modules']
  // }
})
