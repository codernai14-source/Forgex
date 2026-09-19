package com.forgex.sys.domain.param;

import com.forgex.common.domain.config.PersonalHomepageConfig;
import lombok.Data;

/**
 * 首页配置保存参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class PersonalHomepageSaveParam {

    /**
     * 模块编码。
     * <p>空值或 personal 表示个人首页，其它值表示模块首页。</p>
     */
    private String moduleCode;

    /**
     * 首页配置。
     */
    private PersonalHomepageConfig config;
}
