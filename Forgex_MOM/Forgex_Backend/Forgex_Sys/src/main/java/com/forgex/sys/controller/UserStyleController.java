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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.config.UserStyleConfigService;
import com.forgex.common.domain.config.LayoutStyleConfig;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.UserLayoutStyleParam;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.SysUserMapper;
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
    private SysUserMapper userMapper;

    /**
     * 读取用户在指定租户下的布局样式配置。
     *
     * @param param 请求参数，需包含 account、tenantId
     * @return 布局样式配置，若未配置则返回默认值
     */
    @PostMapping("/get-layout")
    public R<LayoutStyleConfig> getLayout(@RequestBody UserLayoutStyleParam param) {
        if (param == null || param.getAccount() == null || param.getTenantId() == null) {
            return R.fail(500, "account/tenantId 不能为空");
        }
        Long userId = resolveUserId(param.getAccount());
        if (userId == null) {
            return R.fail(404, "用户不存在");
        }
        LayoutStyleConfig config = userStyleConfigService.getLayoutConfig(userId, param.getTenantId());
        return R.ok(config);
    }

    /**
     * 保存用户在指定租户下的布局样式配置。
     *
     * @param param 请求参数，需包含 account、tenantId、config
     * @return 是否保存成功
     */
    @PostMapping("/save-layout")
    public R<Boolean> saveLayout(@RequestBody UserLayoutStyleParam param) {
        if (param == null || param.getAccount() == null || param.getTenantId() == null || param.getConfig() == null) {
            return R.fail(500, "account/tenantId/config 不能为空");
        }
        Long userId = resolveUserId(param.getAccount());
        if (userId == null) {
            return R.fail(404, "用户不存在");
        }
        userStyleConfigService.saveLayoutConfig(userId, param.getTenantId(), param.getConfig());
        return R.ok(Boolean.TRUE);
    }

    /**
     * 根据账号解析用户ID。
     *
     * @param account 用户账号
     * @return 对应的用户ID，若不存在则返回 null
     */
    private Long resolveUserId(String account) {
        if (account == null || account.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account).last("limit 1");
        SysUser user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
