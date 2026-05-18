package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 首页组件拉取配置参数。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
public class HomepageComponentPullParam {

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 分类 ID。
     */
    private Long categoryId;
}
