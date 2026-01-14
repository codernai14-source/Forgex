package com.forgex.common.domain.entity.excel;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * Excel 导出配置主表实体。
 * <p>
 * 数据源：{@code forgex_common}。\n
 * 主表保存导出配置的基础信息（表编号、标题、格式、总计开关、样式等）。\n
 * 字段明细由子表 {@link FxExcelExportConfigItem} 维护。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelExportConfigItem
 */
@Data
@TableName("fx_excel_export_config")
public class FxExcelExportConfig extends BaseEntity {

    /**
     * 表名（业务含义）。
     */
    private String tableName;

    /**
     * 表编号（唯一标识，用于业务端引用）。
     */
    private String tableCode;

    /**
     * 表头样式（JSON）。
     */
    private String headerStyleJson;

    /**
     * 标题列名称。
     */
    private String title;

    /**
     * 标题列小字说明。
     */
    private String subtitle;

    /**
     * 导出格式：xlsx/csv。
     */
    private String exportFormat;

    /**
     * 是否开启总计。
     */
    private Boolean enableTotal;

    /**
     * 版本号（用于配置演进）。
     */
    private Integer version;
}

