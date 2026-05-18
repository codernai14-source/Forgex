package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 首页组件保存参数。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
public class HomepageComponentSaveParam {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 分类编码。
     */
    private String categoryCode;

    /**
     * 分类名称。
     */
    private String categoryName;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 配置层级：PUBLIC/TENANT。
     */
    private String scopeLevel;

    /**
     * 组件编码。
     */
    private String componentCode;

    /**
     * 组件名称。
     */
    private String componentName;

    /**
     * 前端注册组件标识。
     */
    private String componentPath;

    /**
     * 图标名称。
     */
    private String icon;

    /**
     * 作用说明。
     */
    private String useDesc;

    /**
     * 默认参数 JSON。
     */
    private String defaultParamsJson;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 排序号。
     */
    private Integer orderNum;

    /**
     * 备注。
     */
    private String remark;
}
