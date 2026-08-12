package com.forgex.common.domain.entity.i18n;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 多语言类型配置实体
 * <p>
 * 映射表：{@code fx_i18n_language_type}。用于配置系统支持的语言类型。
 * 字段：
 * - {@code langCode} 语言代码；
 * - {@code langName} 语言名称；
 * - {@code langNameEn} 语言英文名称；
 * - {@code icon} 语言图标；
 * - {@code orderNum} 排序号；
 * - {@code enabled} 是否启用；
 * - {@code isDefault} 是否默认语言。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.mapper.i18n.FxI18nLanguageTypeMapper
 */
@Data
@TableName("fx_i18n_language_type")
public class FxI18nLanguageType extends BaseEntity {
    /** 语言代码，例如：zh-CN, en-US */
    private String langCode;
    /** 语言名称，例如：简体中文, English */
    private String langName;
    /** 语言英文名称 */
    private String langNameEn;
    /** 语言图标 */
    private String icon;
    /** 排序号 */
    private Integer orderNum;
    /** 是否启用：1启用 0禁用 */
    private Boolean enabled;
    /** 是否默认语言：1默认 0非默认 */
    private Boolean isDefault;
}
