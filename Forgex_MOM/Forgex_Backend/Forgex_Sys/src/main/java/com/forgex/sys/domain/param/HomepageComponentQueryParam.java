package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页组件查询参数。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HomepageComponentQueryParam extends BaseGetParam {

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
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 配置层级。
     */
    private String scopeLevel;

    /**
     * 检索关键字。
     */
    private String keyword;

    /**
     * 是否启用。
     */
    private Boolean enabled;
}
