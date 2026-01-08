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

/**
 * 检查是否有权限
 * @param permKey 权限标识
 * @returns 是否有权限
 */
function hasPermission(permKey: string): boolean {
  // 从 sessionStorage 获取用户权限列表
  const permissions = sessionStorage.getItem('permissions')
  
  if (!permissions) {
    return false
  }
  
  try {
    const permList: string[] = JSON.parse(permissions)
    return permList.includes(permKey)
  } catch (error) {
    console.error('Failed to parse permissions:', error)
    return false
  }
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
