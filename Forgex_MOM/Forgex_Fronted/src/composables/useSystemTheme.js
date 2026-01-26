import { ref, onMounted, onUnmounted } from 'vue';
/**
 * 系统主题检测 Composable
 *
 * 检测操作系统的主题偏好（浅色/暗色），并监听系统主题变化。
 * 支持 Windows、macOS、Linux 等主流操作系统的主题检测。
 *
 * @returns 包含 systemTheme 的对象
 * - systemTheme: 响应式的系统主题值（'light' 或 'dark'）
 *
 * @remarks
 * 实现原理：
 * - 使用 CSS Media Query `prefers-color-scheme` 检测系统主题
 * - 通过 MediaQueryList 监听系统主题变化
 * - 自动处理组件挂载和卸载时的监听器管理
 *
 * 兼容性：
 * - 现代浏览器：使用 addEventListener/removeEventListener
 * - 旧版浏览器：降级使用 addListener/removeListener
 * - SSR 环境：安全处理，不会报错
 *
 * @example
 * ```typescript
 * // 在组件中使用
 * const { systemTheme } = useSystemTheme()
 *
 * watch(systemTheme, (newTheme) => {
 *   console.log('系统主题变更为:', newTheme)
 * })
 *
 * // 配合 resolveThemeMode 使用
 * const resolvedMode = computed(() =>
 *   resolveThemeMode(layoutConfig.value.themeMode, systemTheme.value)
 * )
 * ```
 */
export function useSystemTheme() {
    const systemTheme = ref('light');
    let mediaQuery = null;
    /**
     * 更新系统主题
     *
     * 读取当前系统主题偏好并更新 systemTheme 值。
     * 在 SSR 环境中安全返回，不执行任何操作。
     */
    const updateSystemTheme = () => {
        if (typeof window === 'undefined')
            return;
        const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)');
        systemTheme.value = darkModeQuery.matches ? 'dark' : 'light';
    };
    /**
     * 媒体查询变化处理
     *
     * 当系统主题发生变化时，更新 systemTheme 值。
     *
     * @param e - MediaQueryListEvent 事件对象
     */
    const handleChange = (e) => {
        systemTheme.value = e.matches ? 'dark' : 'light';
    };
    onMounted(() => {
        if (typeof window === 'undefined')
            return;
        // 初始化系统主题
        updateSystemTheme();
        // 监听系统主题变化
        mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        // 使用 addEventListener（现代浏览器）
        if (mediaQuery.addEventListener) {
            mediaQuery.addEventListener('change', handleChange);
        }
        else {
            // 降级方案（旧版浏览器）
            mediaQuery.addListener(handleChange);
        }
    });
    onUnmounted(() => {
        if (!mediaQuery)
            return;
        // 移除监听器
        if (mediaQuery.removeEventListener) {
            mediaQuery.removeEventListener('change', handleChange);
        }
        else {
            // 降级方案（旧版浏览器）
            mediaQuery.removeListener(handleChange);
        }
    });
    return {
        systemTheme
    };
}
/**
 * 解析主题模式
 *
 * 将用户选择的主题模式转换为实际的浅色/暗色模式。
 * 当用户选择 'system' 时，使用系统主题；否则使用用户指定的模式。
 *
 * @param themeMode - 用户选择的主题模式（'light' | 'dark' | 'system'）
 * @param systemTheme - 当前系统主题（'light' | 'dark'）
 * @returns 解析后的实际主题模式（'light' | 'dark'）
 *
 * @remarks
 * 解析规则：
 * - 'light' → 'light'（强制浅色）
 * - 'dark' → 'dark'（强制暗色）
 * - 'system' → systemTheme（跟随系统）
 *
 * @example
 * ```typescript
 * const { systemTheme } = useSystemTheme()
 *
 * // 用户选择跟随系统
 * resolveThemeMode('system', systemTheme.value) // 'light' 或 'dark'
 *
 * // 用户强制选择浅色
 * resolveThemeMode('light', systemTheme.value) // 'light'
 *
 * // 用户强制选择暗色
 * resolveThemeMode('dark', systemTheme.value) // 'dark'
 *
 * // 在 computed 中使用
 * const resolvedMode = computed(() =>
 *   resolveThemeMode(userThemeMode.value, systemTheme.value)
 * )
 * ```
 */
export function resolveThemeMode(themeMode, systemTheme) {
    if (themeMode === 'system') {
        return systemTheme;
    }
    return themeMode;
}
