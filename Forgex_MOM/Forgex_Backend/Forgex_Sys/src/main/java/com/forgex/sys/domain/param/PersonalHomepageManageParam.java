package com.forgex.sys.domain.param;

import com.forgex.common.domain.config.PersonalHomepageConfig;
import lombok.Data;

/**
 * 个人首页管理参数。
 */
@Data
public class PersonalHomepageManageParam {

    /** 配置层级：PUBLIC / TENANT */
    private String scopeLevel;

    /** 配置内容 */
    private PersonalHomepageConfig config;
}
