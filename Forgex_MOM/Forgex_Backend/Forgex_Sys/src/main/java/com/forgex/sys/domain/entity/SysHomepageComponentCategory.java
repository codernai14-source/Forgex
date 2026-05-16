package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 首页组件分类实体。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
@TableName("sys_homepage_component_category")
public class SysHomepageComponentCategory extends BaseEntity {

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
     * 备注。
     */
    private String remark;
}
