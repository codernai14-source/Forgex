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

import cn.hutool.core.util.HexUtil;
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.RSAPasswordProvider;
import com.forgex.common.crypto.TdeConfigChecker;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.EmailConfig;
import com.forgex.common.domain.config.LoginSecurityConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.config.CaptchaConfig;
import com.forgex.sys.domain.config.CryptoConfig;
import com.forgex.sys.domain.config.FileUploadConfig;
import com.forgex.sys.domain.config.SecurityConfig;
import com.forgex.sys.domain.config.SystemBasicConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.security.SecureRandom;
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
    private static final String KEY_SECURITY_LOGIN_FAILURE = "security.login.failure";
    private static final String KEY_SECURITY_CRYPTO_TRANSPORT = "security.crypto.transport";
    private static final String KEY_MAIL_SETTINGS = "mail.settings";
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";

    // 加密配置键常量
    private static final String KEY_CRYPTO_SM4 = "security.crypto.sm4";
    private static final String KEY_CRYPTO_AES = "security.crypto.aes";
    private static final String KEY_CRYPTO_RSA = "security.crypto.rsa";
    private static final String KEY_KMS_MASTER = "security.kms.master";
    private static final String KEY_FILE_ENCRYPT = "security.file.encrypt";
    private static final String KEY_FIELD_ENCRYPT = "security.field.encrypt";

    @Autowired
    private ConfigService configService;

    @Autowired
    private DataSource dataSource;

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
    @RequirePerm("sys:config:edit")
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
    @RequirePerm("sys:config:edit")
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
    @RequirePerm("sys:config:view")
    @GetMapping("/security")
    public R<SecurityConfig> getSecurityConfig() {
        // 1. 获取默认安全配置
        SecurityConfig defaults = SecurityConfig.defaults();
        
        // 2. 从 ConfigService 获取各项配置
        CaptchaConfig captcha = configService.getJson(KEY_LOGIN_CAPTCHA, CaptchaConfig.class, defaults.getCaptcha());
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults.getPasswordPolicy());
        LoginSecurityConfig loginSecurity = configService.getJson(KEY_SECURITY_LOGIN_FAILURE, LoginSecurityConfig.class, defaults.getLoginSecurity());
        CryptoTransportConfig transport = configService.getJson(KEY_SECURITY_CRYPTO_TRANSPORT, CryptoTransportConfig.class, defaults.getCryptoTransport());

        // 3. 组装安全配置对象
        SecurityConfig result = new SecurityConfig();
        result.setCaptcha(captcha == null ? CaptchaConfig.defaults() : captcha);
        result.setPasswordPolicy(policy == null ? SecurityConfig.defaults().getPasswordPolicy() : policy);
        result.setLoginSecurity(loginSecurity == null ? SecurityConfig.defaults().getLoginSecurity() : loginSecurity);
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
    @RequirePerm("sys:config:edit")
    @PutMapping("/security")
    public R<Boolean> setSecurityConfig(@RequestBody SecurityConfig body) {
        // 1. 使用默认值填充空配置
        SecurityConfig config = body == null ? SecurityConfig.defaults() : body;

        // 2. 分别获取各项配置，使用默认值兜底
        CaptchaConfig captcha = config.getCaptcha() == null ? CaptchaConfig.defaults() : config.getCaptcha();
        PasswordPolicyConfig policy = config.getPasswordPolicy() == null ? SecurityConfig.defaults().getPasswordPolicy() : config.getPasswordPolicy();
        LoginSecurityConfig loginSecurity = config.getLoginSecurity() == null ? SecurityConfig.defaults().getLoginSecurity() : config.getLoginSecurity();
        CryptoTransportConfig transport = config.getCryptoTransport() == null ? SecurityConfig.defaults().getCryptoTransport() : config.getCryptoTransport();

        if (!isPasswordValid(policy.getDefaultPassword(), policy)) {
            return R.fail(CommonPrompt.DEFAULT_PASSWORD_INVALID);
        }

        // 3. 分别保存各项配置
        configService.setJson(KEY_LOGIN_CAPTCHA, captcha);
        configService.setJson(KEY_SECURITY_PASSWORD_POLICY, policy);
        configService.setJson(KEY_SECURITY_LOGIN_FAILURE, loginSecurity);
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
    @RequirePerm("sys:config:view")
    @GetMapping("/email")
    public R<EmailConfig> getEmailConfig() {
        EmailConfig config = configService.getGlobalJson(KEY_MAIL_SETTINGS, EmailConfig.class, EmailConfig.defaults());
        return R.ok(config == null ? EmailConfig.defaults() : config);
    }

    @RequirePerm("sys:config:edit")
    @PutMapping("/email")
    public R<Boolean> setEmailConfig(@RequestBody EmailConfig config) {
        configService.setGlobalJson(KEY_MAIL_SETTINGS, config == null ? EmailConfig.defaults() : config);
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }

    @RequirePerm("sys:config:view")
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
    @RequirePerm("sys:config:edit")
    @PutMapping("/file-upload")
    public R<Boolean> setFileUploadConfig(@RequestBody FileUploadConfig config) {
        // 1. 使用默认配置填充空值
        configService.setGlobalJson(KEY_FILE_UPLOAD, config == null ? FileUploadConfig.defaults() : config);
        // 2. 返回保存成功提示
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }

    // ======================== 加密配置接口 ========================

    /**
     * 获取加密配置
     * <p>
     * 接口路径：GET /sys/config/crypto
     * 需要权限：sys:config:query
     * </p>
     *
     * @return {@link R} 包含加密配置的统一返回结构
     */
    @RequirePerm("sys:config:view")
    @GetMapping("/crypto")
    public R<CryptoConfig> getCryptoConfig() {
        CryptoConfig config = new CryptoConfig();
        CryptoConfig.Sm4Config sm4 = configService.getJson(KEY_CRYPTO_SM4, CryptoConfig.Sm4Config.class, new CryptoConfig.Sm4Config());
        CryptoConfig.AesConfig aes = configService.getJson(KEY_CRYPTO_AES, CryptoConfig.AesConfig.class, new CryptoConfig.AesConfig());
        CryptoConfig.RsaConfig rsa = configService.getJson(KEY_CRYPTO_RSA, CryptoConfig.RsaConfig.class, new CryptoConfig.RsaConfig());
        CryptoConfig.KmsConfig kms = configService.getJson(KEY_KMS_MASTER, CryptoConfig.KmsConfig.class, new CryptoConfig.KmsConfig());
        CryptoConfig.FileEncryptConfig fileEnc = configService.getJson(KEY_FILE_ENCRYPT, CryptoConfig.FileEncryptConfig.class, new CryptoConfig.FileEncryptConfig());
        CryptoConfig.FieldEncryptConfig fieldEnc = configService.getJson(KEY_FIELD_ENCRYPT, CryptoConfig.FieldEncryptConfig.class, new CryptoConfig.FieldEncryptConfig());
        config.setSm4(sm4 != null ? sm4 : new CryptoConfig.Sm4Config());
        config.setAes(aes != null ? aes : new CryptoConfig.AesConfig());
        config.setRsa(rsa != null ? rsa : new CryptoConfig.RsaConfig());
        config.setKms(kms != null ? kms : new CryptoConfig.KmsConfig());
        config.setFileEncrypt(fileEnc != null ? fileEnc : new CryptoConfig.FileEncryptConfig());
        config.setFieldEncrypt(fieldEnc != null ? fieldEnc : new CryptoConfig.FieldEncryptConfig());
        return R.ok(config);
    }

    /**
     * 保存加密配置
     * <p>
     * 接口路径：PUT /sys/config/crypto
     * 需要权限：sys:config:edit
     * </p>
     *
     * @param config 加密配置聚合对象
     * @return {@link R} 操作结果
     */
    @RequirePerm("sys:config:edit")
    @PutMapping("/crypto")
    public R<Boolean> setCryptoConfig(@RequestBody CryptoConfig config) {
        if (config.getSm4() != null) configService.setJson(KEY_CRYPTO_SM4, config.getSm4());
        if (config.getAes() != null) configService.setJson(KEY_CRYPTO_AES, config.getAes());
        if (config.getRsa() != null) configService.setJson(KEY_CRYPTO_RSA, config.getRsa());
        if (config.getKms() != null) configService.setJson(KEY_KMS_MASTER, config.getKms());
        if (config.getFileEncrypt() != null) configService.setJson(KEY_FILE_ENCRYPT, config.getFileEncrypt());
        if (config.getFieldEncrypt() != null) configService.setJson(KEY_FIELD_ENCRYPT, config.getFieldEncrypt());
        return R.ok(CommonPrompt.SAVE_SUCCESS, true);
    }

    /**
     * 生成 SM4 密钥（128位，32字符Hex）
     * <p>
     * 接口路径：POST /sys/config/crypto/generate/sm4
     * 需要权限：sys:config:edit
     * </p>
     */
    @RequirePerm("sys:config:edit")
    @PostMapping("/crypto/generate/sm4")
    public R<Map<String, String>> generateSm4Key() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        String keyHex = HexUtil.encodeHexStr(key);
        return R.ok(Map.of("keyHex", keyHex));
    }

    /**
     * 生成 AES-256 密钥（256位，64字符Hex）
     * <p>
     * 接口路径：POST /sys/config/crypto/generate/aes
     * 需要权限：sys:config:edit
     * </p>
     */
    @RequirePerm("sys:config:edit")
    @PostMapping("/crypto/generate/aes")
    public R<Map<String, String>> generateAesKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String keyHex = HexUtil.encodeHexStr(key);
        return R.ok(Map.of("keyHex", keyHex));
    }

    /**
     * 生成 RSA 密钥对
     * <p>
     * 接口路径：POST /sys/config/crypto/generate/rsa?keySize=2048
     * 需要权限：sys:config:edit
     * </p>
     *
     * @param keySize 密钥长度（2048 或 4096，默认 2048）
     */
    @RequirePerm("sys:config:edit")
    @PostMapping("/crypto/generate/rsa")
    public R<Map<String, String>> generateRsaKeyPair(@RequestParam(defaultValue = "2048") int keySize) {
        String[] keys = RSAPasswordProvider.generateKeyPair(keySize);
        return R.ok(Map.of("publicKey", keys[0], "privateKey", keys[1]));
    }

    /**
     * 生成 KMS 主密钥（256位，64字符Hex）
     * <p>
     * 接口路径：POST /sys/config/crypto/generate/kms-master
     * 需要权限：sys:config:edit
     * </p>
     */
    @RequirePerm("sys:config:edit")
    @PostMapping("/crypto/generate/kms-master")
    public R<Map<String, String>> generateKmsMasterKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String keyHex = HexUtil.encodeHexStr(key);
        return R.ok(Map.of("masterKeyHex", keyHex));
    }

    /**
     * 检测 TDE 数据库透明加密状态
     * <p>
     * 接口路径：GET /sys/config/crypto/tde-status
     * 需要权限：sys:config:query
     * </p>
     */
    @RequirePerm("sys:config:view")
    @GetMapping("/crypto/tde-status")
    public R<TdeConfigChecker.TdeStatus> getTdeStatus() {
        TdeConfigChecker.TdeStatus status = TdeConfigChecker.check(dataSource);
        return R.ok(status);
    }

    private boolean isPasswordValid(String password, PasswordPolicyConfig policy) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        int minLength = policy == null || policy.getMinLength() == null ? 0 : policy.getMinLength();
        if (password.length() < minLength) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireNumbers()) && !password.matches(".*\\d.*")) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireUppercase()) && !password.matches(".*[A-Z].*")) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireLowercase()) && !password.matches(".*[a-z].*")) {
            return false;
        }
        if (policy != null && Boolean.TRUE.equals(policy.getRequireSymbols()) && !password.matches(".*[^A-Za-z0-9].*")) {
            return false;
        }
        return true;
    }
}
