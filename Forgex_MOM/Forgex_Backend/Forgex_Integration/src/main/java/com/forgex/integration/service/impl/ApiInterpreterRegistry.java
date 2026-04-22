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
 * 鍏ユ牳瑙ｉ噴鍣ㄦ敞鍐岃〃
 */
@Component
@RequiredArgsConstructor
public class ApiInterpreterRegistry {

    private final ApplicationContext applicationContext;

    private Map<String, ApiInboundInterpreter> registry = Collections.emptyMap();

    @PostConstruct
    public void init() {
        Map<String, ApiInboundInterpreter> beans = applicationContext.getBeansOfType(ApiInboundInterpreter.class);
        registry = Collections.unmodifiableMap(new HashMap<>(beans));
    }

    public ApiInboundInterpreter get(String beanName) {
        return registry.get(beanName);
    }
}
