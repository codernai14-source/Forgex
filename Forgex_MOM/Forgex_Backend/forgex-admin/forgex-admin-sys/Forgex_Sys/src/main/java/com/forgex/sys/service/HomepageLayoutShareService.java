package com.forgex.sys.service;

import com.forgex.common.domain.config.PersonalHomepageConfig;
import com.forgex.sys.domain.vo.HomepageLayoutShareVO;

/**
 * 首页布局分享码服务。
 */
public interface HomepageLayoutShareService {

    /**
     * 创建租户内首页布局分享码。
     *
     * @param tenantId   租户 ID
     * @param moduleCode 首页模块编码
     * @param config     首页布局配置
     * @return 分享码信息
     */
    HomepageLayoutShareVO createShare(Long tenantId, String moduleCode, PersonalHomepageConfig config);

    /**
     * 按分享码预览租户内首页布局。
     *
     * @param tenantId   租户 ID
     * @param shareCode  分享码
     * @param moduleCode 首页模块编码
     * @return 分享码信息，不存在时返回 null
     */
    HomepageLayoutShareVO previewShare(Long tenantId, String shareCode, String moduleCode);
}
