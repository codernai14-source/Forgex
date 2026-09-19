package com.forgex.auth.domain.param;

import lombok.Data;

/**
 * 切换语言请求参数
 * <p>
 * 用于封装用户切换系统语言时提交的参数信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.controller.AuthController
 * @see com.forgex.common.i18n.LangContext
 */
@Data
public class ChangeLanguageParam {
    /**
     * 语言代码
     * <p>
     * 可选值：
     * <ul>
     *     <li>zh-CN：简体中文</li>
     *     <li>zh-TW：繁体中文</li>
     *     <li>en-US：英文</li>
     * </ul>
     * </p>
     */
    private String lang;
}
