package com.forgex.sys.controller;

import com.forgex.common.config.PersonalHomepageConfigService;
import com.forgex.common.domain.config.PersonalHomepageConfig;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.PersonalHomepageManageParam;
import com.forgex.sys.domain.param.PersonalHomepageSaveParam;
import com.forgex.sys.domain.vo.PersonalHomepageSummaryVO;
import com.forgex.sys.service.PersonalHomepageSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人首页配置控制器。
 * <p>
 * 提供个人首页配置的查询、保存、重置功能，
 * 以及首页摘要信息（头像、昵称、在线时长、问候语）的查询。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 */
@RestController
@RequestMapping("/sys/homepage")
@RequiredArgsConstructor
public class PersonalHomepageController {

    private final PersonalHomepageConfigService personalHomepageConfigService;

    private final PersonalHomepageSummaryService personalHomepageSummaryService;

    @PostMapping("/current/get")
    public R<PersonalHomepageConfig> getCurrentConfig() {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        return R.ok(personalHomepageConfigService.getEffectiveConfig(userId, tenantId));
    }

    @PostMapping("/current/save")
    public R<Boolean> saveCurrentConfig(@RequestBody PersonalHomepageSaveParam param) {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        if (param == null || param.getConfig() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        personalHomepageConfigService.saveUserConfig(userId, tenantId, param.getConfig());
        return R.ok(CommonPrompt.SAVE_SUCCESS, Boolean.TRUE);
    }

    @PostMapping("/current/reset")
    public R<Boolean> resetCurrentConfig() {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        personalHomepageConfigService.resetUserConfig(userId, tenantId);
        return R.ok(CommonPrompt.RESTORE_SUCCESS, Boolean.TRUE);
    }

    @PostMapping("/manage/get")
    public R<PersonalHomepageConfig> getManageConfig(@RequestBody PersonalHomepageManageParam param) {
        Long tenantId = TenantContext.get();
        String scopeLevel = normalizeScope(param == null ? null : param.getScopeLevel());
        if ("PUBLIC".equals(scopeLevel)) {
            return R.ok(personalHomepageConfigService.getPublicConfig());
        }
        return R.ok(personalHomepageConfigService.getTenantConfig(tenantId));
    }

    @PostMapping("/manage/save")
    public R<Boolean> saveManageConfig(@RequestBody PersonalHomepageManageParam param) {
        if (param == null || param.getConfig() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        String scopeLevel = normalizeScope(param.getScopeLevel());
        if ("PUBLIC".equals(scopeLevel)) {
            personalHomepageConfigService.savePublicConfig(param.getConfig());
        } else {
            personalHomepageConfigService.saveTenantConfig(TenantContext.get(), param.getConfig());
        }
        return R.ok(CommonPrompt.SAVE_SUCCESS, Boolean.TRUE);
    }

    private String normalizeScope(String scopeLevel) {
        if (!StringUtils.hasText(scopeLevel)) {
            return "TENANT";
        }
        String value = scopeLevel.trim().toUpperCase();
        return "PUBLIC".equals(value) ? "PUBLIC" : "TENANT";
    }

    /**
     * 获取个人首页摘要信息。
     * <p>
     * 返回当前用户的头像、昵称、在线时长和问候语，
     * 用于首页顶部用户卡片展示。
     * </p>
     *
     * @return 摘要信息VO
     */
    @GetMapping("/summary")
    public R<PersonalHomepageSummaryVO> getSummary() {
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        PersonalHomepageSummaryVO summary = personalHomepageSummaryService.getSummary(userId, tenantId);
        return R.ok(summary);
    }
}
