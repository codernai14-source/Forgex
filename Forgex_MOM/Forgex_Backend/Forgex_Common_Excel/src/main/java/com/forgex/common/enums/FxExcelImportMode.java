package com.forgex.common.enums;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Excel 公共导入模式。
 * <p>
 * ADD：新增，已存在数据跳过；UPDATE：修改，仅更新已存在数据；
 * COVER：覆盖，由业务处理器按业务边界清空后重新导入。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
public enum FxExcelImportMode {
    /**
     * 新增。
     */
    ADD,

    /**
     * 修改。
     */
    UPDATE,

    /**
     * 覆盖。
     */
    COVER;

    /**
     * 解析导入模式。
     *
     * @param value 原始值
     * @return 导入模式
     */
    public static FxExcelImportMode parse(String value) {
        if (!StringUtils.hasText(value)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_MODE_INVALID);
        }
        try {
            return FxExcelImportMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_MODE_INVALID);
        }
    }
}
