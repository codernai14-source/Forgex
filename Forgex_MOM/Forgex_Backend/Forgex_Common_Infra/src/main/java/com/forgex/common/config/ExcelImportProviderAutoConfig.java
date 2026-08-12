package com.forgex.common.config;

import com.forgex.common.service.excel.provider.JsonOptionProvider;
import com.forgex.common.service.excel.provider.RoleListOptionProvider;
import com.forgex.common.service.excel.provider.TemplateOptionProviderRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Excel 导入模板 Provider 自动配置
 * <p>
 * 在应用启动时自动注册所有的 TemplateOptionProvider 实现。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 * @see TemplateOptionProviderRegistry
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExcelImportProviderAutoConfig {

    private final TemplateOptionProviderRegistry registry;
    private final JsonOptionProvider jsonOptionProvider;
    
    // 注意：RoleListOptionProvider 需要由业务模块（如 Sys 模块）实现并注册
    // 这里不强制依赖，避免循环依赖问题

    /**
     * 应用启动时注册 Provider
     * <p>
     * 监听 ApplicationReadyEvent 事件，在应用完全启动后注册所有 Provider。
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerProviders() {
        log.info("Register Excel Import Template Option Providers...");
        
        // 注册 JSON 选项 Provider（用于静态 JSON 数据源）
        registry.register("JSON", jsonOptionProvider);
        
        // 注意：ROLE_LIST Provider 需要业务模块实现后自动注册
        
        log.info("Registered {} providers: {}", 
                registry.getAllProviderCodes().size(), 
                registry.getAllProviderCodes());
    }
}
