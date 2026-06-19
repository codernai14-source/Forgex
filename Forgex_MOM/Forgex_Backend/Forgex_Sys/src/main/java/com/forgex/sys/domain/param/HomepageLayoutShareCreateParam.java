package com.forgex.sys.domain.param;

import com.forgex.common.domain.config.PersonalHomepageConfig;
import lombok.Data;

/**
 * 首页布局分享码创建参数。
 */
@Data
public class HomepageLayoutShareCreateParam {

    /**
     * 首页模块编码。
     */
    private String moduleCode;

    /**
     * 首页布局配置。
     */
    private PersonalHomepageConfig config;
}
