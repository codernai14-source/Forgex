/**
 * 应用全局 Store
 *
 * @description 管理应用级状态，如侧边栏、主题、语言和加载状态。
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { setLocale as setI18nLocale, type LocaleCode } from '../locales'

export const useAppStore = defineStore('app', () => {
  // ============ State ============

  /**
   * 侧边栏折叠状态。
   */
  const sidebarCollapsed = ref(false)

  /**
   * 当前主题。
   */
  const theme = ref<'light' | 'dark'>('light')

  /**
   * 当前语言。
   */
  const locale = ref<LocaleCode>('zh-CN')

  /**
   * 全局加载状态。
   */
  const loading = ref(false)

  /**
   * 表单展示模式，modal 表示弹窗，drawer 表示抽屉。
   */
  const formMode = ref<'modal' | 'drawer'>('drawer')

  // ============ 操作 ============

  /**
   * 切换侧边栏折叠状态。
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /**
   * 设置侧边栏折叠状态。
   */
  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
  }

  /**
   * 切换主题。
   */
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  /**
   * 设置主题。
   */
  function setTheme(newTheme: 'light' | 'dark') {
    theme.value = newTheme
  }

  /**
   * 切换语言。
   */
  function toggleLocale() {
    const locales: LocaleCode[] = ['zh-CN', 'en-US', 'zh-TW', 'ja-JP', 'ko-KR']
    const currentIndex = locales.indexOf(locale.value)
    const nextIndex = (currentIndex + 1) % locales.length
    const newLocale = locales[nextIndex]
    locale.value = newLocale
    setI18nLocale(newLocale)
  }

  /**
   * 设置语言。
   * @param newLocale 新的语言代码
   */
  function setLocale(newLocale: LocaleCode) {
    locale.value = newLocale
    setI18nLocale(newLocale)
  }

  /**
   * 设置全局加载状态。
   */
  function setLoading(isLoading: boolean) {
    loading.value = isLoading
  }

  return {
    // State
    sidebarCollapsed,
    theme,
    locale,
    loading,

    // 操作
    toggleSidebar,
    setSidebarCollapsed,
    toggleTheme,
    setTheme,
    toggleLocale,
    setLocale,
    setLoading,
    formMode,
    set表单Mode: (mode: 'modal' | 'drawer') => formMode.value = mode
  }
}, {
  // 持久化配置
  // persist: {
  //   key: 'forgex-app',
  //   storage: localStorage,
  //   paths: ['sidebarCollapsed', 'theme', 'locale']
  // }
})
