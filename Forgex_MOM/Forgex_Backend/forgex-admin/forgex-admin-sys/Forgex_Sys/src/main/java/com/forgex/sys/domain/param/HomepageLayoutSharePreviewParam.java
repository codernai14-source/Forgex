package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 首页布局分享码预览参数。
 */
@Data
public class HomepageLayoutSharePreviewParam {

    /**
     * 分享码。
     */
    private String shareCode;

    /**
     * 首页模块编码。
     */
    private String moduleCode;
}
