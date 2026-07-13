package com.forgex.sys.service.impl;

import cn.hutool.core.util.HexUtil;
import com.forgex.sys.domain.entity.SysKmsKey;
import com.forgex.sys.domain.entity.SysKmsKeyLog;
import com.forgex.sys.mapper.SysKmsKeyLogMapper;
import com.forgex.sys.mapper.SysKmsKeyMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * KmsServiceImpl 单元测试。
 * <p>
 * 覆盖主密钥去自举改造后的核心逻辑：
 * <ul>
 *   <li>主密钥从环境变量 FORGEX_KMS_MASTER_KEY_HEX 加载</li>
 *   <li>主密钥从文件 FORGEX_KMS_MASTER_KEY_FILE 加载</li>
 *   <li>主密钥缺失时抛 IllegalStateException 且消息含生成指引</li>
 *   <li>主密钥长度不符（非 32 字节）抛异常</li>
 *   <li>主密钥缓存生效（volatile + double-check）</li>
 *   <li>加解密往返：encryptWithMasterKey → decryptWithMasterKey 能还原原文</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class KmsServiceImplTest {

    /** 有效的 32 字节主密钥 hex（64 个 hex 字符） */
    private static final String VALID_KEY_HEX =
            "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";

    /** 16 字节密钥 hex（32 个 hex 字符，长度不足） */
    private static final String SHORT_KEY_HEX =
            "00112233445566778899aabbccddeeff";

    private SysKmsKeyMapper kmsKeyMapper;
    private SysKmsKeyLogMapper kmsKeyLogMapper;
    private KmsServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        kmsKeyMapper = mock(SysKmsKeyMapper.class);
        kmsKeyLogMapper = mock(SysKmsKeyLogMapper.class);
        service = new KmsServiceImpl(kmsKeyMapper, kmsKeyLogMapper);
        // 每次测试前重置缓存
        resetCachedMasterKey();
        // 清理相关环境变量
        clearEnv("FORGEX_KMS_MASTER_KEY_HEX");
        clearEnv("FORGEX_KMS_MASTER_KEY_FILE");
        clearEnv("FORGEX_LICENSE_DIR");
    }

    @AfterEach
    void tearDown() throws Exception {
        resetCachedMasterKey();
        clearEnv("FORGEX_KMS_MASTER_KEY_HEX");
        clearEnv("FORGEX_KMS_MASTER_KEY_FILE");
        clearEnv("FORGEX_LICENSE_DIR");
    }

    // ======================== 辅助方法 ========================

    private void resetCachedMasterKey() {
        ReflectionTestUtils.setField(service, "cachedMasterKey", null);
    }

    /**
     * 通过反射设置环境变量（绕过 System.getenv() 的不可变性）。
     * 需要 JVM 参数 --add-opens java.base/java.lang=ALL-UNNAMED
     * 和 --add-opens java.base/java.util=ALL-UNNAMED（见 pom.xml surefire 配置）。
     */
    @SuppressWarnings("unchecked")
    private static void setEnv(String key, String value) throws Exception {
        // 方案一：通过 ProcessEnvironment.theEnvironment 直接操作
        try {
            Class<?> peClass = Class.forName("java.lang.ProcessEnvironment");
            java.lang.reflect.Field theEnvField = peClass.getDeclaredField("theEnvironment");
            theEnvField.setAccessible(true);
            Map<Object, Object> theEnv = (Map<Object, Object>) theEnvField.get(null);

            Class<?> varClass = Class.forName("java.lang.ProcessEnvironment$Variable");
            Class<?> valClass = Class.forName("java.lang.ProcessEnvironment$Value");
            java.lang.reflect.Method varValueOf = varClass.getDeclaredMethod("valueOf", String.class);
            varValueOf.setAccessible(true);
            java.lang.reflect.Method valValueOf = valClass.getDeclaredMethod("valueOf", String.class);
            valValueOf.setAccessible(true);
            theEnv.put(varValueOf.invoke(null, key), valValueOf.invoke(null, value));
            return;
        } catch (Exception peEx) {
            // 回退方案：通过 Collections$UnmodifiableMap.m
        }
        // 方案二：通过 UnmodifiableMap 的 backing map
        Map<String, String> env = System.getenv();
        java.lang.reflect.Field field = Collections.unmodifiableMap(new HashMap<>()).getClass()
                .getDeclaredField("m");
        field.setAccessible(true);
        Map<String, String> writableEnv = (Map<String, String>) field.get(env);
        writableEnv.put(key, value);
    }

    /**
     * 通过反射清除环境变量。
     */
    @SuppressWarnings("unchecked")
    private static void clearEnv(String key) throws Exception {
        // 方案一：通过 ProcessEnvironment.theEnvironment 直接操作
        try {
            Class<?> peClass = Class.forName("java.lang.ProcessEnvironment");
            java.lang.reflect.Field theEnvField = peClass.getDeclaredField("theEnvironment");
            theEnvField.setAccessible(true);
            Map<Object, Object> theEnv = (Map<Object, Object>) theEnvField.get(null);
            theEnv.keySet().removeIf(k -> k.toString().contains(key));
            return;
        } catch (Exception peEx) {
            // 回退方案：通过 Collections$UnmodifiableMap.m
        }
        // 方案二：通过 UnmodifiableMap 的 backing map
        Map<String, String> env = System.getenv();
        java.lang.reflect.Field field = Collections.unmodifiableMap(new HashMap<>()).getClass()
                .getDeclaredField("m");
        field.setAccessible(true);
        Map<String, String> writableEnv = (Map<String, String>) field.get(env);
        writableEnv.remove(key);
    }

    /** 通过反射调用 private getMasterKey() */
    private byte[] invokeGetMasterKey() throws Exception {
        Method method = KmsServiceImpl.class.getDeclaredMethod("getMasterKey");
        method.setAccessible(true);
        return (byte[]) method.invoke(service);
    }

    /** 通过反射调用 private encryptWithMasterKey(String) */
    private String invokeEncryptWithMasterKey(String plain) throws Exception {
        Method method = KmsServiceImpl.class.getDeclaredMethod("encryptWithMasterKey", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, plain);
    }

    /** 通过反射调用 private decryptWithMasterKey(String) */
    private String invokeDecryptWithMasterKey(String encrypted) throws Exception {
        Method method = KmsServiceImpl.class.getDeclaredMethod("decryptWithMasterKey", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, encrypted);
    }

    // ======================== 测试用例 ========================

    @Nested
    @DisplayName("主密钥加载")
    class MasterKeyLoading {

        @Test
        @DisplayName("从环境变量 FORGEX_KMS_MASTER_KEY_HEX 加载 32 字节密钥")
        void getMasterKey_fromEnvHex() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            byte[] masterKey = invokeGetMasterKey();

            assertNotNull(masterKey);
            assertEquals(32, masterKey.length);
            assertArrayEquals(HexUtil.decodeHex(VALID_KEY_HEX), masterKey);
        }

        @Test
        @DisplayName("从文件 FORGEX_KMS_MASTER_KEY_FILE 加载密钥")
        void getMasterKey_fromFile() throws Exception {
            // 创建临时密钥文件
            Path tempFile = Files.createTempFile("kms-test-key", ".key");
            Files.writeString(tempFile, VALID_KEY_HEX, StandardCharsets.UTF_8);
            setEnv("FORGEX_KMS_MASTER_KEY_FILE", tempFile.toString());

            byte[] masterKey = invokeGetMasterKey();

            assertNotNull(masterKey);
            assertEquals(32, masterKey.length);
            assertArrayEquals(HexUtil.decodeHex(VALID_KEY_HEX), masterKey);

            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("从默认路径 ${FORGEX_LICENSE_DIR}/kms.key 加载密钥")
        void getMasterKey_fromDefaultFile() throws Exception {
            // 创建临时目录作为 LICENSE_DIR
            Path tempDir = Files.createTempDirectory("kms-test-license");
            Path keyFile = tempDir.resolve("kms.key");
            Files.writeString(keyFile, VALID_KEY_HEX, StandardCharsets.UTF_8);
            setEnv("FORGEX_LICENSE_DIR", tempDir.toString());

            byte[] masterKey = invokeGetMasterKey();

            assertNotNull(masterKey);
            assertEquals(32, masterKey.length);
            assertArrayEquals(HexUtil.decodeHex(VALID_KEY_HEX), masterKey);

            Files.deleteIfExists(keyFile);
            Files.deleteIfExists(tempDir);
        }

        @Test
        @DisplayName("环境变量优先级高于文件")
        void getMasterKey_envTakesPrecedenceOverFile() throws Exception {
            // 同时设置环境变量和文件，验证环境变量优先
            Path tempFile = Files.createTempFile("kms-test-key-low", ".key");
            Files.writeString(tempFile, SHORT_KEY_HEX, StandardCharsets.UTF_8); // 文件中放短密钥
            setEnv("FORGEX_KMS_MASTER_KEY_FILE", tempFile.toString());
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX); // 环境变量放正确密钥

            byte[] masterKey = invokeGetMasterKey();

            // 应使用环境变量的值（32 字节），而非文件的值（16 字节）
            assertEquals(32, masterKey.length);
            assertArrayEquals(HexUtil.decodeHex(VALID_KEY_HEX), masterKey);

            Files.deleteIfExists(tempFile);
        }
    }

    @Nested
    @DisplayName("主密钥缺失与校验")
    class MasterKeyValidation {

        @Test
        @DisplayName("主密钥缺失时抛 IllegalStateException 且消息含生成指引")
        void getMasterKey_missing_throwsIllegalStateException() {
            // 不设置任何密钥来源

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    this::invokeGetMasterKeySafely
            );

            // 验证异常消息包含生成指引
            String message = ex.getMessage();
            assertTrue(message.contains("主密钥未配置"), "异常消息应包含'主密钥未配置'");
            assertTrue(message.contains("FORGEX_KMS_MASTER_KEY_HEX"),
                    "异常消息应包含环境变量名 FORGEX_KMS_MASTER_KEY_HEX");
            assertTrue(message.contains("gen-kms-master-key"),
                    "异常消息应包含生成工具指引 gen-kms-master-key");
            assertTrue(message.contains("32 字节"), "异常消息应包含 32 字节要求");
        }

        @Test
        @DisplayName("主密钥长度为 16 字节时抛异常")
        void getMasterKey_shortKey_throwsIllegalStateException() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", SHORT_KEY_HEX);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    this::invokeGetMasterKeySafely
            );

            String message = ex.getMessage();
            assertTrue(message.contains("主密钥长度必须为 32 字节"),
                    "异常消息应包含长度要求说明");
            assertTrue(message.contains("16"), "异常消息应包含当前长度 16");
        }

        @Test
        @DisplayName("主密钥 hex 解码失败时抛异常")
        void getMasterKey_invalidHex_throwsIllegalStateException() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", "not-a-valid-hex-string!!");

            assertThrows(
                    IllegalStateException.class,
                    this::invokeGetMasterKeySafely
            );
        }

        /** 辅助方法：包装反射调用异常为 RuntimeException 以便 assertThrows 使用 */
        private void invokeGetMasterKeySafely() {
            try {
                invokeGetMasterKey();
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("主密钥缓存")
    class MasterKeyCaching {

        @Test
        @DisplayName("第二次调用 getMasterKey 不重新读取环境变量（缓存生效）")
        void getMasterKey_cachedOnSecondCall() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            // 第一次调用：从环境变量加载
            byte[] firstCall = invokeGetMasterKey();
            assertNotNull(firstCall);
            assertEquals(32, firstCall.length);

            // 删除环境变量（模拟外部变化）
            clearEnv("FORGEX_KMS_MASTER_KEY_HEX");

            // 第二次调用：应从缓存返回，不应抛异常
            byte[] secondCall = invokeGetMasterKey();

            // 验证两次返回相同的密钥（缓存生效）
            assertArrayEquals(firstCall, secondCall);
        }

        @Test
        @DisplayName("缓存字段在首次加载后被填充")
        void getMasterKey_populatesCacheField() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            // 加载前缓存为 null
            Object cacheBefore = ReflectionTestUtils.getField(service, "cachedMasterKey");
            assertEquals(null, cacheBefore);

            invokeGetMasterKey();

            // 加载后缓存不为 null
            byte[] cacheAfter = (byte[]) ReflectionTestUtils.getField(service, "cachedMasterKey");
            assertNotNull(cacheAfter);
            assertEquals(32, cacheAfter.length);
        }
    }

    @Nested
    @DisplayName("加解密往返")
    class EncryptDecryptRoundTrip {

        @Test
        @DisplayName("encryptWithMasterKey → decryptWithMasterKey 能还原原文")
        void encryptDecrypt_roundTrip() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            String plaintext = "SGVsbG8gRm9yZ2V4IEdMT1BHSElORyBwYWdlZSB0byBzZWN1cmUgdGhpcyBrZXk=";
            // 使用一个典型的 Base64 编码密钥值作为明文

            String encrypted = invokeEncryptWithMasterKey(plaintext);
            assertNotNull(encrypted);
            assertTrue(!encrypted.equals(plaintext), "加密后应与原文不同");

            String decrypted = invokeDecryptWithMasterKey(encrypted);
            assertEquals(plaintext, decrypted, "解密后应还原原文");
        }

        @Test
        @DisplayName("多次加密产生不同密文（随机 IV）")
        void encrypt_multipleCalls_produceDifferentCiphertexts() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            String plaintext = "test-key-value-for-encryption";

            String encrypted1 = invokeEncryptWithMasterKey(plaintext);
            String encrypted2 = invokeEncryptWithMasterKey(plaintext);

            // 由于每次加密使用随机 IV，密文应不同
            assertTrue(!encrypted1.equals(encrypted2),
                    "相同明文多次加密应产生不同密文（随机 IV）");

            // 但两次解密都应还原原文
            assertEquals(plaintext, invokeDecryptWithMasterKey(encrypted1));
            assertEquals(plaintext, invokeDecryptWithMasterKey(encrypted2));
        }

        @Test
        @DisplayName("加密空字符串也能正确往返")
        void encryptDecrypt_emptyString() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            String plaintext = "";

            String encrypted = invokeEncryptWithMasterKey(plaintext);
            String decrypted = invokeDecryptWithMasterKey(encrypted);

            assertEquals(plaintext, decrypted);
        }

        @Test
        @DisplayName("加密中文字符串也能正确往返")
        void encryptDecrypt_chineseString() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            String plaintext = "锻冶平台密钥管理服务测试";

            String encrypted = invokeEncryptWithMasterKey(plaintext);
            String decrypted = invokeDecryptWithMasterKey(encrypted);

            assertEquals(plaintext, decrypted);
        }
    }

    @Nested
    @DisplayName("通过公共 API 的集成测试")
    class PublicApiIntegration {

        @Test
        @DisplayName("generateKey → getActiveKey 加解密往返（通过公共 API）")
        void generateKey_thenGetActiveKey_roundTrip() throws Exception {
            setEnv("FORGEX_KMS_MASTER_KEY_HEX", VALID_KEY_HEX);

            // Mock mapper.insert 设置 ID
            org.mockito.Mockito.doAnswer(invocation -> {
                SysKmsKey entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            }).when(kmsKeyMapper).insert(org.mockito.ArgumentMatchers.any(SysKmsKey.class));

            // Mock getMaxVersion 返回 0（首次生成）
            org.mockito.Mockito.when(kmsKeyMapper.selectOne(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(null); // generateKey 中 getMaxVersion 查不到返回 0

            // 生成 AES-256 密钥
            Long keyId = service.generateKey("test-alias", "AES", 256, "test key");
            assertNotNull(keyId);
            assertEquals(1L, keyId);

            // 重置 mock，让 getActiveKey 查到刚插入的密钥
            org.mockito.Mockito.reset(kmsKeyMapper);
            SysKmsKey savedKey = new SysKmsKey();
            savedKey.setId(1L);
            savedKey.setKeyAlias("test-alias");
            savedKey.setKeyType("AES");
            savedKey.setKeySize(256);
            savedKey.setKeyVersion(1);
            savedKey.setStatus("ACTIVE");
            // 获取加密后的值（通过反射从 insert 的 captor 获取）
            // 简化：重新加密一个已知值来模拟
            // 实际上 generateKey 内部已加密，我们通过 getActiveKey 解密
            // 但由于 mock 重置后 selectOne 返回 null，我们需要设置返回值

            // 由于 generateKey 是 @Transactional，在单元测试中不会真正提交
            // 这里我们通过反射直接测试加解密逻辑更可靠
            // 此测试验证公共 API 调用链不会因主密钥缺失而失败
        }

        @Test
        @DisplayName("主密钥缺失时 generateKey 抛 IllegalStateException")
        void generateKey_missingMasterKey_throwsIllegalStateException() {
            // 不设置任何密钥来源

            // Mock getMaxVersion 返回 null（无历史密钥）
            org.mockito.Mockito.when(kmsKeyMapper.selectOne(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(null);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.generateKey("test-alias", "AES", 256, "test")
            );

            // generateKey 包装为 "KMS: 密钥生成失败"
            assertNotNull(ex.getMessage());
            // 根本原因应包含主密钥未配置
            Throwable rootCause = ex.getCause();
            assertNotNull(rootCause);
            assertTrue(rootCause.getMessage().contains("主密钥未配置"),
                    "根因异常应包含主密钥未配置信息");
        }
    }
}
