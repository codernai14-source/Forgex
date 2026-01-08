/**
 * 权限 Store
 * 
 * @description 管理用户权限、按钮权限等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const usePermissionStore = defineStore('permission', () => {
  // ============ State ============
  
  /**
   * 按钮权限列表
   * 例如：['sys:user:add', 'sys:user:edit', 'sys:user:delete']
   */
  const permissions = ref<string[]>([])
  
  /**
   * 路由权限列表
   */
  const routes = ref<any[]>([])
  
  /**
   * 模块列表
   */
  const modules = ref<any[]>([])
  
  // ============ Getters ============
  
  /**
   * 是否有权限
   */
  const hasPermission = computed(() => {
    return (permKey: string) => {
      return permissions.value.includes(permKey)
    }
  })
  
  /**
   * 是否有任意一个权限
   */
  const hasAnyPermission = computed(() => {
    return (permKeys: string[]) => {
      return permKeys.some(key => permissions.value.includes(key))
    }
  })
  
  /**
   * 是否有所有权限
   */
  const hasAllPermissions = computed(() => {
    return (permKeys: string[]) => {
      return permKeys.every(key => permissions.value.includes(key))
    }
  })
  
  // ============ Actions ============
  
  /**
   * 设置权限列表
   */
  function setPermissions(perms: string[]) {
    permissions.value = perms
    
    // 同步到 sessionStorage（兼容旧代码）
    sessionStorage.setItem('permissions', JSON.stringify(perms))
  }
  
  /**
   * 添加权限
   */
  function addPermission(perm: string) {
    if (!permissions.value.includes(perm)) {
      permissions.value.push(perm)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }
  
  /**
   * 移除权限
   */
  function removePermission(perm: string) {
    const index = permissions.value.indexOf(perm)
    if (index > -1) {
      permissions.value.splice(index, 1)
      sessionStorage.setItem('permissions', JSON.stringify(permissions.value))
    }
  }
  
  /**
   * 设置路由列表
   */
  function setRoutes(routeList: any[]) {
    routes.value = routeList
    
    // 持久化到 localStorage
    try {
      localStorage.setItem('fx-dynamic-routes', JSON.stringify(routeList))
    } catch (error) {
      console.error('Failed to cache routes to localStorage:', error)
    }
  }
  
  /**
   * 设置模块列表
   */
  function setModules(moduleList: any[]) {
    modules.value = moduleList
    
    // 持久化到 localStorage
    try {
      localStorage.setItem('fx-dynamic-modules', JSON.stringify(moduleList))
    } catch (error) {
      console.error('Failed to cache modules to localStorage:', error)
    }
  }
  
  /**
   * 从 localStorage 恢复路由和模块
   */
  function restoreRoutesAndModules() {
    try {
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
   * 清除所有权限
   */
  function clearPermissions() {
    permissions.value = []
    routes.value = []
    modules.value = []
    
    // 清除 sessionStorage
    sessionStorage.removeItem('permissions')
    
    // 清除 localStorage 中的路由和模块缓存
    localStorage.removeItem('fx-dynamic-routes')
    localStorage.removeItem('fx-dynamic-modules')
  }
  
  /**
   * 从 sessionStorage 恢复权限（用于页面刷新）
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
  
  // 页面加载时尝试从 sessionStorage 恢复
  restoreFromSession()
  
  return {
    // State
    permissions,
    routes,
    modules,
    
    // Getters
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    
    // Actions
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
  // 持久化配置（可选）
  // persist: {
  //   key: 'forgex-permission',
  //   storage: sessionStorage,
  //   paths: ['permissions', 'routes', 'modules']
  // }
})
