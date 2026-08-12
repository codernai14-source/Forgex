package com.forgex.common.domain.entity.excel;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * Excel 导出配置子表实体。
 * <p>
 * 数据源：{@code forgex_common}。\n
 * 用于描述导出列（字段、列头多语言、样式、顺序等）。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelExportConfig
 */
@Data
@TableName("fx_excel_export_config_item")
public class FxExcelExportConfigItem extends BaseEntity {

    /**
     * 导出配置主表ID。
     */
    private Long configId;

    /**
     * 导出字段（与数据源字段名对应）。
     */
    private String exportField;

    /**
     * 字段名（默认列头）。
     */
    private String fieldName;

    /**
     * 字段多语言配置（JSON）。
     */
    private String i18nJson;

    /**
     * 列头样式（JSON）。
     */
    private String headerStyleJson;

    /**
     * 列内容样式（JSON）。
     */
    private String cellStyleJson;

    /**
     * 顺序（越小越靠前）。
     */
    private Integer orderNum;
}

