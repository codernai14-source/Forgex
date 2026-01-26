import { usePermissionStore } from '@/stores/permission';
/**
 * 检查是否有权限
 * @param permKey 权限标识
 * @returns 是否有权限
 */
function hasPermission(permKey) {
    const permissionStore = usePermissionStore();
    // 由于 store 中 hasPermission 是 computed，
    // 这里统一通过 .value 调用
    const checker = permissionStore.hasPermission;
    if (typeof checker === 'function') {
        return checker(permKey);
    }
    if (checker && typeof checker.value === 'function') {
        return checker.value(permKey);
    }
    return false;
}
/**
 * 权限指令
 */
export const permission = {
    /**
     * 元素挂载时检查权限
     */
    mounted(el, binding) {
        const { value } = binding;
        const originalDisplay = el.style.display;
        el.__vPermissionOriginalDisplay = originalDisplay;
        if (value && !hasPermission(value)) {
            el.style.display = 'none';
            return;
        }
        el.style.display = originalDisplay;
    },
    /**
     * 元素更新时检查权限
     */
    updated(el, binding) {
        const { value } = binding;
        const originalDisplay = el.__vPermissionOriginalDisplay ?? '';
        if (value && !hasPermission(value)) {
            el.style.display = 'none';
            return;
        }
        el.style.display = originalDisplay;
    }
};
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
export default permission;
