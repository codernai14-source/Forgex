/**
 * 多语言配置
 * 
 * 统一管理系统的多语言配置
 */
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

// 语言包
const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
}

// 获取存储的语言设置
const storedLocale = localStorage.getItem('fx-locale')
// 获取浏览器语言
const browserLocale = navigator.language || 'zh-CN'
// 确定初始语言
const initialLocale = storedLocale || (browserLocale.startsWith('en') ? 'en-US' : 'zh-CN')

// 创建 i18n 实例
const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: initialLocale, // 当前语言
  fallbackLocale: 'zh-CN', // 回退语言
  messages, // 语言包
  globalInjection: true, // 全局注入 $t 函数
})

/**
 * 切换语言
 * @param locale 语言代码（'zh-CN' 或 'en-US'）
 */
export function setLocale(locale: 'zh-CN' | 'en-US') {
  i18n.global.locale.value = locale
  localStorage.setItem('fx-locale', locale)
  // 更新 HTML lang 属性
  document.documentElement.lang = locale
}

/**
 * 获取当前语言
 */
export function getLocale(): string {
  return i18n.global.locale.value
}

/**
 * 获取所有可用语言
 */
export function getAvailableLocales() {
  return [
    { value: 'zh-CN', label: '简体中文' },
    { value: 'en-US', label: 'English' },
  ]
}

export default i18n
