package com.forgex.integration.service.impl;

import com.forgex.integration.spi.ApiInboundInterpreter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 入站解释器注册表
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ApiInterpreterRegistry {

    private final ApplicationContext applicationContext;

    private Map<String, ApiInboundInterpreter> registry = Collections.emptyMap();

    /**
     * 处理init。
     */
    @PostConstruct
    public void init() {
        Map<String, ApiInboundInterpreter> beans = applicationContext.getBeansOfType(ApiInboundInterpreter.class);
        registry = Collections.unmodifiableMap(new HashMap<>(beans));
    }

    /**
     * 查询数据详情。
     *
     * @param beanName bean名称
     * @return 处理结果
     */
    public ApiInboundInterpreter get(String beanName) {
        return registry.get(beanName);
    }
}
