package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 首页组件公共/租户配置实体。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
@TableName("sys_homepage_component_config")
public class SysHomepageComponentConfig extends BaseEntity {

    /**
     * 分类 ID。
     */
    private Long categoryId;

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
