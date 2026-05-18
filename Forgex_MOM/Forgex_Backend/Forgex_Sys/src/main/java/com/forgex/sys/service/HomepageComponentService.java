package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.entity.SysHomepageComponentConfig;
import com.forgex.sys.domain.param.HomepageComponentPreferenceParam;
import com.forgex.sys.domain.param.HomepageComponentPullParam;
import com.forgex.sys.domain.param.HomepageComponentQueryParam;
import com.forgex.sys.domain.param.HomepageComponentSaveParam;
import com.forgex.sys.domain.vo.HomepageComponentVO;

import java.util.List;

/**
 * 首页组件目录服务。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
public interface HomepageComponentService {

    /**
     * 分页查询公共/租户组件配置。
     */
    IPage<HomepageComponentVO> pageComponents(Page<SysHomepageComponentConfig> page, HomepageComponentQueryParam param, Long tenantId);

    /**
     * 查询当前用户生效组件。
     */
    List<HomepageComponentVO> listEffectiveComponents(Long userId, Long tenantId, HomepageComponentQueryParam param);

    /**
     * 查询当前用户个人首页组件配置。
     */
    List<HomepageComponentVO> listPersonalComponents(Long userId, Long tenantId, HomepageComponentQueryParam param);

    /**
     * 保存组件配置。
     */
    Long saveComponent(HomepageComponentSaveParam param, Long tenantId);

    /**
     * 删除组件配置。
     */
    boolean deleteComponent(Long id, Long tenantId);

    /**
     * 按范围删除组件配置。
     */
    boolean deleteComponent(Long id, Long tenantId, String scopeLevel);

    /**
     * 租户拉取公共组件配置。
     */
    int pullPublicComponents(Long tenantId, HomepageComponentPullParam param);

    /**
     * 个人拉取租户生效组件配置。
     */
    int pullTenantComponents(Long userId, Long tenantId, HomepageComponentPullParam param);

    /**
     * 设置个人收藏。
     */
    boolean favoriteComponent(Long userId, Long tenantId, HomepageComponentPreferenceParam param);

    /**
     * 添加个人组件。
     */
    boolean addComponent(Long userId, Long tenantId, HomepageComponentPreferenceParam param);

    /**
     * 移除个人组件。
     */
    boolean removeComponent(Long userId, Long tenantId, HomepageComponentPreferenceParam param);
}
