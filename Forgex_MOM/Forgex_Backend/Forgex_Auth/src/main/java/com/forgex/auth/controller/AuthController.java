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
package com.forgex.auth.controller;

import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.domain.param.SliderValidateParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.service.AuthService;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.config.ConfigService;
import com.forgex.common.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

import com.forgex.common.domain.config.CryptoTransportConfig;

/**
 * 认证控制器。
 * <p>
 * 提供与登录相关的入口接口：登录、选择租户、获取传输加密公钥、验证码校验等。
 * Controller 仅负责参数接收与结果返回，业务逻辑在 Service 层处理。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private ConfigService configService;

    /**
     * 登录接口：输入账号密码，返回绑定的租户列表
     * 逻辑：委派给服务层处理登录并查询租户
     * @param param 登录参数
     * @return 租户VO列表的统一返回
     * @see com.forgex.auth.service.AuthService#login(LoginParam)
     */
    @PostMapping("/login")
    public R<List<TenantVO>> login(@RequestBody LoginParam param) {
        return authService.login(param);
    }

    /**
     * 选择租户接口：设置当前租户上下文
     * 逻辑：委派服务层写入租户上下文
     * @param param 选择租户参数
     * @return 操作结果
     * @see com.forgex.auth.service.AuthService#chooseTenant(TenantChoiceParam)
     */
    @PostMapping("/chooseTenant")
    public R<com.forgex.auth.domain.dto.SysUserDTO> chooseTenant(@RequestBody TenantChoiceParam param) {
        return authService.chooseTenant(param);
    }

    @PostMapping("/choose-tenant")
    public R<com.forgex.auth.domain.dto.SysUserDTO> chooseTenantAlias(@RequestBody TenantChoiceParam param) {
        return authService.chooseTenant(param);
    }

    /**
     * 更新租户偏好排序与默认租户
     * @param body { account: string; ordered: number[]; defaultTenantId?: number }
     */
    @PostMapping("/tenant/preferences")
    public R<Boolean> updateTenantPreferences(@RequestBody Map<String, Object> body) {
        String account = (String) body.get("account");
        Object orderedObj = body.get("ordered");
        Long defaultTenantId = body.get("defaultTenantId") == null ? null : Long.valueOf(String.valueOf(body.get("defaultTenantId")));
        java.util.List<Long> ordered = new java.util.ArrayList<>();
        if (orderedObj instanceof java.util.List<?>) {
            for (Object o : (java.util.List<?>) orderedObj) ordered.add(Long.valueOf(String.valueOf(o)));
        }
        return authService.updateTenantPreferences(account, ordered, defaultTenantId);
    }

    /**
     * 管理员权限校验示例接口
     * 逻辑：委派服务层检查角色
     * @return 操作结果
     * @see com.forgex.auth.service.AuthService#secureAdmin()
     */
    @PostMapping("/secure/admin")
    public R<Boolean> secureAdmin() {
        return authService.secureAdmin();
    }

    @PostMapping("/reset-password")
    public R<Boolean> resetPassword(@RequestBody com.forgex.auth.domain.param.ResetPasswordParam param) {
        Long userId = param == null ? null : param.getUserId();
        return authService.resetPasswordById(userId);
    }

    /**
     * 生成图片验证码
     * 逻辑：委派验证码服务生成图片并写入Redis，返回 `captchaId` 与 `imageBase64`
     * @return 统一返回结构，包含图片验证码信息
     * @see com.forgex.auth.service.CaptchaService#generateImageCaptcha()
     */
    @PostMapping("/captcha/image")
    public R<Map<String, String>> captchaImage() {
        return R.ok(captchaService.generateImageCaptcha());
    }

    /**
     * 生成滑块验证码
     * 逻辑：委派验证码服务生成滑块数据，返回渲染所需结构（含 id）
     * @return 验证码渲染数据
     * @see com.forgex.auth.service.CaptchaService#generateSliderCaptcha()
     */
    @PostMapping("/captcha/slider")
    public R<Object> captchaSlider() {
        Object sliderData = captchaService.generateSliderCaptcha();
        return R.ok(sliderData);
    }

    /**
     * 校验滑块轨迹并发放令牌
     * 逻辑：委派验证码服务校验前端轨迹，成功后生成一次性令牌
     * @param param 滑块校验参数，包含 `id` 与 `track`
     * @return 令牌字符串（登录时作为验证码使用）
     * @see com.forgex.auth.service.CaptchaService#validateSlider(String, Object)
     */
    @PostMapping("/captcha/slider/validate")
    public R<String> captchaSliderValidate(@RequestBody SliderValidateParam param) {
        if (param == null || param.getId() == null || param.getTrack() == null) {
            return R.fail(500, "验证码不能为空");
        }
        String token = captchaService.validateSlider(param.getId(), param.getTrack());
        if (token != null) {
            return R.ok(token);
        }
        return R.fail(500, "验证码不正确");
    }

    @PostMapping("/logout")
    public R<Boolean> logout() {
        return authService.logout();
    }

    /**
     * 获取前端传输加密所需公钥
     * 逻辑：读取 common 配置中的 `security.crypto.transport.publicKey`
     */
    @GetMapping("/crypto/public-key")
    /**
     * 获取前端传输加密所需公钥。
     * 从配置库读取 {@code security.crypto.transport.publicKey}，用于前端 SM2 加密登录密码。
     * @return SM2 公钥 Base64
     */
    public R<String> publicKey() {
        CryptoTransportConfig cfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        String pub = cfg == null ? null : cfg.getPublicKey();
        return pub != null ? R.ok(pub) : R.fail(500, "未配置公钥");
    }
}
