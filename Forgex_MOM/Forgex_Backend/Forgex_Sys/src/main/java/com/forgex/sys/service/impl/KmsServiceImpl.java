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
import com.forgex.common.config.ConfigService;
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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * 密钥管理服务实现（KMS）。
 * <p>
 * 使用主密钥（Master Key）对所有业务密钥进行 AES-256-GCM 加密后存储到数据库。
 * 主密钥从 ConfigService 的 {@code security.kms.master} 配置中读取，首次使用时自动生成。
 * <p>
 * 所有密钥操作均记录到审计日志表 {@code sys_kms_key_log}。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KmsServiceImpl implements KmsService {

    private static final String MASTER_KEY_CONFIG = "security.kms.master";
    private static final int MASTER_KEY_SIZE = 32; // 256 bits
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SysKmsKeyMapper kmsKeyMapper;
    private final SysKmsKeyLogMapper kmsKeyLogMapper;
    private final ConfigService configService;

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
     * 获取主密钥（从配置库读取，不存在则自动生成）。
     */
    private byte[] getMasterKey() {
        Map m = configService.getJson(MASTER_KEY_CONFIG, Map.class, null);
        String keyHex = m == null ? null : (String) m.get("keyHex");
        if (keyHex == null || keyHex.isEmpty()) {
            log.info("KMS: 主密钥不存在，自动生成...");
            byte[] key = new byte[MASTER_KEY_SIZE];
            new SecureRandom().nextBytes(key);
            keyHex = HexUtil.encodeHexStr(key);
            java.util.Map<String, String> cfg = new java.util.HashMap<>();
            cfg.put("keyHex", keyHex);
            configService.setJson(MASTER_KEY_CONFIG, cfg);
            log.info("KMS: 主密钥已生成并保存到配置库");
        }
        return HexUtil.decodeHex(keyHex);
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
            log.warn("KMS: 审计日志写入失败 action={}, alias={}", action, alias, e);
        }
    }
}

