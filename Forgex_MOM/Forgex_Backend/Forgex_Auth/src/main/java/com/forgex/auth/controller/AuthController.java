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
import com.forgex.auth.domain.param.RegisterParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.service.AuthService;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.config.ConfigService;
import com.forgex.common.i18n.CommonPrompt;
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
 * @since 2026-03-28
 * @see com.forgex.auth.service.AuthService
 * @see com.forgex.auth.service.CaptchaService
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 认证服务
     */
    @Autowired
    private AuthService authService;

    /**
     * 验证码服务
     */
    @Autowired
    private CaptchaService captchaService;

    /**
     * 配置服务
     */
    @Autowired
    private ConfigService configService;

    /**
     * 用户登录接口
     * <p>
     * 接口路径：POST /auth/login
     * 需要认证：否（登录接口）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收前端传入的登录参数（账号、密码、验证码等）</li>
     *   <li>调用 {@link AuthService#login(LoginParam)} 处理登录逻辑</li>
     *   <li>查询该用户绑定的所有租户列表</li>
     *   <li>返回租户 VO 列表</li>
     * </ol>
     *
     * @param param 登录参数，包含账号、密码、验证码等信息
     *              - account: 用户账号（必填）
     *              - password: 密码（必填，SM2 加密）
     *              - captchaId: 验证码 ID（必填）
     *              - captchaCode: 验证码（必填）
     * @return {@link R} 包含租户 VO 列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 租户列表（List&lt;TenantVO&gt;）
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 验证码错误或账号密码错误时抛出
     * @see AuthService#login(LoginParam)
     * @see TenantVO
     */
    @PostMapping("/login")
    public R<List<TenantVO>> login(@RequestBody LoginParam param) {
        // 委派给服务层处理登录逻辑
        return authService.login(param);
    }

    /**
     * 选择租户接口：设置当前租户上下文
     * <p>
     * 接口路径：POST /auth/chooseTenant
     * 需要认证：是（需要先登录）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户选择的租户 ID</li>
     *   <li>调用 {@link AuthService#chooseTenant(TenantChoiceParam)} 设置租户上下文</li>
     *   <li>返回当前登录用户的详细信息（含头像、当前租户 ID 等）</li>
     * </ol>
     *
     * @param param 选择租户参数，包含账号和租户 ID
     *              - account: 用户账号（必填）
     *              - tenantId: 租户 ID（必填）
     * @return {@link R} 包含用户 DTO 的操作结果
     *         - code: 状态码（200=成功）
     *         - data: 用户详细信息（SysUserDTO）
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 租户不存在或无权访问时抛出
     * @see AuthService#chooseTenant(TenantChoiceParam)
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
     * @return {@link R} 包含用户 DTO 的操作结果
     * @throws com.forgex.common.exception.BusinessException 租户不存在或无权访问时抛出
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
     * 接口路径：POST /auth/tenant/preferences
     * 需要认证：是
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体提取账号参数</li>
     *   <li>提取租户排序列表（ordered 字段）</li>
     *   <li>提取默认租户 ID（可选）</li>
     *   <li>将 ordered 对象转换为 Long 列表</li>
     *   <li>委派给服务层更新租户偏好设置</li>
     * </ol>
     *
     * @param body 请求体，包含账号、租户排序列表和默认租户 ID
     *             - account: 用户账号（必填）
     *             - ordered: 租户 ID 列表（按喜好顺序排列）
     *             - defaultTenantId: 默认租户 ID（可选）
     * @return {@link R} 更新是否成功
     *         - code: 状态码（200=成功）
     *         - data: true=成功，false=失败
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @PostMapping("/tenant/preferences")
    public R<Boolean> updateTenantPreferences(@RequestBody Map<String, Object> body) {
        // 1. 从请求体中提取账号
        String account = (String) body.get("account");
        
        // 2. 提取租户排序列表（前端保存的顺序）
        Object orderedObj = body.get("ordered");
        
        // 3. 提取默认租户 ID（可选，可能为 null）
        Long defaultTenantId = body.get("defaultTenantId") == null 
            ? null 
            : Long.valueOf(String.valueOf(body.get("defaultTenantId")));
        
        // 4. 将 ordered 对象转换为 Long 列表
        java.util.List<Long> ordered = new java.util.ArrayList<>();
        if (orderedObj instanceof java.util.List<?>) {
            for (Object o : (java.util.List<?>) orderedObj) {
                ordered.add(Long.valueOf(String.valueOf(o)));
            }
        }
        
        // 5. 委派给服务层更新租户偏好设置
        return authService.updateTenantPreferences(account, ordered, defaultTenantId);
    }

    /**
     * 管理员权限校验示例接口
     * <p>
     * 接口路径：POST /auth/secure/admin
     * 需要认证：是
     * </p>
     * <p>用途：</p>
     * <ul>
     *   <li>验证当前登录用户是否拥有管理员角色</li>
     *   <li>用于权限控制示例</li>
     * </ul>
     *
     * @return {@link R} 校验结果，true 表示拥有管理员权限
     *         - code: 状态码（200=成功）
     *         - data: true=拥有管理员权限，false=无管理员权限
     *         - message: 提示信息
     * @see AuthService#secureAdmin()
     * @deprecated 仅用于示例，生产环境请使用标准权限控制
     */
    @Deprecated
    @PostMapping("/secure/admin")
    public R<Boolean> secureAdmin() {
        // 委派给服务层检查管理员权限
        return authService.secureAdmin();
    }

    /**
     * 重置用户密码
     * <p>
     * 接口路径：POST /auth/reset-password
     * 需要权限：sys:user:resetPwd
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID</li>
     *   <li>调用服务层重置密码</li>
     *   <li>返回重置结果</li>
     * </ol>
     *
     * @param param 重置密码参数，包含用户 ID
     *              - userId: 用户 ID（必填）
     * @return {@link R} 重置是否成功
     *         - code: 状态码（200=成功）
     *         - data: true=成功，false=失败
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 用户不存在时抛出
     * @see AuthService#resetPasswordById(Long)
     */
    @PostMapping("/reset-password")
    public R<Boolean> resetPassword(@RequestBody com.forgex.auth.domain.param.ResetPasswordParam param) {
        // 从参数中提取用户 ID
        Long userId = param == null ? null : param.getUserId();
        // 委派给服务层重置密码
        return authService.resetPasswordById(userId);
    }

    /**
     * 生成图片验证码
     * <p>
     * 接口路径：POST /auth/captcha/image
     * 需要认证：否
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用验证码服务生成图片验证码</li>
     *   <li>将验证码存入 Redis，设置过期时间</li>
     *   <li>返回验证码 ID 和 Base64 编码的图片</li>
     * </ol>
     *
     * @return {@link R} 统一返回结构，包含图片验证码信息
     *         - code: 状态码（200=成功）
     *         - data: 验证码信息（Map）
     *           - captchaId: 验证码 ID
     *           - imageBase64: Base64 编码的图片
     *         - message: 提示信息
     * @see CaptchaService#generateImageCaptcha()
     */
    @PostMapping("/captcha/image")
    public R<Map<String, String>> captchaImage() {
        // 委派给验证码服务生成图片验证码
        return R.ok(captchaService.generateImageCaptcha());
    }

    /**
     * 生成滑块验证码
     * <p>
     * 接口路径：POST /auth/captcha/slider
     * 需要认证：否
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用验证码服务生成滑块验证码数据</li>
     *   <li>返回滑块图片、缺口图片、验证码 ID 等渲染所需数据</li>
     * </ol>
     *
     * @return {@link R} 验证码渲染数据
     *         - code: 状态码（200=成功）
     *         - data: 滑块验证码数据（Object）
     *           - captchaId: 验证码 ID
     *           - backgroundImage: 滑块图片（Base64）
     *           - sliderImage: 缺口图片（Base64）
     *         - message: 提示信息
     * @see CaptchaService#generateSliderCaptcha()
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
     * 接口路径：POST /auth/captcha/slider/validate
     * 需要认证：否
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收前端传来的滑块轨迹数据</li>
     *   <li>校验轨迹是否正确</li>
     *   <li>校验通过后生成一次性令牌</li>
     *   <li>返回令牌供登录时使用</li>
     * </ol>
     *
     * @param param 滑块校验参数，包含 {@code id} 与 {@code track}
     *              - id: 验证码 ID（必填）
     *              - track: 滑块轨迹数据（必填）
     * @return {@link R} 令牌字符串（登录时作为验证码使用），校验失败返回错误信息
     *         - code: 状态码（200=成功）
     *         - data: 一次性令牌字符串
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 轨迹校验失败时抛出
     * @see CaptchaService#validateSlider(String, Object)
     */
    @PostMapping("/captcha/slider/validate")
    public R<String> captchaSliderValidate(@RequestBody SliderValidateParam param) {
        // 1. 参数校验：检查 id 和 track 是否为空
        if (param == null || param.getId() == null || param.getTrack() == null) {
            return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }
        
        // 2. 调用服务层校验滑块轨迹
        String token = captchaService.validateSlider(param.getId(), param.getTrack());
        
        // 3. 返回校验结果
        if (token != null) {
            return R.ok(token);
        }
        return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
    }

    /**
     * 用户登出
     * <p>
     * 接口路径：POST /auth/logout
     * 需要认证：是
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用服务层执行登出逻辑</li>
     *   <li>清除 SaToken 会话信息</li>
     *   <li>删除 Redis 中的登录上下文</li>
     *   <li>清除租户上下文</li>
     * </ol>
     *
     * @return {@link R} 登出是否成功
     *         - code: 状态码（200=成功）
     *         - data: true=成功，false=失败
     *         - message: 提示信息
     * @see AuthService#logout()
     */
    @PostMapping("/logout")
    public R<Boolean> logout() {
        // 委派给服务层处理登出逻辑
        return authService.logout();
    }

    /**
     * 切换用户语言偏好
     * <p>
     * 接口路径：POST /auth/changeLanguage
     * 需要认证：是
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收前端传入的语言编码</li>
     *   <li>调用服务层更新用户语言偏好</li>
     *   <li>返回操作结果</li>
     * </ol>
     *
     * @param param 切换语言参数，包含语言编码
     *              - lang: 语言编码（必填，如 zh_CN/en_US）
     * @return {@link R} 切换是否成功
     *         - code: 状态码（200=成功）
     *         - data: true=成功，false=失败
     *         - message: 提示信息
     * @throws com.forgex.common.exception.BusinessException 语言编码不支持时抛出
     */
    @PostMapping("/changeLanguage")
    public R<Boolean> changeLanguage(@RequestBody ChangeLanguageParam param) {
        // 从参数中提取语言编码
        String lang = param == null ? null : param.getLang();
        // 委派给服务层切换语言
        return authService.changeLanguage(lang);
    }

    /**
     * 获取前端传输加密所需公钥
     * <p>
     * 接口路径：GET /auth/crypto/public-key
     * 需要认证：否
     * </p>
     * <p>用途：</p>
     * <ul>
     *   <li>前端使用该公钥对登录密码进行 SM2 加密</li>
     *   <li>后端使用对应的私钥解密</li>
     *   <li>确保密码在传输过程中的安全性</li>
     * </ul>
     *
     * @return {@link R} SM2 公钥 Base64 编码字符串，如果未配置则返回错误信息
     *         - code: 状态码（200=成功）
     *         - data: SM2 公钥 Base64 字符串
     *         - message: 提示信息
     * @see CryptoTransportConfig
     */
    @GetMapping("/crypto/public-key")
    public R<String> publicKey() {
        // 从配置中读取 SM2 公钥
        CryptoTransportConfig cfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        String pub = cfg == null ? null : cfg.getPublicKey();
        // 返回公钥或错误信息
        return pub != null ? R.ok(pub) : R.fail(CommonPrompt.PUBLIC_KEY_NOT_CONFIGURED);
    }

    /**
     * 邀请码注册接口
     * <p>
     * 接口路径：POST /auth/register
     * 需要认证：否（匿名接口）
     * </p>
     *
     * @param param 注册参数，包含账号、密码、邀请码、验证码等
     * @return 注册结果
     */
    @PostMapping("/register")
    public R<Boolean> register(@RequestBody RegisterParam param) {
        return authService.register(param);
    }

    /**
     * 校验邀请码接口
     * <p>
     * 接口路径：POST /auth/register/check-invite
     * 需要认证：否（匿名接口）
     * </p>
     *
     * @param body 请求体，包含 inviteCode
     * @return 校验结果
     */
    @PostMapping("/register/check-invite")
    public R<Boolean> checkInviteCode(@RequestBody Map<String, String> body) {
        String inviteCode = body == null ? null : body.get("inviteCode");
        return authService.validateInviteCode(inviteCode);
    }
}
