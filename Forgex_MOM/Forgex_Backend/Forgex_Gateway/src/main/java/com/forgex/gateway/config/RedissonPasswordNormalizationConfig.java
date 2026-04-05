package com.forgex.gateway.config;

import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Redisson 配置归一化。
 * <p>
 * 当前环境的 Redis 未开启密码时，Nacos 中的 password 可能会以空字符串注入。
 * Spring Data Redis 会把它当作未配置处理，而 Redisson 会继续发送 AUTH，导致网关启动失败。
 * 这里仅将“空白密码”归一化为 null，不吞掉真实的错误密码配置。
 * </p>
 */
@Configuration(proxyBeanMethods = false)
public class RedissonPasswordNormalizationConfig {

    @Bean
    public RedissonAutoConfigurationCustomizer redissonPasswordNormalizer() {
        return config -> {
            normalizePassword(config, "getSingleServerConfig", "getPassword", "setPassword");
            normalizePassword(config, "getClusterServersConfig", "getPassword", "setPassword");
            normalizePassword(config, "getMasterSlaveServersConfig", "getPassword", "setPassword");
            normalizePassword(config, "getReplicatedServersConfig", "getPassword", "setPassword");
            normalizePassword(config, "getSentinelServersConfig", "getPassword", "setPassword");
            normalizePassword(config, "getSentinelServersConfig", "getSentinelPassword", "setSentinelPassword");
        };
    }

    private void normalizePassword(Config config, String configGetter, String passwordGetter, String passwordSetter) {
        Object serverConfig = invoke(config, Config.class, configGetter);
        if (serverConfig == null) {
            return;
        }
        Object password = invoke(serverConfig, serverConfig.getClass(), passwordGetter);
        if (password instanceof String text && !StringUtils.hasText(text)) {
            invoke(serverConfig, serverConfig.getClass(), passwordSetter, String.class, null);
        }
    }

    private Object invoke(Object target, Class<?> type, String methodName, Class<?>... parameterTypes) {
        if (target == null) {
            return null;
        }
        Method method = ReflectionUtils.findMethod(type, methodName, parameterTypes);
        if (method == null) {
            return null;
        }
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, target);
    }

    private void invoke(Object target, Class<?> type, String methodName, Class<?> parameterType, Object arg) {
        if (target == null) {
            return;
        }
        Method method = ReflectionUtils.findMethod(type, methodName, parameterType);
        if (method == null) {
            return;
        }
        ReflectionUtils.makeAccessible(method);
        ReflectionUtils.invokeMethod(method, target, arg);
    }
}
