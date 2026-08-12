package com.forgex.common.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 个人主页模块提示枚举
 * <p>
 * 约定：{@link #promptCode} 需与表 {@code fx_i18n_message.prompt_code} 保持一致，
 * 由 {@code module + promptCode} 唯一定位一条国际化文案记录。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nMessageService
 * @see com.forgex.common.web.RMessageI18nAdvice
 */
@Getter
public enum ProfilePromptEnum implements I18nPrompt {
    // ========== 个人信息 ==========
    PROFILE_UPDATE_SUCCESS("PROFILE_UPDATE_SUCCESS", "个人信息更新成功"),
    PROFILE_UPDATE_FAILED("PROFILE_UPDATE_FAILED", "个人信息更新失败"),
    
    // ========== 头像管理 ==========
    AVATAR_UPLOAD_SUCCESS("AVATAR_UPLOAD_SUCCESS", "头像上传成功"),
    AVATAR_UPLOAD_FAILED("AVATAR_UPLOAD_FAILED", "头像上传失败");

    private final String promptCode;
    private final String defaultTemplate;

    ProfilePromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "profile";
    }
}
