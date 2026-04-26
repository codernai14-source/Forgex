/**
 * 权限指令
 * 
 * 鐢ㄤ簬鎺у埗鎸夐挳绾у埆鐨勬潈闄愭樉绀?
 * 
 * @example
 * ```vue
 * <a-button v-permission="'sys:user:add'">鏂板</a-button>
 * <a-button v-permission="'sys:user:edit'">缂栬緫</a-button>
 * ```
 */
import { Directive, DirectiveBinding } from 'vue'
import { use权限Store } from '@/stores/permission'

/**
 * 妫€鏌ユ槸鍚︽湁鏉冮檺
 * @param permKey 权限标识
 * @returns 鏄惁鏈夋潈闄?
 */
function has权限(permKey: string): boolean {
  const permissionStore = use权限Store()
  
  // 鐢变簬 store 涓?has权限 鏄?computed锛?
  // 杩欓噷缁熶竴閫氳繃 .value 璋冪敤
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
 * 权限指令
 */
export const permission: Directive = {
  /**
   * 鍏冪礌鎸傝浇鏃舵鏌ユ潈闄?
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
   * 鍏冪礌鏇存柊鏃舵鏌ユ潈闄?
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
 * 鍦?Vue 搴旂敤涓敞鍐屾潈闄愭寚浠?
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
