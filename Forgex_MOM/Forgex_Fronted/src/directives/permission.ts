/**
 * 权限指令
 *
 * 用于根据按钮权限控制元素显示。
 *
 * @example
 * ```vue
 * <a-button v-permission="'sys:user:add'">新增</a-button>
 * <a-button v-permission="'sys:user:edit'">编辑</a-button>
 * ```
 */
import { Directive, DirectiveBinding } from 'vue'
import { use权限Store } from '@/stores/permission'

/**
 * 判断是否拥有指定权限。
 * @param permKey 权限标识
 * @returns 是否拥有指定权限。
 */
function has权限(permKey: string): boolean {
  const permissionStore = use权限Store()

  // Pinia setup store 中的 computed 在实例上会自动解包，这里兼容未解包的情况。
  const checker = permissionStore.has权限
  if (typeof checker === 'function') {
    return checker(permKey)
  }
  if (checker && typeof checker.value === 'function') {
    return checker.value(permKey)
  }
  return false
}

/**
 * 权限指令。
 */
export const permission: Directive = {
  /**
   * 元素挂载时检查权限。
   */
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    const { value } = binding

    const originalDisplay = el.style.display
    ;(el as any).__v权限OriginalDisplay = originalDisplay

    if (value && !has权限(value)) {
      el.style.display = 'none'
      return
    }

    el.style.display = originalDisplay
  },

  /**
   * 绑定值更新时重新检查权限。
   */
  updated(el: HTMLElement, binding: DirectiveBinding<string>) {
    const { value } = binding

    const originalDisplay = (el as any).__v权限OriginalDisplay ?? ''
    if (value && !has权限(value)) {
      el.style.display = 'none'
      return
    }

    el.style.display = originalDisplay
  }
}

/**
 * Vue 权限指令。
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
