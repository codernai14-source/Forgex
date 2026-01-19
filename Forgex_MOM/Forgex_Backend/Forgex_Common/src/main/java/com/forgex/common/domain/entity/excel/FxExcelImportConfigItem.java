package com.forgex.common.domain.entity.excel;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * Excel 导入配置子表实体。
 * <p>
 * 数据源：{@code forgex_common}。\n
 * 用于描述导入列（字段、类型、字典、必填、顺序等）。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelImportConfig
 */
@Data
@TableName("fx_excel_import_config_item")
public class FxExcelImportConfigItem extends BaseEntity {

    /**
     * 导入配置主表ID。
     */
    private Long configId;

    /**
     * 字段多语言配置（JSON）。
     */
    private String i18nJson;

    /**
     * 导入字段（与数据源字段名对应）。
     */
    private String importField;

    /**
     * 字段类型：time/date/datetime/dict/string/number 等。
     */
    private String fieldType;

    /**
     * 字典编号（fieldType=dict 时使用）。
     */
    private String dictCode;

    /**
     * 是否必填。
     */
    private Boolean required;

    /**
     * 顺序（越小越靠前）。
     */
    private Integer orderNum;
}

