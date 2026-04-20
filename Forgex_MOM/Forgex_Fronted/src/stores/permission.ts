/**
 * 鏉冮檺 Store
 * 
 * @description 绠＄悊鐢ㄦ埛鏉冮檺銆佹寜閽潈闄愮瓑
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const ROUTE_CACHE_VERSION = '20260417-integration-menu-v5'

export const use权限Store = defineStore('permission', () => {
  // ============ State ============
  
  /**
   * 鎸夐挳鏉冮檺鍒楄〃
   * 渚嬪锛歔'sys:user:add', 'sys:user:edit', 'sys:user:delete']
   */
  const permissions = ref<string[]>([])
  
  /**
   * 璺敱鏉冮檺鍒楄〃
   */
  const routes = ref<any[]>([])
  
  /**
   * 妯″潡鍒楄〃
   */
  const modules = ref<any[]>([])
  
  // ============ Getters ============
  
  /**
   * 鏄惁鏈夋潈闄?
   */
  const has权限 = computed(() => {
    return (permKey: string) => {
      return permissions.value.includes(permKey)
    }
  })
  
  /**
   * 鏄惁鏈変换鎰忎竴涓潈闄?
   */
  const hasAny权限 = computed(() => {
    return (permKeys: string[]) => {
      return permKeys.some(key => permissions.value.includes(key))
    }
  })
  
  /**
   * 鏄惁鏈夋墍鏈夋潈闄?
   */
  const hasAll权限s = computed(() => {
    return (permKeys: string[]) => {
      return permKeys.every(key => permissions.value.includes(key))
    }
  })
  
  // ============ 操作 ============
  
  /**
   * 璁剧疆鏉冮檺鍒楄〃
   */
  function set权限s(perms: string[]) {
    permissions.value = perms
    
    // 鍚屾鍒?sessionStorage锛堝吋瀹规棫浠ｇ爜锛?
    sessionStorage.setItem('permissions', JSON.stringify(perms))
  }
  
  /**
   * 娣诲姞鏉冮檺
   */
  function add权限(perm: string) {
    if (!permissions.value.includes(perm)) {
      permissions.value.push(perm)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }
  
  /**
   * 绉婚櫎鏉冮檺
   */
  function remove权限(perm: string) {
    const index = permissions.value.indexOf(perm)
    if (index > -1) {
      permissions.value.splice(index, 1)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }
  
  /**
   * 璁剧疆璺敱鍒楄〃
   */
  function setRoutes(routeList: any[]) {
    routes.value = routeList
    
    // 鎸佷箙鍖栧埌 localStorage
    try {
      localStorage.setItem('fx-dynamic-routes', JSON.stringify(routeList))
      localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
    } catch (error) {
      console.error('Failed to cache routes to localStorage:', error)
    }
  }
  
  /**
   * 璁剧疆妯″潡鍒楄〃
   */
  function setModules(moduleList: any[]) {
    modules.value = moduleList
    
    // 鎸佷箙鍖栧埌 localStorage
    try {
      localStorage.setItem('fx-dynamic-modules', JSON.stringify(moduleList))
      localStorage.setItem('fx-dynamic-routes-version', ROUTE_CACHE_VERSION)
    } catch (error) {
      console.error('Failed to cache modules to localStorage:', error)
    }
  }
  
  /**
   * 浠?localStorage 鎭㈠璺敱鍜屾ā鍧?
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
   * 娓呴櫎鎵€鏈夋潈闄?
   */
  function clear权限s() {
    permissions.value = []
    routes.value = []
    modules.value = []
    
    // 娓呴櫎 sessionStorage
    sessionStorage.removeItem('permissions')
    
    // 娓呴櫎 localStorage 涓殑璺敱鍜屾ā鍧楃紦瀛?
    localStorage.removeItem('fx-dynamic-routes')
    localStorage.removeItem('fx-dynamic-modules')
    localStorage.removeItem('fx-dynamic-routes-version')
  }
  
  /**
   * 浠?sessionStorage 鎭㈠鏉冮檺锛堢敤浜庨〉闈㈠埛鏂帮級
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
  
  // ============ 鍒濆鍖?============
  
  // 椤甸潰鍔犺浇鏃跺皾璇曚粠 sessionStorage 鎭㈠
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
  // 鎸佷箙鍖栭厤缃紙鍙€夛級
  // persist: {
  //   key: 'forgex-permission',
  //   storage: sessionStorage,
  //   paths: ['permissions', 'routes', 'modules']
  // }
})
