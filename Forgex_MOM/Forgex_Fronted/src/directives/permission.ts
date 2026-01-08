/**
 * 权限指令
 * 
 * 用于控制按钮级别的权限显示
 * 
 * @example
 * ```vue
 * <a-button v-permission="'sys:user:add'">新增</a-button>
 * <a-button v-permission="'sys:user:edit'">编辑</a-button>
 * ```
 */
import { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

/**
 * 检查是否有权限
 * @param permKey 权限标识
 * @returns 是否有权限
 */
function hasPermission(permKey: string): boolean {
  const permissionStore = usePermissionStore()
  
  // 添加调试日志
  console.log('[Permission] Checking permission:', permKey)
  console.log('[Permission] hasPermission type:', typeof permissionStore.hasPermission)
  console.log('[Permission] hasPermission.value type:', typeof permissionStore.hasPermission.value)
  
  // 兼容性检查：支持多种调用方式
  const checkFn = permissionStore.hasPermission
  
  // 如果 hasPermission 本身就是函数（旧版本）
  if (typeof checkFn === 'function') {
    return checkFn(permKey)
  }
  
  // 如果 hasPermission.value 是函数（新版本 computed）
  if (checkFn && typeof checkFn.value === 'function') {
    return checkFn.value(permKey)
  }
  
  // 降级：直接检查 permissions 数组
  console.warn('[Permission] Fallback to direct permissions check')
  return permissionStore.permissions.includes(permKey)
}

/**
 * 权限指令
 */
export const permission: Directive = {
  /**
   * 元素挂载时检查权限
   */
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    const { value } = binding
    
    // 如果没有权限，移除元素
    if (value && !hasPermission(value)) {
      el.parentNode?.removeChild(el)
    }
  },
  
  /**
   * 元素更新时检查权限
   */
  updated(el: HTMLElement, binding: DirectiveBinding<string>) {
    const { value, oldValue } = binding
    
    // 如果权限标识改变，重新检查
    if (value !== oldValue) {
      if (value && !hasPermission(value)) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}

/**
 * 在 Vue 应用中注册权限指令
 * 
 * @example
 * ```typescript
 * import { createApp } from 'vue'
 * import { permission } from './directives/permission'
 * 
 * const app = createApp(App)
 * app.directive('permission', permission)
 * ```
 */
export default permission
