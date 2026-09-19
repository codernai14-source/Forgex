package com.forgex.common.domain.entity.excel;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * Excel 导入配置主表实体。
 * <p>
 * 数据源：{@code forgex_common}。\n
 * 主表保存导入模板的基础信息（表编号、标题、说明等），\n
 * 字段明细由子表 {@link FxExcelImportConfigItem} 维护。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelImportConfigItem
 */
@Data
@TableName("fx_excel_import_config")
public class FxExcelImportConfig extends BaseEntity {

    /**
     * 表名（业务含义）。
     */
    private String tableName;

    /**
     * 表编号（唯一标识，用于业务端引用）。
     */
    private String tableCode;

    /**
     * 导入处理器 Bean 名称。
     * <p>
     * 公共导入执行接口根据该名称从 Spring 容器中查找业务处理器，
     * 并把导入模式、导入配置和导入数据统一传入业务处理器。
     * </p>
     */
    private String handlerBeanName;

    /**
     * 导入权限码。
     * <p>
     * 公共导入执行接口按表编码读取配置后，使用该权限码进行动态鉴权。
     * </p>
     */
    private String importPermission;

    /**
     * 标题列名称。
     */
    private String title;

    /**
     * 模板标题国际化 JSON
     * <p>
     * 存储多语言标题，格式：{"zh-CN": "用户导入模板", "en-US": "User Import Template"}
     * </p>
     *
     * @since 1.1.0
     */
    private String titleI18nJson;

    /**
     * 标题列小字说明。
     */
    private String subtitle;

    /**
     * 模板说明国际化 JSON
     * <p>
     * 存储多语言说明，格式：{"zh-CN": "请填写用户信息", "en-US": "Please fill in user information"}
     * </p>
     *
     * @since 1.1.0
     */
    private String subtitleI18nJson;

    /**
     * 模板说明样式 JSON
     * <p>
     * 存储样式配置，格式：{"backgroundColor": "#8EC67F", "wrapText": true, "fontSize": 12}
     * </p>
     *
     * @since 1.1.0
     */
    private String subtitleStyleJson;

    /**
     * 版本号（用于配置演进）。
     */
    private Integer version;
}

