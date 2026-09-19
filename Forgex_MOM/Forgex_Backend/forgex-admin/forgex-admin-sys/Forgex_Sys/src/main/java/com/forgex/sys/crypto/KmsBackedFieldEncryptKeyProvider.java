package com.forgex.sys.crypto;

import cn.hutool.core.util.HexUtil;
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.FieldEncryptKeyProvider;
import com.forgex.common.crypto.FieldEncryptKeyProviders;
import com.forgex.sys.service.KmsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Map;

/**
 * 将字段加密 SM4 密钥托管到 KMS，并迁移存量配置库密钥。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see KmsService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KmsBackedFieldEncryptKeyProvider implements FieldEncryptKeyProvider {

    /** 字段加密派生密钥别名。 */
    public static final String ALIAS = "field-encrypt-sm4";

    private final KmsService kmsService;
    private final ConfigService configService;

    /**
     * 注册到 Common_Crypto 静态查找表。
     */
    @PostConstruct
    public void register() {
        FieldEncryptKeyProviders.register(this);
    }

    /**
     * 解析 SM4 hex 密钥，优先 KMS，其次迁移配置库存量。
     *
     * @return hex 密钥
     */
    @Override
    public String resolveSm4KeyHex() {
        try {
            String active = kmsService.getActiveKey(ALIAS);
            if (StringUtils.hasText(active)) {
                return toHex(active);
            }
            String legacy = readLegacyHex();
            if (StringUtils.hasText(legacy)) {
                kmsService.importExistingKey(ALIAS, "SM4", 128,
                        Base64.getEncoder().encodeToString(HexUtil.decodeHex(legacy)),
                        "migrated from security.crypto.sm4");
                return legacy;
            }
            kmsService.generateKey(ALIAS, "SM4", 128, "field encrypt derived key");
            String created = kmsService.getActiveKey(ALIAS);
            return StringUtils.hasText(created) ? toHex(created) : null;
        } catch (Exception ex) {
            log.warn("KMS 字段加密密钥不可用，回退配置库: {}", ex.getMessage());
            return readLegacyHex();
        }
    }

    private String readLegacyHex() {
        Map<?, ?> map = configService.getJson("security.crypto.sm4", Map.class, null);
        Object keyHex = map == null ? null : map.get("keyHex");
        return keyHex == null ? null : String.valueOf(keyHex);
    }

    private String toHex(String rawBase64) {
        byte[] raw = Base64.getDecoder().decode(rawBase64);
        return HexUtil.encodeHexStr(raw);
    }
}
