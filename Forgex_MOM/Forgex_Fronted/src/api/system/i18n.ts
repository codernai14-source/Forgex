/**
 * 多语言配置 API 接口文件
 * 负责与后端多语言配置接口进行交互
 * @author Forgex Team
 * @version 1.0.0
 */
import http from '../http'

/**
 * 多语言类型接口
 * 定义多语言类型的数据结构
 */
export interface LanguageType {
  /** 语言ID */
  id: number
  /** 语言代码，例如：zh-CN, en-US */
  langCode: string
  /** 语言名称，例如：简体中文, English */
  langName: string
  /** 语言英文名称 */
  langNameEn: string
  /** 语言图标 */
  icon: string
  /** 排序号 */
  orderNum: number
  /** 是否启用：1启用 0禁用 */
  enabled: boolean
  /** 是否默认语言：1默认 0非默认 */
  isDefault: boolean
  /** 创建人 */
  createBy: string
  /** 创建时间 */
  createTime: string
  /** 更新人 */
  updateBy: string | null
  /** 更新时间 */
  updateTime: string
  /** 是否删除：1删除 0未删除 */
  deleted: number
}

/**
 * 获取所有启用的语言类型列表
 * @returns 启用的语言类型列表
 */
export function listEnabledLanguages() {
  return http.post<LanguageType[]>('/sys/i18n/languageType/listEnabled')
}

/**
 * 获取所有语言类型列表
 * @returns 所有语言类型列表
 */
export function listAllLanguages() {
  return http.post<LanguageType[]>('/sys/i18n/languageType/listAll')
}

/**
 * 根据语言代码获取语言类型
 * @param langCode 语言代码
 * @returns 语言类型实体
 */
export function getLanguageByCode(langCode: string) {
  return http.post<LanguageType>('/sys/i18n/languageType/getByLangCode', { langCode })
}

/**
 * 获取默认语言类型
 * @returns 默认语言类型实体
 */
export function getDefaultLanguage() {
  return http.post<LanguageType>('/sys/i18n/languageType/getDefault')
}
