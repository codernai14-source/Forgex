package com.forgex.common.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * 文件管理模块提示枚举
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
public enum FilePromptEnum implements I18nPrompt {
    // ========== 文件上传 ==========
    FILE_UPLOAD_SUCCESS("FILE_UPLOAD_SUCCESS", "文件上传成功"),
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "文件上传失败"),
    
    // ========== 文件下载 ==========
    FILE_DOWNLOAD_SUCCESS("FILE_DOWNLOAD_SUCCESS", "文件下载成功"),
    FILE_DOWNLOAD_FAILED("FILE_DOWNLOAD_FAILED", "文件下载失败"),
    
    // ========== 文件删除 ==========
    FILE_DELETE_SUCCESS("FILE_DELETE_SUCCESS", "文件删除成功"),
    FILE_DELETE_FAILED("FILE_DELETE_FAILED", "文件删除失败"),
    
    // ========== 文件校验 ==========
    FILE_NOT_FOUND("FILE_NOT_FOUND", "文件不存在"),
    FILE_TYPE_NOT_ALLOWED("FILE_TYPE_NOT_ALLOWED", "文件类型不允许"),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "文件大小超出限制");

    private final String promptCode;
    private final String defaultTemplate;

    FilePromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "file";
    }
}
