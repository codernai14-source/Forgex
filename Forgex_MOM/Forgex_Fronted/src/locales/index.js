/**
 * 多语言配置
 *
 * 统一管理系统的多语言配置
 */
import { createI18n } from 'vue-i18n';
import { clearDictCache } from '@/hooks/useDict';
import zhCN from './zh-CN';
import enUS from './en-US';
import zhTW from './zh-TW';
import jaJP from './ja-JP';
import koKR from './ko-KR';
// 语言包
const messages = {
    'zh-CN': zhCN,
    'en-US': enUS,
    'zh-TW': zhTW,
    'ja-JP': jaJP,
    'ko-KR': koKR,
};
// 获取存储的语言设置
const storedLocale = localStorage.getItem('fx-locale');
// 获取浏览器语言
const browserLocale = navigator.language || 'zh-CN';
// 确定初始语言
const initialLocale = storedLocale || (browserLocale.startsWith('en') ? 'en-US' : 'zh-CN');
// 创建 i18n 实例
const i18n = createI18n({
    legacy: false, // 使用 Composition API 模式
    locale: initialLocale, // 当前语言
    fallbackLocale: 'zh-CN', // 回退语言
    messages, // 语言包
    globalInjection: true, // 全局注入 $t 函数
});
/**
 * 切换语言
 * @param locale 语言代码
 */
export function setLocale(locale) {
    i18n.global.locale.value = locale;
    localStorage.setItem('fx-locale', locale);
    // 更新 HTML lang 属性
    document.documentElement.lang = locale;
    // 清除字典缓存，以便重新加载对应语言的字典数据
    clearDictCache();
}
/**
 * 获取当前语言
 */
export function getLocale() {
    return i18n.global.locale.value;
}
/**
 * 获取所有可用语言
 */
export function getAvailableLocales() {
    return [
        { value: 'zh-CN', label: '简体中文' },
        { value: 'en-US', label: 'English' },
        { value: 'zh-TW', label: '繁體中文' },
        { value: 'ja-JP', label: '日本語' },
        { value: 'ko-KR', label: '한국어' },
    ];
}
export default i18n;
