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
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis 字段透明加密拦截器。
 * <p>
 * 拦截 {@code Executor.update}（INSERT/UPDATE）和 {@code Executor.query}（SELECT），
 * 对标注了 {@link FieldEncrypt} 注解的 String 字段自动执行加密/解密操作。
 * </p>
 * <p>
 * 不得在每次 SQL 上都去读 {@code sys_config}：该表在 {@code forgex_common}，
 * 而业务 SQL 常在 {@code admin} 事务里执行。事务内嵌套查询无法切换数据源，
 * 会变成 {@code Table 'forgex_admin.sys_config' doesn't exist}，打开首页上报菜单访问即会失败。
 * 无加密字段的语句直接放行；开关读取失败时使用默认开启，不阻断业务 SQL。
 * </p>
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

    /** 字段加密开关配置键，对应 {@code forgex_common.sys_config} */
    static final String FIELD_ENCRYPT_ENABLED_KEY = "security.crypto.field-encrypt.enabled";

    /** 开关缓存时长，避免每个 SQL 都打配置库 */
    private static final long ENABLED_CACHE_TTL_MS = 60_000L;

    /** 字段缓存：Class -> 带 @FieldEncrypt 注解的字段列表 */
    private static final Map<Class<?>, List<EncryptFieldMeta>> FIELD_CACHE = new ConcurrentHashMap<>();

    /** 嵌套 SQL 标记，避免拦截器读配置时再次进入自身 */
    private static final ThreadLocal<Boolean> REENTRANT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final ConfigService configService;

    private volatile Boolean cachedEnabled;

    private volatile long cachedEnabledAt;

    /**
     * 构造字段加密拦截器。
     *
     * @param configService 配置服务，用于读取字段加密开关
     */
    public FieldEncryptInterceptor(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (Boolean.TRUE.equals(REENTRANT.get())) {
            return invocation.proceed();
        }

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        if (isConfigStatement(ms)) {
            return invocation.proceed();
        }

        SqlCommandType commandType = ms.getSqlCommandType();
        if (commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE) {
            if (!parameterHasEncryptFields(args[1])) {
                return invocation.proceed();
            }
        } else if (commandType == SqlCommandType.SELECT) {
            if (!resultTypeHasEncryptFields(ms)) {
                return invocation.proceed();
            }
        } else {
            return invocation.proceed();
        }

        if (!isFieldEncryptEnabled()) {
            return invocation.proceed();
        }

        REENTRANT.set(Boolean.TRUE);
        try {
            if (commandType == SqlCommandType.INSERT || commandType == SqlCommandType.UPDATE) {
                Object parameter = args[1];
                if (parameter != null) {
                    encryptFields(parameter);
                }
                return invocation.proceed();
            }
            Object result = invocation.proceed();
            if (result instanceof List<?>) {
                for (Object item : (List<?>) result) {
                    if (item != null) {
                        decryptFields(item);
                    }
                }
            }
            return result;
        } finally {
            REENTRANT.remove();
        }
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
     * 判断当前语句是否为配置表访问。
     *
     * @param ms MappedStatement
     * @return 访问 {@code SysConfigMapper} 时返回 {@code true}
     */
    private boolean isConfigStatement(MappedStatement ms) {
        String id = ms.getId();
        return id != null && id.contains("SysConfigMapper");
    }

    /**
     * 读取字段加密开关。
     * <p>
     * 事务中嵌套查询无法切到 {@code common} 库时捕获异常并回落默认值，避免菜单访问上报被拖死。
     * </p>
     *
     * @return 是否启用字段加密，默认 {@code true}
     */
    boolean isFieldEncryptEnabled() {
        long now = System.currentTimeMillis();
        Boolean cached = cachedEnabled;
        if (cached != null && now - cachedEnabledAt < ENABLED_CACHE_TTL_MS) {
            return cached;
        }
        boolean enabled = true;
        REENTRANT.set(Boolean.TRUE);
        try {
            enabled = configService.getBoolean(FIELD_ENCRYPT_ENABLED_KEY, true);
            cachedEnabled = enabled;
            cachedEnabledAt = now;
        } catch (RuntimeException ex) {
            log.warn("读取字段加密开关失败，使用默认开启。当前语句可能处于非 common 数据源事务中: {}", ex.getMessage());
            cachedEnabled = Boolean.TRUE;
            cachedEnabledAt = now;
        } finally {
            REENTRANT.remove();
        }
        return enabled;
    }

    /**
     * 判断写入参数是否包含需加密字段。
     *
     * @param parameter MyBatis 参数，可能是实体或 MyBatis-Plus 的 Map 包装
     * @return 存在 {@link FieldEncrypt} 字段时返回 {@code true}
     */
    boolean parameterHasEncryptFields(Object parameter) {
        if (parameter == null) {
            return false;
        }
        if (parameter instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                if (value != null && !isPrimitive(value.getClass()) && parameterHasEncryptFields(value)) {
                    return true;
                }
            }
            return false;
        }
        return !getEncryptFields(parameter.getClass()).isEmpty();
    }

    /**
     * 判断查询结果类型是否包含需解密字段。
     *
     * @param ms MappedStatement
     * @return 结果类型带 {@link FieldEncrypt} 时返回 {@code true}
     */
    private boolean resultTypeHasEncryptFields(MappedStatement ms) {
        List<ResultMap> resultMaps = ms.getResultMaps();
        if (resultMaps == null || resultMaps.isEmpty()) {
            return false;
        }
        for (ResultMap resultMap : resultMaps) {
            Class<?> type = resultMap.getType();
            if (type != null && !isPrimitive(type) && !getEncryptFields(type).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 加密对象中标注了 @FieldEncrypt 的字段。
     *
     * @param obj 待加密对象
     */
    private void encryptFields(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                if (value != null && !isPrimitive(value.getClass())) {
                    encryptFields(value);
                }
            }
            return;
        }

        List<EncryptFieldMeta> fields = getEncryptFields(obj.getClass());
        if (fields.isEmpty()) {
            return;
        }

        for (EncryptFieldMeta meta : fields) {
            try {
                String value = (String) meta.field.get(obj);
                if (value == null || value.isEmpty() || value.startsWith(ENCRYPT_PREFIX)) {
                    continue;
                }
                CryptoPasswordProvider provider = CryptoProviders.resolve(meta.algorithm, configService);
                String encrypted = ENCRYPT_PREFIX + provider.encrypt(value);
                meta.field.set(obj, encrypted);
            } catch (Exception e) {
                log.error("字段加密失败: class={}, field={}", obj.getClass().getSimpleName(), meta.field.getName(), e);
            }
        }
    }

    /**
     * 解密对象中标注了 @FieldEncrypt 的字段。
     *
     * @param obj 待解密对象
     */
    private void decryptFields(Object obj) {
        if (obj == null) {
            return;
        }

        List<EncryptFieldMeta> fields = getEncryptFields(obj.getClass());
        if (fields.isEmpty()) {
            return;
        }

        for (EncryptFieldMeta meta : fields) {
            try {
                String value = (String) meta.field.get(obj);
                if (value == null || !value.startsWith(ENCRYPT_PREFIX)) {
                    continue;
                }
                String cipherText = value.substring(ENCRYPT_PREFIX.length());
                CryptoPasswordProvider provider = CryptoProviders.resolve(meta.algorithm, configService);
                String decrypted = provider.decrypt(cipherText);
                meta.field.set(obj, decrypted);
            } catch (Exception e) {
                log.error("字段解密失败: class={}, field={}", obj.getClass().getSimpleName(), meta.field.getName(), e);
            }
        }
    }

    /**
     * 获取类中带 @FieldEncrypt 注解的字段列表（带缓存）。
     *
     * @param clazz 实体类型
     * @return 加密字段元数据，无注解时为空列表
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
     *
     * @param clazz 待判断类型
     * @return 基本类型或常见包装类型时返回 {@code true}
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
