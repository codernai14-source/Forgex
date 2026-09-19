package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 首页组件个人偏好参数。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Data
public class HomepageComponentPreferenceParam {

    /**
     * 组件编码。
     */
    private String componentCode;

    /**
     * 是否收藏。
     */
    private Boolean favorite;

    /**
     * 模块编码。
     */
    private String moduleCode;
}
