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
import com.forgex.auth.domain.param.ChangeLanguageParam;
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
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #login(LoginParam)} - 用户登录，返回绑定的租户列表</li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} - 选择租户，设置当前租户上下文</li>
 *   <li>{@link #captchaImage()} - 生成图片验证码</li>
 *   <li>{@link #captchaSlider()} - 生成滑块验证码</li>
 *   <li>{@link #captchaSliderValidate(SliderValidateParam)} - 校验滑块轨迹</li>
 *   <li>{@link #publicKey()} - 获取前端传输加密公钥</li>
 *   <li>{@link #logout()} - 用户登出</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.auth.service.AuthService
 * @see com.forgex.auth.service.CaptchaService
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
     * <p>
     * 逻辑：委派给服务层处理登录并查询租户
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>接收前端传入的登录参数（账号、密码、验证码等）</li>
     *   <li>调用 {@link com.forgex.auth.service.AuthService#login(LoginParam)} 处理登录逻辑</li>
     *   <li>返回该用户绑定的所有租户列表</li>
     * </ul>
     *
     * @param param 登录参数，包含账号、密码、验证码等信息
     * @return {@link R} 包含租户VO列表的统一返回结构
     * @see com.forgex.auth.service.AuthService#login(LoginParam)
     * @see com.forgex.auth.domain.param.LoginParam
     * @see com.forgex.auth.domain.vo.TenantVO
     */
    @PostMapping("/login")
    public R<List<TenantVO>> login(@RequestBody LoginParam param) {
        // 委派给服务层处理登录逻辑
        return authService.login(param);
    }

    /**
     * 选择租户接口：设置当前租户上下文
     * <p>
     * 逻辑：委派服务层写入租户上下文
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>接收用户选择的租户ID</li>
     *   <li>调用 {@link com.forgex.auth.service.AuthService#chooseTenant(TenantChoiceParam)} 设置租户上下文</li>
     *   <li>返回当前登录用户的详细信息（含头像、当前租户ID等）</li>
     * </ul>
     *
     * @param param 选择租户参数，包含账号和租户ID
     * @return {@link R} 包含用户DTO的操作结果
     * @see com.forgex.auth.service.AuthService#chooseTenant(TenantChoiceParam)
     * @see com.forgex.auth.domain.dto.SysUserDTO
     */
    @PostMapping("/chooseTenant")
    public R<com.forgex.auth.domain.dto.SysUserDTO> chooseTenant(@RequestBody TenantChoiceParam param) {
        // 委派给服务层处理租户选择逻辑
        return authService.chooseTenant(param);
    }

    /**
     * 选择租户接口别名（兼容旧版本）
     * <p>
     * 功能与 {@link #chooseTenant(TenantChoiceParam)} 相同
     * </p>
     *
     * @param param 选择租户参数
     * @return 用户DTO
     * @see #chooseTenant(TenantChoiceParam)
     */
    @PostMapping("/choose-tenant")
    public R<com.forgex.auth.domain.dto.SysUserDTO> chooseTenantAlias(@RequestBody TenantChoiceParam param) {
        // 调用主方法处理
        return authService.chooseTenant(param);
    }

    /**
     * 更新租户偏好排序与默认租户
     * <p>
     * 用于保存用户对租户的排序偏好和默认租户设置
     * </p>
     * <p>参数说明：</p>
     * <ul>
     *   <li>account：用户账号</li>
     *   <li>ordered：租户ID按喜好顺序排列（前端保存的顺序）</li>
     *   <li>defaultTenantId：默认租户ID（可选）</li>
     * </ul>
     *
     * @param body 请求体，包含账号、租户排序列表和默认租户ID
     * @return {@link R} 更新是否成功
     */
    @PostMapping("/tenant/preferences")
    public R<Boolean> updateTenantPreferences(@RequestBody Map<String, Object> body) {
        // 从请求体中提取账号
        String account = (String) body.get("account");
        // 提取租户排序列表
        Object orderedObj = body.get("ordered");
        // 提取默认租户ID（可能为null）
        Long defaultTenantId = body.get("defaultTenantId") == null ? null : Long.valueOf(String.valueOf(body.get("defaultTenantId")));
        // 初始化租户ID列表
        java.util.List<Long> ordered = new java.util.ArrayList<>();
        // 将ordered对象转换为Long列表
        if (orderedObj instanceof java.util.List<?>) {
            for (Object o : (java.util.List<?>) orderedObj) ordered.add(Long.valueOf(String.valueOf(o)));
        }
        // 委派给服务层更新租户偏好设置
        return authService.updateTenantPreferences(account, ordered, defaultTenantId);
    }

    /**
     * 管理员权限校验示例接口
     * <p>
     * 逻辑：委派服务层检查角色
     * </p>
     * <p>用途：</p>
     * <ul>
     *   <li>验证当前登录用户是否拥有管理员角色</li>
     *   <li>用于权限控制示例</li>
     * </ul>
     *
     * @return {@link R} 校验结果，true表示拥有管理员权限
     * @see com.forgex.auth.service.AuthService#secureAdmin()
     */
    @PostMapping("/secure/admin")
    public R<Boolean> secureAdmin() {
        // 委派给服务层检查管理员权限
        return authService.secureAdmin();
    }

    /**
     * 重置用户密码
     * <p>
     * 将指定用户的密码重置为默认密码（123456）
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>接收用户ID</li>
     *   <li>调用服务层重置密码</li>
     *   <li>返回重置结果</li>
     * </ul>
     *
     * @param param 重置密码参数，包含用户ID
     * @return {@link R} 重置是否成功
     * @see com.forgex.auth.service.AuthService#resetPasswordById(Long)
     */
    @PostMapping("/reset-password")
    public R<Boolean> resetPassword(@RequestBody com.forgex.auth.domain.param.ResetPasswordParam param) {
        // 从参数中提取用户ID
        Long userId = param == null ? null : param.getUserId();
        // 委派给服务层重置密码
        return authService.resetPasswordById(userId);
    }

    /**
     * 生成图片验证码
     * <p>
     * 逻辑：委派验证码服务生成图片并写入Redis，返回 {@code captchaId} 与 {@code imageBase64}
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>调用验证码服务生成图片验证码</li>
     *   <li>将验证码存入Redis，设置过期时间</li>
     *   <li>返回验证码ID和Base64编码的图片</li>
     * </ul>
     *
     * @return {@link R} 统一返回结构，包含图片验证码信息（captchaId和imageBase64）
     * @see com.forgex.auth.service.CaptchaService#generateImageCaptcha()
     */
    @PostMapping("/captcha/image")
    public R<Map<String, String>> captchaImage() {
        // 委派给验证码服务生成图片验证码
        return R.ok(captchaService.generateImageCaptcha());
    }

    /**
     * 生成滑块验证码
     * <p>
     * 逻辑：委派验证码服务生成滑块数据，返回渲染所需结构（含 id）
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>调用验证码服务生成滑块验证码数据</li>
     *   <li>返回滑块图片、缺口图片、验证码ID等渲染所需数据</li>
     * </ul>
     *
     * @return {@link R} 验证码渲染数据，包含滑块图片、缺口图片、验证码ID等
     * @see com.forgex.auth.service.CaptchaService#generateSliderCaptcha()
     */
    @PostMapping("/captcha/slider")
    public R<Object> captchaSlider() {
        // 委派给验证码服务生成滑块验证码
        Object sliderData = captchaService.generateSliderCaptcha();
        return R.ok(sliderData);
    }

    /**
     * 校验滑块轨迹并发放令牌
     * <p>
     * 逻辑：委派验证码服务校验前端轨迹，成功后生成一次性令牌
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>接收前端传来的滑块轨迹数据</li>
     *   <li>校验轨迹是否正确</li>
     *   <li>校验通过后生成一次性令牌</li>
     *   <li>返回令牌供登录时使用</li>
     * </ul>
     *
     * @param param 滑块校验参数，包含 {@code id} 与 {@code track}
     * @return {@link R} 令牌字符串（登录时作为验证码使用），校验失败返回错误信息
     * @see com.forgex.auth.service.CaptchaService#validateSlider(String, Object)
     */
    @PostMapping("/captcha/slider/validate")
    public R<String> captchaSliderValidate(@RequestBody SliderValidateParam param) {
        // 校验参数是否为空
        if (param == null || param.getId() == null || param.getTrack() == null) {
            return R.fail(500, "验证码不能为空");
        }
        // 委派给验证码服务校验滑块轨迹
        String token = captchaService.validateSlider(param.getId(), param.getTrack());
        // 校验成功，返回令牌
        if (token != null) {
            return R.ok(token);
        }
        // 校验失败，返回错误信息
        return R.fail(500, "验证码不正确");
    }

    /**
     * 用户登出
     * <p>
     * 清除用户登录状态，删除Redis中的登录上下文信息
     * </p>
     * <p>流程说明：</p>
     * <ul>
     *   <li>调用服务层执行登出逻辑</li>
     *   <li>清除SaToken会话信息</li>
     *   <li>删除Redis中的登录上下文</li>
     *   <li>清除租户上下文</li>
     * </ul>
     *
     * @return {@link R} 登出是否成功
     * @see com.forgex.auth.service.AuthService#logout()
     */
    @PostMapping("/logout")
    public R<Boolean> logout() {
        // 委派给服务层处理登出逻辑
        return authService.logout();
    }

    @PostMapping("/changeLanguage")
    public R<Boolean> changeLanguage(@RequestBody ChangeLanguageParam param) {
        String lang = param == null ? null : param.getLang();
        return authService.changeLanguage(lang);
    }

    /**
     * 获取前端传输加密所需公钥
     * <p>
     * 逻辑：读取 common 配置中的 {@code security.crypto.transport.publicKey}
     * </p>
     * <p>用途：</p>
     * <ul>
     *   <li>前端使用该公钥对登录密码进行SM2加密</li>
     *   <li>后端使用对应的私钥解密</li>
     *   <li>确保密码在传输过程中的安全性</li>
     * </ul>
     *
     * @return {@link R} SM2 公钥 Base64编码字符串，如果未配置则返回错误信息
     * @see com.forgex.common.domain.config.CryptoTransportConfig
     */
    @GetMapping("/crypto/public-key")
    public R<String> publicKey() {
        // 从配置服务中获取传输加密配置
        CryptoTransportConfig cfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        // 提取公钥
        String pub = cfg == null ? null : cfg.getPublicKey();
        // 返回公钥或错误信息
        return pub != null ? R.ok(pub) : R.fail(500, "未配置公钥");
    }
}
