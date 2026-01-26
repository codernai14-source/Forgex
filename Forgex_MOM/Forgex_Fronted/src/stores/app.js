/**
 * 应用全局 Store
 *
 * @description 管理应用全局状态，如主题、语言、侧边栏等
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { setLocale as setI18nLocale } from '../locales';
export const useAppStore = defineStore('app', () => {
    // ============ State ============
    /**
     * 侧边栏是否折叠
     */
    const sidebarCollapsed = ref(false);
    /**
     * 当前主题
     */
    const theme = ref('light');
    /**
     * 当前语言
     */
    const locale = ref('zh-CN');
    /**
     * 加载状态
     */
    const loading = ref(false);
    /**
     * 表单模式：modal-弹窗，drawer-抽屉
     */
    const formMode = ref('drawer');
    // ============ Actions ============
    /**
     * 切换侧边栏折叠状态
     */
    function toggleSidebar() {
        sidebarCollapsed.value = !sidebarCollapsed.value;
    }
    /**
     * 设置侧边栏折叠状态
     */
    function setSidebarCollapsed(collapsed) {
        sidebarCollapsed.value = collapsed;
    }
    /**
     * 切换主题
     */
    function toggleTheme() {
        theme.value = theme.value === 'light' ? 'dark' : 'light';
    }
    /**
     * 设置主题
     */
    function setTheme(newTheme) {
        theme.value = newTheme;
    }
    /**
     * 切换语言
     */
    function toggleLocale() {
        const locales = ['zh-CN', 'en-US', 'zh-TW', 'ja-JP', 'ko-KR'];
        const currentIndex = locales.indexOf(locale.value);
        const nextIndex = (currentIndex + 1) % locales.length;
        const newLocale = locales[nextIndex];
        locale.value = newLocale;
        setI18nLocale(newLocale);
    }
    /**
     * 设置语言
     * @param newLocale 新的语言代码
     */
    function setLocale(newLocale) {
        locale.value = newLocale;
        setI18nLocale(newLocale);
    }
    /**
     * 设置加载状态
     */
    function setLoading(isLoading) {
        loading.value = isLoading;
    }
    return {
        // State
        sidebarCollapsed,
        theme,
        locale,
        loading,
        // Actions
        toggleSidebar,
        setSidebarCollapsed,
        toggleTheme,
        setTheme,
        toggleLocale,
        setLocale,
        setLoading,
        formMode,
        setFormMode: (mode) => formMode.value = mode
    };
}, {
// 持久化配置（可选）
// persist: {
//   key: 'forgex-app',
//   storage: localStorage,
//   paths: ['sidebarCollapsed', 'theme', 'locale']
// }
});
