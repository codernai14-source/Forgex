package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 首页组件个人偏好快照实体。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
@TableName("sys_homepage_component_preference")
public class SysHomepageComponentPreference extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 来源组件配置 ID。
     */
    private Long sourceComponentId;

    /**
     * 组件编码快照。
     */
    private String componentCode;

    /**
     * 组件名称快照。
     */
    private String componentName;

    /**
     * 前端注册组件标识快照。
     */
    private String componentPath;

    /**
     * 图标名称快照。
     */
    private String icon;

    /**
     * 作用说明快照。
     */
    private String useDesc;

    /**
     * 是否收藏。
     */
    private Boolean favorite;

    /**
     * 是否移除。
     */
    private Boolean removed;

    /**
     * 排序号。
     */
    private Integer orderNum;

    /**
     * 个人参数 JSON。
     */
    private String paramsJson;

    /**
     * 备注。
     */
    private String remark;
}
