package com.forgex.common.service.i18n;

import com.forgex.common.i18n.I18nPrompt;

/**
 * 国际化消息解析服务
 * <p>
 * 用于将 {@link I18nPrompt} 或 (module + promptCode) 转换为当前语言下的最终文案。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.i18n.LangContext
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
public interface I18nMessageService {
    /**
     * 解析国际化提示为最终文案（支持参数占位符）
     *
     * @param prompt 国际化提示（通常为实现 {@link I18nPrompt} 的枚举）
     * @param args 参数（用于 {@link java.text.MessageFormat} 注入）
     * @return 最终文案；当 prompt 为空或模板缺失时返回 null
     */
    String resolve(I18nPrompt prompt, Object[] args);

    /**
     * 通过 module + promptCode 解析国际化提示为最终文案（支持参数占位符）
     * <p>
     * 用于跨服务/跨进程场景：当响应体无法携带枚举对象本身（例如 JSON 反序列化后）时，
     * 仍可通过 code 反查国际化文案。
     * </p>
     *
     * @param module 模块标识
     * @param promptCode 提示码
     * @param args 参数（用于 {@link java.text.MessageFormat} 注入）
     * @return 最终文案；当 module/promptCode 为空或查不到模板时返回 null
     */
    String resolve(String module, String promptCode, Object[] args);
}
