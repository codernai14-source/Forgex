package com.forgex.sys.domain.param;

import com.forgex.common.domain.config.PersonalHomepageConfig;
import lombok.Data;

/**
 * 个人首页保存参数。
 */
@Data
public class PersonalHomepageSaveParam {

    /** 个人首页配置 */
    private PersonalHomepageConfig config;
}
