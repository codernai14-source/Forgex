package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysHomepageComponentConfig;
import com.forgex.sys.domain.param.HomepageComponentPreferenceParam;
import com.forgex.sys.domain.param.HomepageComponentPullParam;
import com.forgex.sys.domain.param.HomepageComponentQueryParam;
import com.forgex.sys.domain.param.HomepageComponentSaveParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.HomepageComponentVO;
import com.forgex.sys.service.HomepageComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页组件目录控制器。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@RestController
@RequestMapping("/homepage/component")
@RequiredArgsConstructor
public class HomepageComponentController {

    private final HomepageComponentService homepageComponentService;

    /**
     * 分页查询组件配置。
     */
    @PostMapping("/page")
    public R<IPage<HomepageComponentVO>> page(@RequestBody(required = false) HomepageComponentQueryParam param) {
        HomepageComponentQueryParam condition = param == null ? new HomepageComponentQueryParam() : param;
        Page<SysHomepageComponentConfig> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(homepageComponentService.pageComponents(page, condition, TenantContext.get()));
    }

    /**
     * 保存组件配置。
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody HomepageComponentSaveParam param) {
        Long id = homepageComponentService.saveComponent(param, TenantContext.get());
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        return R.ok(CommonPrompt.SAVE_SUCCESS, id);
    }

    /**
     * 删除组件配置。
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody IdParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        return R.ok(CommonPrompt.DELETE_SUCCESS, homepageComponentService.deleteComponent(param.getId(), TenantContext.get()));
    }

    /**
     * 查询当前用户生效组件。
     */
    @PostMapping("/effective/list")
    public R<List<HomepageComponentVO>> listEffective(@RequestBody(required = false) HomepageComponentQueryParam param) {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(homepageComponentService.listEffectiveComponents(userId, tenantId, param));
    }

    /**
     * 设置个人收藏。
     */
    @PostMapping("/favorite")
    public R<Boolean> favorite(@RequestBody HomepageComponentPreferenceParam param) {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(homepageComponentService.favoriteComponent(userId, tenantId, param));
    }

    /**
     * 添加个人首页组件。
     */
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody HomepageComponentPreferenceParam param) {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(homepageComponentService.addComponent(userId, tenantId, param));
    }

    /**
     * 移除个人首页组件。
     */
    @PostMapping("/remove")
    public R<Boolean> remove(@RequestBody HomepageComponentPreferenceParam param) {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(homepageComponentService.removeComponent(userId, tenantId, param));
    }

    /**
     * 租户拉取公共配置。
     */
    @PostMapping("/pull-public")
    public R<Integer> pullPublic(@RequestBody(required = false) HomepageComponentPullParam param) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(CommonPrompt.SYNC_SUCCESS, homepageComponentService.pullPublicComponents(tenantId, param));
    }

    /**
     * 个人拉取租户配置。
     */
    @PostMapping("/pull-tenant")
    public R<Integer> pullTenant(@RequestBody(required = false) HomepageComponentPullParam param) {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(CommonPrompt.SYNC_SUCCESS, homepageComponentService.pullTenantComponents(userId, tenantId, param));
    }
}
