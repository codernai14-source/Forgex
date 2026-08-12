/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.sys.service.impl;

import cn.hutool.core.util.HexUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forgex.common.crypto.RSAPasswordProvider;
import com.forgex.sys.domain.entity.SysKmsKey;
import com.forgex.sys.domain.entity.SysKmsKeyLog;
import com.forgex.sys.mapper.SysKmsKeyLogMapper;
import com.forgex.sys.mapper.SysKmsKeyMapper;
import com.forgex.sys.service.KmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 密钥管理服务实现（KMS）。
 * <p>
 * 使用主密钥（Master Key）对所有业务密钥进行 AES-256-GCM 加密后存储到数据库。
 * 主密钥从外部注入，按优先级读取：
 * <ol>
 *   <li>环境变量 {@code FORGEX_KMS_MASTER_KEY_HEX}（hex 字符串，解码为 32 字节）</li>
 *   <li>环境变量 {@code FORGEX_KMS_MASTER_KEY_FILE} 指向的密钥文件；未设则默认 {@code ${FORGEX_LICENSE_DIR}/kms.key}</li>
 * </ol>
 * 主密钥不再落库，缺失时启动直接失败并输出生成指引，杜绝主密钥与业务密钥同库存储。
 * <p>
 * 所有密钥操作均记录到审计日志表 {@code sys_kms_key_log}。
 *
 * @author Forgex Team
 * @version 1.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KmsServiceImpl implements KmsService {

    /** 主密钥 hex 环境变量名 */
    private static final String ENV_MASTER_KEY_HEX = "FORGEX_KMS_MASTER_KEY_HEX";
    /** 主密钥文件路径环境变量名 */
    private static final String ENV_MASTER_KEY_FILE = "FORGEX_KMS_MASTER_KEY_FILE";
    /** 密钥文件默认目录环境变量名 */
    private static final String ENV_LICENSE_DIR = "FORGEX_LICENSE_DIR";
    /** 默认密钥文件名 */
    private static final String DEFAULT_KEY_FILE_NAME = "kms.key";
    private static final int MASTER_KEY_SIZE = 32; // 256 bits
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SysKmsKeyMapper kmsKeyMapper;
    private final SysKmsKeyLogMapper kmsKeyLogMapper;

    /** 主密钥缓存，首次加载后复用，避免每次加解密重复读取文件 */
    private volatile byte[] cachedMasterKey;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateKey(String alias, String keyType, int keySize, String description) {
        log.info("KMS: 生成密钥 alias={}, type={}, size={}", alias, keyType, keySize);
        try {
            // 生成原始密钥
            String rawKeyBase64 = generateRawKey(keyType, keySize);

            // 使用主密钥加密
            String encryptedValue = encryptWithMasterKey(rawKeyBase64);

            // 计算版本号
            int version = getMaxVersion(alias) + 1;

            // 保存到数据库
            SysKmsKey key = new SysKmsKey();
            key.setKeyAlias(alias);
            key.setKeyType(keyType.toUpperCase());
            key.setKeySize(keySize);
            key.setEncryptedKeyValue(encryptedValue);
            key.setKeyVersion(version);
            key.setStatus("ACTIVE");
            key.setDescription(description);
            kmsKeyMapper.insert(key);

            // 记录审计日志
            writeLog(key.getId(), alias, "CREATE", "SUCCESS", null);

            log.info("KMS: 密钥生成成功 id={}, alias={}, version={}", key.getId(), alias, version);
            return key.getId();
        } catch (Exception e) {
            writeLog(null, alias, "CREATE", "FAIL", e.getMessage());
            throw new IllegalStateException("KMS: 密钥生成失败", e);
        }
    }

    @Override
    public String getActiveKey(String alias) {
        try {
            SysKmsKey key = kmsKeyMapper.selectOne(new LambdaQueryWrapper<SysKmsKey>()
                    .eq(SysKmsKey::getKeyAlias, alias)
                    .eq(SysKmsKey::getStatus, "ACTIVE")
                    .eq(SysKmsKey::getDeleted, false)
                    .orderByDesc(SysKmsKey::getKeyVersion)
                    .last("LIMIT 1"));

            if (key == null) {
                return null;
            }

            String rawKey = decryptWithMasterKey(key.getEncryptedKeyValue());
            writeLog(key.getId(), alias, "RETRIEVE", "SUCCESS", null);
            return rawKey;
        } catch (Exception e) {
            writeLog(null, alias, "RETRIEVE", "FAIL", e.getMessage());
            throw new IllegalStateException("KMS: 密钥检索失败", e);
        }
    }

    @Override
    public String getKey(String alias, int version) {
        try {
            SysKmsKey key = kmsKeyMapper.selectOne(new LambdaQueryWrapper<SysKmsKey>()
                    .eq(SysKmsKey::getKeyAlias, alias)
                    .eq(SysKmsKey::getKeyVersion, version)
                    .eq(SysKmsKey::getDeleted, false)
                    .last("LIMIT 1"));

            if (key == null) {
                return null;
            }

            String rawKey = decryptWithMasterKey(key.getEncryptedKeyValue());
            writeLog(key.getId(), alias, "RETRIEVE", "SUCCESS", null);
            return rawKey;
        } catch (Exception e) {
            writeLog(null, alias, "RETRIEVE", "FAIL", e.getMessage());
            throw new IllegalStateException("KMS: 密钥检索失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rotateKey(String alias) {
        log.info("KMS: 轮换密钥 alias={}", alias);
        try {
            // 查找当前激活密钥
            SysKmsKey current = kmsKeyMapper.selectOne(new LambdaQueryWrapper<SysKmsKey>()
                    .eq(SysKmsKey::getKeyAlias, alias)
                    .eq(SysKmsKey::getStatus, "ACTIVE")
                    .eq(SysKmsKey::getDeleted, false)
                    .orderByDesc(SysKmsKey::getKeyVersion)
                    .last("LIMIT 1"));

            if (current == null) {
                throw new IllegalArgumentException("KMS: 未找到激活密钥 alias=" + alias);
            }

            // 将旧密钥标记为 ROTATED
            kmsKeyMapper.update(null, new LambdaUpdateWrapper<SysKmsKey>()
                    .eq(SysKmsKey::getId, current.getId())
                    .set(SysKmsKey::getStatus, "ROTATED"));

            writeLog(current.getId(), alias, "ROTATE", "SUCCESS", null);

            // 生成新版本密钥
            return generateKey(alias, current.getKeyType(), current.getKeySize(), current.getDescription());
        } catch (Exception e) {
            writeLog(null, alias, "ROTATE", "FAIL", e.getMessage());
            throw new IllegalStateException("KMS: 密钥轮换失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableKey(String alias) {
        log.info("KMS: 禁用密钥 alias={}", alias);
        try {
            int rows = kmsKeyMapper.update(null, new LambdaUpdateWrapper<SysKmsKey>()
                    .eq(SysKmsKey::getKeyAlias, alias)
                    .eq(SysKmsKey::getStatus, "ACTIVE")
                    .set(SysKmsKey::getStatus, "DISABLED"));

            writeLog(null, alias, "DISABLE", rows > 0 ? "SUCCESS" : "FAIL",
                    rows > 0 ? null : "No active key found");
            return rows > 0;
        } catch (Exception e) {
            writeLog(null, alias, "DISABLE", "FAIL", e.getMessage());
            throw new IllegalStateException("KMS: 密钥禁用失败", e);
        }
    }

    @Override
    public int getMaxVersion(String alias) {
        SysKmsKey key = kmsKeyMapper.selectOne(new LambdaQueryWrapper<SysKmsKey>()
                .eq(SysKmsKey::getKeyAlias, alias)
                .eq(SysKmsKey::getDeleted, false)
                .orderByDesc(SysKmsKey::getKeyVersion)
                .last("LIMIT 1"));
        return key == null ? 0 : key.getKeyVersion();
    }

    // ======================== 主密钥管理 ========================

    /**
     * 获取主密钥（从外部环境变量或密钥文件读取，不再落库）。
     * <p>
     * 读取优先级：
     * <ol>
     *   <li>环境变量 {@code FORGEX_KMS_MASTER_KEY_HEX}（hex 字符串，解码为 32 字节）</li>
     *   <li>环境变量 {@code FORGEX_KMS_MASTER_KEY_FILE} 指向的密钥文件（读取 hex 内容）；
     *       若未设该变量则默认尝试 {@code ${FORGEX_LICENSE_DIR}/kms.key}</li>
     * </ol>
     * 主密钥缺失或长度不符时抛 {@link IllegalStateException}，启动直接失败。
     * <p>
     * 加载后缓存到 {@link #cachedMasterKey}，避免每次加解密重复读取文件。
     *
     * @return 32 字节主密钥
     */
    private byte[] getMasterKey() {
        byte[] cached = cachedMasterKey;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (cachedMasterKey != null) {
                return cachedMasterKey;
            }
            byte[] key = loadMasterKeyFromExternal();
            cachedMasterKey = key;
            return key;
        }
    }

    /**
     * 按优先级从外部加载主密钥，校验长度后返回。
     *
     * @return 32 字节主密钥
     * @throws IllegalStateException 主密钥缺失或长度不符
     */
    private byte[] loadMasterKeyFromExternal() {
        String keyHex = resolveMasterKeyHex();
        byte[] key;
        try {
            key = HexUtil.decodeHex(keyHex);
        } catch (Exception e) {
            throw new IllegalStateException(buildMissingKeyMessage("主密钥 hex 解码失败: " + e.getMessage()), e);
        }
        if (key.length != MASTER_KEY_SIZE) {
            throw new IllegalStateException(buildMissingKeyMessage(
                    "主密钥长度必须为 " + MASTER_KEY_SIZE + " 字节(256 位), 当前为 " + key.length + " 字节"));
        }
        log.info("KMS: 主密钥已加载(来源: {})", describeKeySource());
        return key;
    }

    /**
     * 按优先级解析主密钥 hex 字符串。
     * <p>
     * 优先级 1: 环境变量 {@code FORGEX_KMS_MASTER_KEY_HEX}。
     * 优先级 2: {@code FORGEX_KMS_MASTER_KEY_FILE} 指向的文件; 未设则默认 {@code ${FORGEX_LICENSE_DIR}/kms.key}。
     *
     * @return 主密钥 hex 字符串
     * @throws IllegalStateException 所有来源均未提供主密钥
     */
    private String resolveMasterKeyHex() {
        // 优先级 1: 环境变量 FORGEX_KMS_MASTER_KEY_HEX
        String hex = System.getenv(ENV_MASTER_KEY_HEX);
        if (hex != null && !hex.isEmpty()) {
            return hex.trim();
        }

        // 优先级 2: 密钥文件
        String filePath = System.getenv(ENV_MASTER_KEY_FILE);
        if (filePath == null || filePath.isEmpty()) {
            // 默认尝试 ${FORGEX_LICENSE_DIR}/kms.key
            String licenseDir = System.getenv(ENV_LICENSE_DIR);
            if (licenseDir != null && !licenseDir.isEmpty()) {
                filePath = Paths.get(licenseDir, DEFAULT_KEY_FILE_NAME).toString();
            }
        }

        if (filePath != null && !filePath.isEmpty()) {
            Path path = Paths.get(filePath);
            if (Files.isReadable(path)) {
                try {
                    String content = Files.readString(path, StandardCharsets.UTF_8).trim();
                    if (!content.isEmpty()) {
                        return content;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(buildMissingKeyMessage(
                            "读取主密钥文件失败: " + filePath + ", 原因: " + e.getMessage()), e);
                }
            }
        }

        throw new IllegalStateException(buildMissingKeyMessage(null));
    }

    /**
     * 描述当前主密钥来源(仅日志展示, 不泄露密钥内容)。
     *
     * @return 来源描述
     */
    private String describeKeySource() {
        String hex = System.getenv(ENV_MASTER_KEY_HEX);
        if (hex != null && !hex.isEmpty()) {
            return "环境变量 " + ENV_MASTER_KEY_HEX;
        }
        String filePath = System.getenv(ENV_MASTER_KEY_FILE);
        if (filePath != null && !filePath.isEmpty()) {
            return "文件 " + filePath;
        }
        String licenseDir = System.getenv(ENV_LICENSE_DIR);
        if (licenseDir != null && !licenseDir.isEmpty()) {
            return "文件 " + Paths.get(licenseDir, DEFAULT_KEY_FILE_NAME);
        }
        return "未知";
    }

    /**
     * 构建主密钥缺失的错误消息, 包含生成与配置指引。
     *
     * @param detail 具体错误细节, 可为 null
     * @return 完整错误消息
     */
    private String buildMissingKeyMessage(String detail) {
        StringBuilder sb = new StringBuilder();
        sb.append("KMS: 主密钥未配置, 服务无法启动。");
        if (detail != null && !detail.isEmpty()) {
            sb.append(" 原因: ").append(detail).append("。");
        }
        sb.append("\n请按以下步骤配置主密钥:");
        sb.append("\n  1. 使用密钥生成工具生成 32 字节主密钥:");
        sb.append("\n     bash gen-kms-master-key.sh -o ${FORGEX_LICENSE_DIR}/kms.key");
        sb.append("\n  2. 方式一(文件, 推荐): 将密钥写入文件并设置环境变量:");
        sb.append("\n     export FORGEX_KMS_MASTER_KEY_FILE=${FORGEX_LICENSE_DIR}/kms.key");
        sb.append("\n     (默认即读取 ${FORGEX_LICENSE_DIR}/kms.key, 可不设该变量)");
        sb.append("\n  3. 方式二(环境变量, 适用容器/K8s Secret):");
        sb.append("\n     export FORGEX_KMS_MASTER_KEY_HEX=<64位hex字符串>");
        sb.append("\n  4. 主密钥必须为 32 字节(64 位 hex 字符), 详见 KMS 主密钥管理文档。");
        return sb.toString();
    }

    /**
     * 使用主密钥加密原始密钥值。
     */
    private String encryptWithMasterKey(String plainBase64) throws Exception {
        byte[] masterKey = getMasterKey();
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(masterKey, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plainBase64.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // IV + Ciphertext -> Base64
        byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
        System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 使用主密钥解密密钥值。
     */
    private String decryptWithMasterKey(String encryptedBase64) throws Exception {
        byte[] masterKey = getMasterKey();
        byte[] data = Base64.getDecoder().decode(encryptedBase64);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);
        byte[] encrypted = new byte[data.length - GCM_IV_LENGTH];
        System.arraycopy(data, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKey, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
    }

    // ======================== 密钥生成 ========================

    /**
     * 根据类型和长度生成原始密钥。
     */
    private String generateRawKey(String keyType, int keySize) {
        String type = keyType.toUpperCase();
        switch (type) {
            case "AES": {
                int bytes = keySize / 8;
                byte[] key = new byte[bytes];
                new SecureRandom().nextBytes(key);
                return Base64.getEncoder().encodeToString(key);
            }
            case "SM4": {
                byte[] key = new byte[16]; // SM4 固定 128 位
                new SecureRandom().nextBytes(key);
                return Base64.getEncoder().encodeToString(key);
            }
            case "RSA": {
                String[] keys = RSAPasswordProvider.generateKeyPair(keySize);
                // 存储格式：publicKey::privateKey
                return keys[0] + "::" + keys[1];
            }
            case "SM2": {
                cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2();
                return sm2.getPublicKeyBase64() + "::" + sm2.getPrivateKeyBase64();
            }
            default:
                throw new IllegalArgumentException("Unsupported key type: " + keyType);
        }
    }

    // ======================== 审计日志 ========================

    /**
     * 写入 KMS 审计日志。
     */
    private void writeLog(Long keyId, String alias, String action, String result, String errorMessage) {
        try {
            SysKmsKeyLog logEntry = new SysKmsKeyLog();
            logEntry.setKeyId(keyId);
            logEntry.setKeyAlias(alias);
            logEntry.setAction(action);
            logEntry.setResult(result);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setOperateTime(LocalDateTime.now());
            kmsKeyLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("KMS: 审计日志写入失败 action={}, alias={}", action, alias, e);
        }
    }
}

