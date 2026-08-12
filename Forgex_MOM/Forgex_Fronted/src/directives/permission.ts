/**
 * Permission directive
 *
 * Shows or hides elements based on button permissions.
 *
 * @example
 * ```vue
 * <a-button v-permission="'sys:user:add'">新增</a-button>
 * <a-button v-permission="'sys:user:edit'">编辑</a-button>
 * ```
 */
import { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

function hasPermission(permKey: string): boolean {
  const permissionStore = usePermissionStore()
  const checker = permissionStore.hasPermission
  if (typeof checker === 'function') {
    return checker(permKey)
  }
  if (checker && typeof checker.value === 'function') {
    return checker.value(permKey)
  }
  return false
}

export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    const { value } = binding
    const originalDisplay = el.style.display
    ;(el as any).__vPermissionOriginalDisplay = originalDisplay

    if (value && !hasPermission(value)) {
      el.style.display = 'none'
      return
    }

    el.style.display = originalDisplay
  },

  updated(el: HTMLElement, binding: DirectiveBinding<string>) {
    const { value } = binding
    const originalDisplay = (el as any).__vPermissionOriginalDisplay ?? ''
    if (value && !hasPermission(value)) {
      el.style.display = 'none'
      return
    }

    el.style.display = originalDisplay
  }
}

export default permission
