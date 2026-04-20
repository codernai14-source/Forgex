/**
 * 语言配置 API
 * 用于系统语言类型的查询、维护、导入与模板下载。
 */
import http from '../http'

/**
 * 语言类型
 */
export interface LanguageType {
  /** ID */
  id: number
  /** 语言代码，例如 zh-CN、en-US */
  langCode: string
  /** 语言名称，例如 简体中文、English */
  langName: string
  /** 英文名称 */
  langNameEn: string
  /** 图标 */
  icon: string
  /** 排序号 */
  orderNum: number
  /** 是否启用 */
  enabled: boolean
  /** 是否默认语言 */
  isDefault: boolean
  /** 创建人 */
  createBy: string
  /** 创建时间 */
  createTime: string
  /** 更新人 */
  updateBy: string | null
  /** 更新时间 */
  updateTime: string
  /** 删除标记 */
  deleted: number
}

/** 获取启用的语言列表。 */
export function listEnabledLanguages() {
  return http.post<LanguageType[]>('/sys/i18n/languageType/listEnabled')
}

/** 获取全部语言列表。 */
export function listAllLanguages() {
  return http.post<LanguageType[]>('/sys/i18n/languageType/listAll')
}

/** 根据语言代码查询语言。 */
export function getLanguageByCode(langCode: string) {
  return http.post<LanguageType>('/sys/i18n/languageType/getByLangCode', { langCode })
}

/** 获取默认语言。 */
export function getDefaultLanguage() {
  return http.post<LanguageType>('/sys/i18n/languageType/getDefault')
}

/** 分页查询语言。 */
export function pageLanguages(param: {
  pageNum?: number
  pageSize?: number
  langCode?: string
  langName?: string
  enabled?: boolean
}) {
  return http.post<any>('/sys/i18n/languageType/page', param)
}

/** 根据 ID 查询语言详情。 */
export function getLanguageById(id: number) {
  return http.post<LanguageType>('/sys/i18n/languageType/detail', { id })
}

/** 新增语言。 */
export function createLanguage(data: Partial<LanguageType>) {
  return http.post<boolean>('/sys/i18n/languageType/create', data)
}

/** 更新语言。 */
export function updateLanguage(data: Partial<LanguageType>) {
  return http.post<boolean>('/sys/i18n/languageType/update', data)
}

/** 删除语言。 */
export function deleteLanguage(id: number) {
  return http.post<boolean>('/sys/i18n/languageType/delete', { id })
}

/** 设置默认语言。 */
export function setDefaultLanguage(id: number) {
  return http.post<boolean>('/sys/i18n/languageType/setDefault', { id })
}

/** 导入语言 Excel。 */
export function importLanguages(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<any>('/sys/i18n/languageType/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/** 下载导入模板。 */
export function downloadImportTemplate() {
  return http.post('/sys/i18n/languageType/template/download', {}, {
    responseType: 'blob',
  })
}
