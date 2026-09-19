package com.forgex.common.starter;

import com.forgex.common.config.JacksonConfig;
import com.forgex.common.config.WebMvcConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 独立业务服务的公共自动配置，只导入明确的 Servlet 能力。
 * 平台 API、数据库和平台组件扫描由消费方按需选择。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ForgexCommonAutoConfiguration {

    /** 已扫描旧 MVC 配置的应用继续使用原实例。 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(WebMvcConfig.class)
    @Import(WebMvcConfig.class)
    static class ContextConfiguration {
    }

    /** 复用平台同一套 Long、时间及布尔 JSON 契约。 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(JacksonConfig.class)
    @Import(JacksonConfig.class)
    static class JsonConfiguration {
    }

    /**
     * 注册轻量业务异常兜底；存在平台全局异常处理器时完全退让。
     *
     * @return 业务异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(value = CommonBusinessExceptionHandler.class,
            type = "com.forgex.common.web.GlobalExceptionHandler", name = "globalExceptionHandler")
    public CommonBusinessExceptionHandler commonBusinessExceptionHandler() {
        return new CommonBusinessExceptionHandler();
    }
}
