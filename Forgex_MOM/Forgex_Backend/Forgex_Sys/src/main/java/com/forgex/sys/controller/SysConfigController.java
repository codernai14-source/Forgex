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

import com.forgex.common.config.ConfigService;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.config.CaptchaConfig;
import com.forgex.sys.domain.config.FileUploadConfig;
import com.forgex.sys.domain.config.SecurityConfig;
import com.forgex.sys.domain.config.SystemBasicConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器。
 * 
 * 职责：
 * - 接收 HTTP 请求
 * - 参数校验（调用 Validator）
 * - 调用 ConfigService 层方法
 * - 返回响应结果
 * 
 * 提供登录验证码配置、系统基础配置、安全配置、文件上传配置的读取与更新接口。
 * Controller 仅负责参数接收与结果返回，配置持久化由配置服务完成。
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @see ConfigService
 */
@RestController
@RequestMapping("/sys/config")
public class SysConfigController {
    private static final String KEY_LOGIN_CAPTCHA = "login.captcha";
    private static final String KEY_SYSTEM_BASIC = "system.basic";
    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";
    private static final String KEY_SECURITY_CRYPTO_TRANSPORT = "security.crypto.transport";
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";

    @Autowired
    private ConfigService configService;
    
    /**
     * 获取登录验证码配置
     * <p>
     * 接口路径：GET /sys/config/login-captcha
     * 需要权限：sys:config:view
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用 ConfigService 获取验证码配置</li>
     *   <li>使用默认配置作为兜底值</li>
     *   <li>返回验证码配置对象</li>
     * </ol>
     *
     * @return {@link R} 包含验证码配置的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 验证码配置对象（CaptchaConfig）
     *         - message: 提示信息
     * @see CaptchaConfig
     */
    @GetMapping("/login-captcha")
    public R<CaptchaConfig> getLoginCaptcha() {
        // 1. 调用 ConfigService 获取验证码配置，使用默认配置作为兜底
        CaptchaConfig c = configService.getJson(KEY_LOGIN_CAPTCHA, CaptchaConfig.class, CaptchaConfig.defaults());
        // 2. 返回验证码配置对象
        return R.ok(c);
    }
    
    /**
     * 更新登录验证码配置
     * <p>
     * 接口路径：PUT /sys/config/login-captcha
     * 需要权限：sys:config:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收配置参数（JSON 格式）</li>
     *   <li>调用 ConfigService 保存配置</li>
     *   <li>返回保存成功提示</li>
     * </ol>
     *
     * @param body 配置体（JSON 格式）
     *             - enabled: 是否启用验证码（可选）
     *             - captchaType: 验证码类型（可选，image/slider）
     *             - retryCount: 最大重试次数（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（保存成功）
     * @see CaptchaConfig
     */
    @PutMapping("/login-captcha")
    public R<Boolean> setLoginCaptcha(@RequestBody Map<String, Object> body) {
        // 1. 调用 ConfigService 保存配置
        configService.setJson(KEY_LOGIN_CAPTCHA, body);
        // 2. 返回保存成功提示
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }
    
    /**
     * 获取全局系统基础配置
     * <p>
     * 接口路径：GET /sys/config/system-basic
     * 需要权限：sys:config:view
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用 ConfigService 获取全局系统基础配置</li>
     *   <li>使用默认配置作为兜底值</li>
     *   <li>返回系统基础配置对象</li>
     * </ol>
     *
     * @return {@link R} 包含系统基础配置的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 系统基础配置对象（SystemBasicConfig）
     *         - message: 提示信息
     * @see SystemBasicConfig
     */
    @GetMapping("/system-basic")
    public R<SystemBasicConfig> getSystemBasicConfig() {
        // 1. 调用 ConfigService 获取全局系统基础配置，使用默认配置作为兜底
        SystemBasicConfig c = configService.getGlobalJson(KEY_SYSTEM_BASIC, SystemBasicConfig.class, SystemBasicConfig.defaults());
        // 2. 返回系统基础配置对象
        return R.ok(c);
    }
    
    /**
     * 更新全局系统基础配置
     * <p>
     * 接口路径：PUT /sys/config/system-basic
     * 需要权限：sys:config:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收系统基础配置对象</li>
     *   <li>调用 ConfigService 保存全局配置</li>
     *   <li>返回保存成功提示</li>
     * </ol>
     *
     * @param config 系统基础配置对象
     *               - systemName: 系统名称（可选）
     *               - systemLogo: 系统 Logo URL（可选）
     *               - copyright: 版权信息（可选）
     *               - footer: 页脚信息（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（保存成功）
     * @see SystemBasicConfig
     */
    @PutMapping("/system-basic")
    public R<Boolean> setSystemBasicConfig(@RequestBody SystemBasicConfig config) {
        // 1. 调用 ConfigService 保存全局配置
        configService.setGlobalJson(KEY_SYSTEM_BASIC, config);
        // 2. 返回保存成功提示
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }
    
    /**
     * 获取聚合安全配置
     * <p>
     * 接口路径：GET /sys/config/security
     * 需要权限：sys:config:view
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>获取默认安全配置</li>
     *   <li>从 ConfigService 获取验证码配置</li>
     *   <li>从 ConfigService 获取密码策略配置</li>
     *   <li>从 ConfigService 获取加密传输配置</li>
     *   <li>组装并返回安全配置对象</li>
     * </ol>
     *
     * @return {@link R} 包含安全配置的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 安全配置对象（SecurityConfig）
     *           - captcha: 验证码配置
     *           - passwordPolicy: 密码策略配置
     *           - cryptoTransport: 加密传输配置
     *         - message: 提示信息
     * @see SecurityConfig
     * @see CaptchaConfig
     * @see PasswordPolicyConfig
     * @see CryptoTransportConfig
     */
    @GetMapping("/security")
    public R<SecurityConfig> getSecurityConfig() {
        // 1. 获取默认安全配置
        SecurityConfig defaults = SecurityConfig.defaults();
        
        // 2. 从 ConfigService 获取各项配置
        CaptchaConfig captcha = configService.getJson(KEY_LOGIN_CAPTCHA, CaptchaConfig.class, defaults.getCaptcha());
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults.getPasswordPolicy());
        CryptoTransportConfig transport = configService.getJson(KEY_SECURITY_CRYPTO_TRANSPORT, CryptoTransportConfig.class, defaults.getCryptoTransport());

        // 3. 组装安全配置对象
        SecurityConfig result = new SecurityConfig();
        result.setCaptcha(captcha == null ? CaptchaConfig.defaults() : captcha);
        result.setPasswordPolicy(policy == null ? SecurityConfig.defaults().getPasswordPolicy() : policy);
        result.setCryptoTransport(transport == null ? SecurityConfig.defaults().getCryptoTransport() : transport);
        
        // 4. 返回安全配置对象
        return R.ok(result);
    }
    
    /**
     * 保存聚合安全配置
     * <p>
     * 接口路径：PUT /sys/config/security
     * 需要权限：sys:config:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收安全配置对象</li>
     *   <li>使用默认值填充空配置</li>
     *   <li>分别保存验证码配置、密码策略配置、加密传输配置</li>
     *   <li>返回保存成功提示</li>
     * </ol>
     *
     * @param body 安全配置对象
     *             - captcha: 验证码配置（可选）
     *             - passwordPolicy: 密码策略配置（可选）
     *             - cryptoTransport: 加密传输配置（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（保存成功）
     * @see SecurityConfig
     */
    @PutMapping("/security")
    public R<Boolean> setSecurityConfig(@RequestBody SecurityConfig body) {
        // 1. 使用默认值填充空配置
        SecurityConfig config = body == null ? SecurityConfig.defaults() : body;

        // 2. 分别获取各项配置，使用默认值兜底
        CaptchaConfig captcha = config.getCaptcha() == null ? CaptchaConfig.defaults() : config.getCaptcha();
        PasswordPolicyConfig policy = config.getPasswordPolicy() == null ? SecurityConfig.defaults().getPasswordPolicy() : config.getPasswordPolicy();
        CryptoTransportConfig transport = config.getCryptoTransport() == null ? SecurityConfig.defaults().getCryptoTransport() : config.getCryptoTransport();

        // 3. 分别保存各项配置
        configService.setJson(KEY_LOGIN_CAPTCHA, captcha);
        configService.setJson(KEY_SECURITY_PASSWORD_POLICY, policy);
        configService.setJson(KEY_SECURITY_CRYPTO_TRANSPORT, transport);
        
        // 4. 返回保存成功提示
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }
    
    /**
     * 获取文件上传配置
     * <p>
     * 接口路径：GET /sys/config/file-upload
     * 需要权限：sys:config:view
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用 ConfigService 获取全局文件上传配置</li>
     *   <li>使用默认配置作为兜底值</li>
     *   <li>返回文件上传配置对象</li>
     * </ol>
     *
     * @return {@link R} 包含文件上传配置的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 文件上传配置对象（FileUploadConfig）
     *         - message: 提示信息
     * @see FileUploadConfig
     */
    @GetMapping("/file-upload")
    public R<FileUploadConfig> getFileUploadConfig() {
        // 1. 调用 ConfigService 获取全局文件上传配置，使用默认配置作为兜底
        FileUploadConfig c = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, FileUploadConfig.defaults());
        // 2. 返回文件上传配置对象（为空时返回默认配置）
        return R.ok(c == null ? FileUploadConfig.defaults() : c);
    }
    
    /**
     * 保存文件上传配置
     * <p>
     * 接口路径：PUT /sys/config/file-upload
     * 需要权限：sys:config:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收文件上传配置对象</li>
     *   <li>使用默认配置填充空值</li>
     *   <li>调用 ConfigService 保存全局配置</li>
     *   <li>返回保存成功提示</li>
     * </ol>
     *
     * @param config 文件上传配置对象
     *               - maxFileSize: 最大文件大小（可选，单位 MB）
     *               - allowedExtensions: 允许的文件扩展名（可选）
     *               - storageType: 存储类型（可选，local/oss/minio）
     *               - uploadPath: 上传路径（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（保存成功）
     * @see FileUploadConfig
     */
    @PutMapping("/file-upload")
    public R<Boolean> setFileUploadConfig(@RequestBody FileUploadConfig config) {
        // 1. 使用默认配置填充空值
        configService.setGlobalJson(KEY_FILE_UPLOAD, config == null ? FileUploadConfig.defaults() : config);
        // 2. 返回保存成功提示
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }
}
