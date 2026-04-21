/**
 * 多语言配置入口。
 * <p>
 * 统一管理系统的语言包注册、当前语言切换与浏览器默认语言初始化逻辑。
 * </p>
 */
import { createI18n } from 'vue-i18n'
import { clearDictCache } from '@/hooks/useDict'
import zhCN from './zh-CN'
import enUS from './en-US'
import zhTW from './zh-TW'
import jaJP from './ja-JP'
import koKR from './ko-KR'

/**
 * 支持的语言编码类型。
 */
export type LocaleCode = 'zh-CN' | 'en-US' | 'zh-TW' | 'ja-JP' | 'ko-KR'

// 语言包映射。
const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'zh-TW': zhTW,
  'ja-JP': jaJP,
  'ko-KR': koKR,
}

// 获取本地持久化的语言设置。
const storedLocale = localStorage.getItem('fx-locale')
// 获取浏览器语言。
const browserLocale = navigator.language || 'zh-CN'
// 计算初始化语言，当前默认优先英文，否则回退中文。
const initialLocale = storedLocale || (browserLocale.startsWith('en') ? 'en-US' : 'zh-CN')

// 创建 i18n 实例。
const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: initialLocale, // 当前语言
  fallbackLocale: 'zh-CN', // 回退语言
  messages, // 语言包集合
  globalInjection: true, // 全局注入 $t 方法
})

/**
 * 切换语言。
 *
 * @param locale 目标语言编码
 */
export function setLocale(locale: LocaleCode) {
  i18n.global.locale.value = locale
  localStorage.setItem('fx-locale', locale)
  // 更新 HTML lang 属性，便于浏览器和辅助工具识别当前语言。
  document.documentElement.lang = locale
  // 清空字典缓存，确保切换语言后重新加载对应语言的字典数据。
  clearDictCache()
}

/**
 * 获取当前语言。
 */
export function getLocale(): LocaleCode {
  return i18n.global.locale.value as LocaleCode
}

/**
 * 获取所有可用语言选项。
 */
export function getAvailableLocales() {
  return [
    { value: 'zh-CN', label: '简体中文' },
    { value: 'en-US', label: 'English' },
    { value: 'zh-TW', label: '繁體中文' },
    { value: 'ja-JP', label: '日本語' },
    { value: 'ko-KR', label: '한국어' },
  ]
}

export default i18n
