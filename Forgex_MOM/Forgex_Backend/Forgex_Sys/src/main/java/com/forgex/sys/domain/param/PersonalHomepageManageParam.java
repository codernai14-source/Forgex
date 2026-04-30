package com.forgex.sys.domain.param;

import com.forgex.common.domain.config.PersonalHomepageConfig;
import lombok.Data;

/**
 * 首页配置管理参数。
 */
@Data
public class PersonalHomepageManageParam {

    /**
     * 模块编码。
     * <p>空值或 personal 表示个人首页，其它值表示模块首页。</p>
     */
    private String moduleCode;

    /**
     * 配置层级：PUBLIC / TENANT。
     */
    private String scopeLevel;

    /**
     * 首页配置内容。
     */
    private PersonalHomepageConfig config;
}
