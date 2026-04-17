/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.controller;

import com.forgex.common.domain.config.GuidePreferenceConfig;
import com.forgex.common.config.UserStyleConfigService;
import com.forgex.common.domain.config.LayoutStyleConfig;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.UserGuidePreferenceParam;
import com.forgex.sys.domain.param.UserLayoutStyleParam;
import com.forgex.sys.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户页面样式配置控制器。
 *
 * <p>提供用户在不同租户下的布局样式配置读写接口。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/user-style")
public class UserStyleController {

    @Autowired
    private UserStyleConfigService userStyleConfigService;

    @Autowired
    private ISysUserService userService;

    /**
     * 读取用户在指定租户下的布局样式配置。
     *
     * @param param 请求参数，需包含 account
     * @return 布局样式配置，若未配置则返回默认值
     */
    @PostMapping("/get-layout")
    public R<LayoutStyleConfig> getLayout(@RequestBody UserLayoutStyleParam param) {
        if (param == null || param.getAccount() == null) {
            return R.fail(CommonPrompt.ACCOUNT_EMPTY);
        }
        Long userId = userService.getUserIdByAccount(param.getAccount());
        if (userId == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        Long tenantId = TenantContext.get();
        LayoutStyleConfig config = userStyleConfigService.getLayoutConfig(userId, tenantId);
        return R.ok(config);
    }

    /**
     * 保存用户在指定租户下的布局样式配置。
     *
     * @param param 请求参数，需包含 account、config
     * @return 是否保存成功
     */
    @PostMapping("/save-layout")
    public R<Boolean> saveLayout(@RequestBody UserLayoutStyleParam param) {
        if (param == null || param.getAccount() == null || param.getConfig() == null) {
            return R.fail(CommonPrompt.ACCOUNT_CONFIG_CANNOT_BE_EMPTY);
        }
        Long userId = userService.getUserIdByAccount(param.getAccount());
        if (userId == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        Long tenantId = TenantContext.get();
        userStyleConfigService.saveLayoutConfig(userId, tenantId, param.getConfig());
        return R.ok(Boolean.TRUE);
    }

    /**
     * 读取当前用户在指定租户下的引导偏好配置。
     *
     * @param param 请求参数，需包含 account
     * @return 引导偏好配置
     */
    @PostMapping("/get-guide")
    public R<GuidePreferenceConfig> getGuide(@RequestBody UserGuidePreferenceParam param) {
        if (param == null || param.getAccount() == null) {
            return R.fail(CommonPrompt.ACCOUNT_EMPTY);
        }
        Long userId = userService.getUserIdByAccount(param.getAccount());
        if (userId == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        Long tenantId = TenantContext.get();
        GuidePreferenceConfig config = userStyleConfigService.getGuidePreference(userId, tenantId);
        return R.ok(config);
    }

    /**
     * 保存当前用户在指定租户下的引导偏好配置。
     *
     * @param param 请求参数，需包含 account 和 config
     * @return 是否保存成功
     */
    @PostMapping("/save-guide")
    public R<Boolean> saveGuide(@RequestBody UserGuidePreferenceParam param) {
        if (param == null || param.getAccount() == null || param.getConfig() == null) {
            return R.fail(CommonPrompt.ACCOUNT_CONFIG_CANNOT_BE_EMPTY);
        }
        Long userId = userService.getUserIdByAccount(param.getAccount());
        if (userId == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        Long tenantId = TenantContext.get();
        userStyleConfigService.saveGuidePreference(userId, tenantId, param.getConfig());
        return R.ok(Boolean.TRUE);
    }
}
