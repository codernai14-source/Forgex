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
package com.forgex.common.crypto;

import com.forgex.common.config.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis 字段透明加密拦截器。
 * <p>
 * 拦截 {@code Executor.update}（INSERT/UPDATE）和 {@code Executor.query}（SELECT），
 * 对标注了 {@link FieldEncrypt} 注解的 String 字段自动执行加密/解密操作。
 * <p>
 * 工作原理：
 * <ul>
 *   <li><b>写入（INSERT/UPDATE）</b>：在 SQL 执行前，扫描参数对象中带 {@code @FieldEncrypt} 的字段，加密其值</li>
 *   <li><b>查询（SELECT）</b>：在结果返回后，扫描结果对象中带 {@code @FieldEncrypt} 的字段，解密其值</li>
 * </ul>
 * <p>
 * 加密/解密委托给 {@link CryptoProviders} 解析对应算法的 {@link CryptoPasswordProvider}。
 * <p>
 * <b>注意事项</b>：
 * <ul>
 *   <li>仅处理 String 类型字段</li>
 *   <li>已加密的数据不会重复加密（通过前缀检测）</li>
 *   <li>字段缓存确保反射开销最小化</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FieldEncrypt
 * @see CryptoProviders
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class FieldEncryptInterceptor implements Interceptor {

    /** 加密字段前缀，用于标识已加密的值，避免重复加密 */
    private static final String ENCRYPT_PREFIX = "ENC:";

    /** 字段缓存：Class -> 带 @FieldEncrypt 注解的字段列表 */
    private static final Map<Class<?>, List<EncryptFieldMeta>> FIELD_CACHE = new ConcurrentHashMap<>();

    private final ConfigService configService;

    public FieldEncryptInterceptor(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        SqlCommandType commandType = ms.getSqlCommandType();

        if (commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE) {
            // 写入操作：加密参数
            Object parameter = args[1];
            if (parameter != null) {
                encryptFields(parameter);
            }
            return invocation.proceed();
        } else if (commandType == SqlCommandType.SELECT) {
            // 查询操作：先执行查询，再解密结果
            Object result = invocation.proceed();
            if (result instanceof List) {
                for (Object item : (List<?>) result) {
                    if (item != null) {
                        decryptFields(item);
                    }
                }
            }
            return result;
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // no-op
    }

    /**
     * 加密对象中标注了 @FieldEncrypt 的字段。
     */
    private void encryptFields(Object obj) {
        if (obj == null) return;

        // 处理 MyBatis-Plus 的 Map 参数包装
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Object value : map.values()) {
                if (value != null && !isPrimitive(value.getClass())) {
                    encryptFields(value);
                }
            }
            return;
        }

        List<EncryptFieldMeta> fields = getEncryptFields(obj.getClass());
        if (fields.isEmpty()) return;

        for (EncryptFieldMeta meta : fields) {
            try {
                String value = (String) meta.field.get(obj);
                if (value == null || value.isEmpty() || value.startsWith(ENCRYPT_PREFIX)) {
                    continue; // 跳过空值或已加密值
                }
                CryptoPasswordProvider provider = CryptoProviders.resolve(meta.algorithm, configService);
                String encrypted = ENCRYPT_PREFIX + provider.encrypt(value);
                meta.field.set(obj, encrypted);
            } catch (Exception e) {
                log.warn("字段加密失败: class={}, field={}", obj.getClass().getSimpleName(), meta.field.getName(), e);
            }
        }
    }

    /**
     * 解密对象中标注了 @FieldEncrypt 的字段。
     */
    private void decryptFields(Object obj) {
        if (obj == null) return;

        List<EncryptFieldMeta> fields = getEncryptFields(obj.getClass());
        if (fields.isEmpty()) return;

        for (EncryptFieldMeta meta : fields) {
            try {
                String value = (String) meta.field.get(obj);
                if (value == null || !value.startsWith(ENCRYPT_PREFIX)) {
                    continue; // 跳过非加密值
                }
                String cipherText = value.substring(ENCRYPT_PREFIX.length());
                CryptoPasswordProvider provider = CryptoProviders.resolve(meta.algorithm, configService);
                String decrypted = provider.decrypt(cipherText);
                meta.field.set(obj, decrypted);
            } catch (Exception e) {
                log.warn("字段解密失败: class={}, field={}", obj.getClass().getSimpleName(), meta.field.getName(), e);
            }
        }
    }

    /**
     * 获取类中带 @FieldEncrypt 注解的字段列表（带缓存）。
     */
    private List<EncryptFieldMeta> getEncryptFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, cls -> {
            List<EncryptFieldMeta> result = new ArrayList<>();
            Class<?> current = cls;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    FieldEncrypt annotation = field.getAnnotation(FieldEncrypt.class);
                    if (annotation != null && field.getType() == String.class) {
                        field.setAccessible(true);
                        result.add(new EncryptFieldMeta(field, annotation.algorithm().toLowerCase()));
                    }
                }
                current = current.getSuperclass();
            }
            return result;
        });
    }

    /**
     * 判断是否为基本类型或包装类型。
     */
    private boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class;
    }

    /**
     * 加密字段元数据。
     */
    private static class EncryptFieldMeta {
        final Field field;
        final String algorithm;

        EncryptFieldMeta(Field field, String algorithm) {
            this.field = field;
            this.algorithm = algorithm;
        }
    }
}

