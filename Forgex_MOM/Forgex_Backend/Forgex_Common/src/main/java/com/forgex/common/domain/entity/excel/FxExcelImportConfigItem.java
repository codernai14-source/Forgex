package com.forgex.common.domain.entity.excel;

import com.baomidou.mybatisplus.annotation.TableField;
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
     * Sheet 编码。
     * <p>
     * 多 Sheet 导入时用于把字段分组；单 Sheet 导入默认使用 {@code main}。
     * </p>
     */
    private String sheetCode;

    /**
     * Sheet 名称。
     * <p>
     * 用于模板生成和前端解析时匹配工作表名称。
     * </p>
     */
    private String sheetName;

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
     * 数据来源类型
     * <p>
     * 可选值：NONE(无下拉), DICT(字典下拉), JSON(静态下拉), PROVIDER(接口下拉)
     * </p>
     *
     * @since 1.1.0
     */
    private String dataSourceType;

    /**
     * 数据来源值
     * <p>
     * - DICT 类型：字典父表 id（如 '189'）
     * - JSON 类型：JSON 数组字符串（如 '["0|停用","1|启用"]'）
     * - PROVIDER 类型：Provider 编码（如 'FACTORY_LIST'）
     * </p>
     *
     * @since 1.1.0
     */
    private String dataSourceValue;

    /**
     * 依赖的父字段 key（级联下拉时使用）
     * <p>
     * 例如：部门字段依赖工厂字段，则填写工厂字段的 importField
     * </p>
     *
     * @since 1.1.0
     */
    private String dependsOnFieldKey;

    /**
     * 多选分隔符
     * <p>
     * 当字段支持多选时，使用此分隔符连接多个值
     * </p>
     *
     * @since 1.1.0
     */
    @TableField("`separator`")
    private String separator;

    /**
     * 字段备注/说明
     * <p>
     * 用于 Excel 表头显示 additional 信息
     * </p>
     *
     * @since 1.1.0
     */
    private String fieldRemark;

    /**
     * 是否必填。
     */
    private Boolean required;

    /**
     * 顺序（越小越靠前）。
     */
    private Integer orderNum;
}

