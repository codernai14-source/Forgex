/**
 * 澶氳瑷€閰嶇疆 API 鎺ュ彛鏂囦欢
 * 璐熻矗涓庡悗绔璇█閰嶇疆鎺ュ彛杩涜浜や簰
 * @author Forgex Team
 * @version 1.0.0
 */
import http from '../http'

/**
 * 澶氳瑷€绫诲瀷鎺ュ彛
 * 瀹氫箟澶氳瑷€绫诲瀷鐨勬暟鎹粨鏋? */
export interface LanguageType {
  /** 璇█ID */
  id: number
  /** 璇█浠ｇ爜锛屼緥濡傦細zh-CN, en-US */
  langCode: string
  /** 璇█鍚嶇О锛屼緥濡傦細绠€浣撲腑鏂? English */
  langName: string
  /** 璇█鑻辨枃鍚嶇О */
  langNameEn: string
  /** 璇█鍥炬爣 */
  icon: string
  /** 鎺掑簭鍙?*/
  orderNum: number
  /** 鏄惁鍚敤锛?鍚敤 0绂佺敤 */
  enabled: boolean
  /** 鏄惁榛樿璇█锛?榛樿 0闈為粯璁?*/
  isDefault: boolean
  /** 鍒涘缓浜?*/
  createBy: string
  /** 鍒涘缓鏃堕棿 */
  createTime: string
  /** 鏇存柊浜?*/
  updateBy: string | null
  /** 鏇存柊鏃堕棿 */
  updateTime: string
  /** 鏄惁鍒犻櫎锛?鍒犻櫎 0鏈垹闄?*/
  deleted: number
}

/**
 * 鑾峰彇鎵€鏈夊惎鐢ㄧ殑璇█绫诲瀷鍒楄〃
 * @returns 鍚敤鐨勮瑷€绫诲瀷鍒楄〃
 */
export function listEnabledLanguages() {
  return http.post<LanguageType[]>('/sys/i18n/languageType/listEnabled')
}

/**
 * 鑾峰彇鎵€鏈夎瑷€绫诲瀷鍒楄〃
 * @returns 鎵€鏈夎瑷€绫诲瀷鍒楄〃
 */
export function listAllLanguages() {
  return http.post<LanguageType[]>('/sys/i18n/languageType/listAll')
}

/**
 * 鏍规嵁璇█浠ｇ爜鑾峰彇璇█绫诲瀷
 * @param langCode 璇█浠ｇ爜
 * @returns 璇█绫诲瀷瀹炰綋
 */
export function getLanguageByCode(langCode: string) {
  return http.post<LanguageType>('/sys/i18n/languageType/getByLangCode', { langCode })
}

/**
 * 鑾峰彇榛樿璇█绫诲瀷
 * @returns 榛樿璇█绫诲瀷瀹炰綋
 */
export function getDefaultLanguage() {
  return http.post<LanguageType>('/sys/i18n/languageType/getDefault')
}

/**
 * 鍒嗛〉鏌ヨ璇█绫诲瀷鍒楄〃
 * @param param 鏌ヨ鍙傛暟
 * @returns 鍒嗛〉缁撴灉
 */
export function pageLanguages(param: {
  pageNum?: number
  pageSize?: number
  langCode?: string
  langName?: string
  enabled?: boolean
}) {
  return http.post<any>('/sys/i18n/languageType/page', param)
}

/**
 * 鏍规嵁 ID 鑾峰彇璇█绫诲瀷璇︽儏
 * @param id 璇█绫诲瀷 ID
 * @returns 璇█绫诲瀷瀹炰綋
 */
export function getLanguageById(id: number) {
  return http.post<LanguageType>('/sys/i18n/languageType/detail', { id })
}

/**
 * 鍒涘缓璇█绫诲瀷
 * @param data 璇█绫诲瀷鏁版嵁
 * @returns 鏄惁鍒涘缓鎴愬姛
 */
export function createLanguage(data: Partial<LanguageType>) {
  return http.post<boolean>('/sys/i18n/languageType/create', data)
}

/**
 * 鏇存柊璇█绫诲瀷
 * @param data 璇█绫诲瀷鏁版嵁
 * @returns 鏄惁鏇存柊鎴愬姛
 */
export function updateLanguage(data: Partial<LanguageType>) {
  return http.post<boolean>('/sys/i18n/languageType/update', data)
}

/**
 * 鍒犻櫎璇█绫诲瀷
 * @param id 璇█绫诲瀷 ID
 * @returns 鏄惁鍒犻櫎鎴愬姛
 */
export function deleteLanguage(id: number) {
  return http.post<boolean>('/sys/i18n/languageType/delete', { id })
}

/**
 * 璁剧疆榛樿璇█
 * @param id 璇█绫诲瀷 ID
 * @returns 鏄惁璁剧疆鎴愬姛
 */
export function setDefaultLanguage(id: number) {
  return http.post<boolean>('/sys/i18n/languageType/setDefault', { id })
}

/**
 * 瀵煎叆璇█绫诲瀷 Excel 鏂囦欢
 * @param file Excel 鏂囦欢
 * @returns 瀵煎叆缁撴灉
 */
export function importLanguages(file: File) {
  const formData = new 表单Data()
  formData.append('file', file)
  return http.post<any>('/sys/i18n/languageType/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 涓嬭浇瀵煎叆妯℃澘
 * @returns 妯℃澘鏂囦欢 blob
 */
export function downloadImportTemplate() {
  return http.post('/sys/i18n/languageType/template/download', {}, {
    responseType: 'blob',
  })
}
