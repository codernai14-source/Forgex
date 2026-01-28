/**
 * 多语言工具函数
 * 
 * 提供多语言相关的工具方法，用于处理 I18n JSON 数据
 * 
 * @author Forgex Team
 * @version 1.0.0
 */

import { getLocale } from '@/locales'

/**
 * 从 I18n JSON 字符串中获取当前语言的值
 * 
 * @param i18nJson - 多语言 JSON 字符串，格式：{"zh-CN":"中文","en-US":"English"}
 * @param fallback - 回退值，当 JSON 为空或解析失败时返回
 * @param langCode - 指定语言代码，不传则使用当前语言
 * @returns 对应语言的值
 * 
 * @example
 * ```typescript
 * const menuName = getI18nValue(menu.nameI18nJson, menu.name)
 * // 如果当前语言是 zh-CN，返回中文名称
 * // 如果当前语言是 en-US，返回英文名称
 * // 如果 JSON 为空或当前语言没有配置，返回 fallback
 * ```
 */
export function getI18nValue(
  i18nJson: string | null | undefined,
  fallback: string = '',
  langCode?: string
): string {
  // 如果 JSON 为空，直接返回回退值
  if (!i18nJson || i18nJson.trim() === '') {
    return fallback
  }

  try {
    // 解析 JSON
    const i18nObj = JSON.parse(i18nJson)
    
    // 获取当前语言代码
    const currentLang = langCode || getLocale()
    
    // 优先返回当前语言的值
    if (i18nObj[currentLang]) {
      return i18nObj[currentLang]
    }
    
    // 如果当前语言没有配置，尝试返回默认语言（zh-CN）
    if (i18nObj['zh-CN']) {
      return i18nObj['zh-CN']
    }
    
    // 如果默认语言也没有，返回第一个可用的值
    const firstValue = Object.values(i18nObj)[0]
    if (firstValue && typeof firstValue === 'string') {
      return firstValue
    }
    
    // 都没有则返回回退值
    return fallback
  } catch (error) {
    console.error('解析 I18n JSON 失败:', error, 'JSON:', i18nJson)
    return fallback
  }
}

/**
 * 将对象转换为 I18n JSON 字符串
 * 
 * @param i18nObj - 多语言对象
 * @returns JSON 字符串
 * 
 * @example
 * ```typescript
 * const json = toI18nJson({
 *   'zh-CN': '用户管理',
 *   'en-US': 'User Management'
 * })
 * // 返回: '{"zh-CN":"用户管理","en-US":"User Management"}'
 * ```
 */
export function toI18nJson(i18nObj: Record<string, string>): string {
  if (!i18nObj || Object.keys(i18nObj).length === 0) {
    return ''
  }
  
  // 过滤掉空值
  const filtered: Record<string, string> = {}
  Object.entries(i18nObj).forEach(([key, value]) => {
    if (value && value.trim()) {
      filtered[key] = value.trim()
    }
  })
  
  return Object.keys(filtered).length > 0 ? JSON.stringify(filtered) : ''
}

/**
 * 解析 I18n JSON 字符串为对象
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @returns 多语言对象
 * 
 * @example
 * ```typescript
 * const obj = parseI18nJson('{"zh-CN":"用户管理","en-US":"User Management"}')
 * // 返回: { 'zh-CN': '用户管理', 'en-US': 'User Management' }
 * ```
 */
export function parseI18nJson(i18nJson: string | null | undefined): Record<string, string> {
  if (!i18nJson || i18nJson.trim() === '') {
    return {}
  }
  
  try {
    const obj = JSON.parse(i18nJson)
    return typeof obj === 'object' && obj !== null ? obj : {}
  } catch (error) {
    console.error('解析 I18n JSON 失败:', error)
    return {}
  }
}

/**
 * 检查 I18n JSON 是否为空
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @returns 是否为空
 */
export function isI18nJsonEmpty(i18nJson: string | null | undefined): boolean {
  if (!i18nJson || i18nJson.trim() === '') {
    return true
  }
  
  try {
    const obj = JSON.parse(i18nJson)
    return Object.keys(obj).length === 0
  } catch (error) {
    return true
  }
}

/**
 * 合并多个 I18n JSON
 * 
 * @param jsonList - JSON 字符串数组
 * @returns 合并后的 JSON 字符串
 * 
 * @example
 * ```typescript
 * const merged = mergeI18nJson([
 *   '{"zh-CN":"用户"}',
 *   '{"en-US":"User"}',
 *   '{"zh-CN":"用户管理"}' // 后面的会覆盖前面的
 * ])
 * // 返回: '{"zh-CN":"用户管理","en-US":"User"}'
 * ```
 */
export function mergeI18nJson(...jsonList: (string | null | undefined)[]): string {
  const merged: Record<string, string> = {}
  
  jsonList.forEach(json => {
    if (json) {
      const obj = parseI18nJson(json)
      Object.assign(merged, obj)
    }
  })
  
  return toI18nJson(merged)
}

/**
 * 获取 I18n JSON 中所有语言的列表
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @returns 语言代码数组
 * 
 * @example
 * ```typescript
 * const langs = getI18nLanguages('{"zh-CN":"用户","en-US":"User"}')
 * // 返回: ['zh-CN', 'en-US']
 * ```
 */
export function getI18nLanguages(i18nJson: string | null | undefined): string[] {
  const obj = parseI18nJson(i18nJson)
  return Object.keys(obj)
}

/**
 * 检查 I18n JSON 是否包含指定语言
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @param langCode - 语言代码
 * @returns 是否包含
 */
export function hasI18nLanguage(
  i18nJson: string | null | undefined,
  langCode: string
): boolean {
  const obj = parseI18nJson(i18nJson)
  return langCode in obj && !!obj[langCode]
}

/**
 * 设置 I18n JSON 中指定语言的值
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @param langCode - 语言代码
 * @param value - 值
 * @returns 新的 JSON 字符串
 * 
 * @example
 * ```typescript
 * const newJson = setI18nValue('{"zh-CN":"用户"}', 'en-US', 'User')
 * // 返回: '{"zh-CN":"用户","en-US":"User"}'
 * ```
 */
export function setI18nValue(
  i18nJson: string | null | undefined,
  langCode: string,
  value: string
): string {
  const obj = parseI18nJson(i18nJson)
  obj[langCode] = value
  return toI18nJson(obj)
}

/**
 * 删除 I18n JSON 中指定语言的值
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @param langCode - 语言代码
 * @returns 新的 JSON 字符串
 */
export function removeI18nValue(
  i18nJson: string | null | undefined,
  langCode: string
): string {
  const obj = parseI18nJson(i18nJson)
  delete obj[langCode]
  return toI18nJson(obj)
}

/**
 * 验证 I18n JSON 格式是否正确
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @returns 验证结果
 */
export function validateI18nJson(i18nJson: string | null | undefined): {
  valid: boolean
  error?: string
} {
  if (!i18nJson || i18nJson.trim() === '') {
    return { valid: true } // 空值也是有效的
  }
  
  try {
    const obj = JSON.parse(i18nJson)
    
    if (typeof obj !== 'object' || obj === null) {
      return { valid: false, error: 'JSON 必须是对象类型' }
    }
    
    // 检查所有值是否都是字符串
    for (const [key, value] of Object.entries(obj)) {
      if (typeof value !== 'string') {
        return { valid: false, error: `语言 ${key} 的值必须是字符串` }
      }
    }
    
    return { valid: true }
  } catch (error) {
    return { valid: false, error: 'JSON 格式错误' }
  }
}

/**
 * 格式化 I18n JSON（美化输出）
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @param indent - 缩进空格数
 * @returns 格式化后的 JSON 字符串
 */
export function formatI18nJson(i18nJson: string | null | undefined, indent: number = 2): string {
  const obj = parseI18nJson(i18nJson)
  return Object.keys(obj).length > 0 ? JSON.stringify(obj, null, indent) : ''
}

/**
 * 获取 I18n JSON 的完整性百分比
 * 
 * @param i18nJson - 多语言 JSON 字符串
 * @param requiredLangs - 必需的语言列表
 * @returns 完整性百分比（0-100）
 * 
 * @example
 * ```typescript
 * const completeness = getI18nCompleteness(
 *   '{"zh-CN":"用户","en-US":"User"}',
 *   ['zh-CN', 'en-US', 'ja-JP']
 * )
 * // 返回: 66.67 (3个语言中配置了2个)
 * ```
 */
export function getI18nCompleteness(
  i18nJson: string | null | undefined,
  requiredLangs: string[]
): number {
  if (requiredLangs.length === 0) {
    return 100
  }
  
  const obj = parseI18nJson(i18nJson)
  const configuredCount = requiredLangs.filter(lang => lang in obj && !!obj[lang]).length
  
  return Math.round((configuredCount / requiredLangs.length) * 100)
}

/**
 * 批量获取多语言值
 * 
 * @param items - 包含 I18n JSON 的对象数组
 * @param jsonField - JSON 字段名
 * @param fallbackField - 回退字段名
 * @returns 处理后的数组
 * 
 * @example
 * ```typescript
 * const menus = [
 *   { id: 1, name: '用户管理', nameI18nJson: '{"zh-CN":"用户管理","en-US":"User"}' },
 *   { id: 2, name: '角色管理', nameI18nJson: '{"zh-CN":"角色管理","en-US":"Role"}' }
 * ]
 * 
 * const processed = batchGetI18nValue(menus, 'nameI18nJson', 'name')
 * // 每个对象会添加 displayName 字段，值为当前语言的翻译
 * ```
 */
export function batchGetI18nValue<T extends Record<string, any>>(
  items: T[],
  jsonField: keyof T,
  fallbackField: keyof T,
  outputField: string = 'displayName'
): (T & { [key: string]: string })[] {
  return items.map(item => ({
    ...item,
    [outputField]: getI18nValue(item[jsonField] as string, item[fallbackField] as string)
  }))
}

