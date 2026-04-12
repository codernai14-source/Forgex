/**
 * 搴旂敤鍏ㄥ眬 Store
 * 
 * @description 绠＄悊搴旂敤鍏ㄥ眬鐘舵€侊紝濡備富棰樸€佽瑷€銆佷晶杈规爮绛?
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { setLocale as setI18nLocale, type LocaleCode } from '../locales'

export const useAppStore = defineStore('app', () => {
  // ============ State ============
  
  /**
   * 渚ц竟鏍忔槸鍚︽姌鍙?
   */
  const sidebarCollapsed = ref(false)
  
  /**
   * 褰撳墠涓婚
   */
  const theme = ref<'light' | 'dark'>('light')
  
  /**
   * 褰撳墠璇█
   */
  const locale = ref<LocaleCode>('zh-CN')
  
  /**
   * 鍔犺浇鐘舵€?
   */
  const loading = ref(false)

  /**
   * 琛ㄥ崟妯″紡锛歮odal-寮圭獥锛宒rawer-鎶藉眽
   */
  const formMode = ref<'modal' | 'drawer'>('drawer')
  
  // ============ 操作 ============
  
  /**
   * 鍒囨崲渚ц竟鏍忔姌鍙犵姸鎬?
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  /**
   * 璁剧疆渚ц竟鏍忔姌鍙犵姸鎬?
   */
  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
  }
  
  /**
   * 鍒囨崲涓婚
   */
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }
  
  /**
   * 璁剧疆涓婚
   */
  function setTheme(newTheme: 'light' | 'dark') {
    theme.value = newTheme
  }
  
  /**
   * 鍒囨崲璇█
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
   * 璁剧疆璇█
   * @param newLocale 鏂扮殑璇█浠ｇ爜
   */
  function setLocale(newLocale: LocaleCode) {
    locale.value = newLocale
    setI18nLocale(newLocale)
  }
  
  /**
   * 璁剧疆鍔犺浇鐘舵€?
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
  // 鎸佷箙鍖栭厤缃紙鍙€夛級
  // persist: {
  //   key: 'forgex-app',
  //   storage: localStorage,
  //   paths: ['sidebarCollapsed', 'theme', 'locale']
  // }
})
