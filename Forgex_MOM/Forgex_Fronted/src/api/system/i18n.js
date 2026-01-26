/**
 * 多语言配置 API 接口文件
 * 负责与后端多语言配置接口进行交互
 * @author Forgex Team
 * @version 1.0.0
 */
import http from '../http';
/**
 * 获取所有启用的语言类型列表
 * @returns 启用的语言类型列表
 */
export function listEnabledLanguages() {
    return http.post('/sys/i18n/languageType/listEnabled');
}
/**
 * 获取所有语言类型列表
 * @returns 所有语言类型列表
 */
export function listAllLanguages() {
    return http.post('/sys/i18n/languageType/listAll');
}
/**
 * 根据语言代码获取语言类型
 * @param langCode 语言代码
 * @returns 语言类型实体
 */
export function getLanguageByCode(langCode) {
    return http.post('/sys/i18n/languageType/getByLangCode', { langCode });
}
/**
 * 获取默认语言类型
 * @returns 默认语言类型实体
 */
export function getDefaultLanguage() {
    return http.post('/sys/i18n/languageType/getDefault');
}
