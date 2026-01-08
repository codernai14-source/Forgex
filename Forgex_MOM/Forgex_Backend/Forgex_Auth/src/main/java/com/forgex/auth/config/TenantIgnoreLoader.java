package com.forgex.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysTenantIgnore;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.auth.mapper.SysTenantIgnoreMapper;
import com.forgex.common.tenant.TenantIgnoreRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

/**
 * 启动时加载租户隔离跳过配置
 */
@Component
@DependsOn("sqlSessionFactory")
@DS("admin")
public class TenantIgnoreLoader {
    @Autowired
    private SysTenantIgnoreMapper mapper;

    /**
     * 初始化加载启用的配置项
     */
    @PostConstruct
    public void init() {
        try {
            List<SysTenantIgnore> list = mapper.selectList(new LambdaQueryWrapper<SysTenantIgnore>()
                    .eq(SysTenantIgnore::getEnabled, 1));
            for (SysTenantIgnore c : list) {
                String scope = c.getScope();
                String matcher = c.getMatcher();
                if ("TABLE".equalsIgnoreCase(scope)) {
                    TenantIgnoreRegistry.addIgnoreTable(matcher);
                } else if ("SERVICE".equalsIgnoreCase(scope)) {
                    TenantIgnoreRegistry.addIgnoreService(matcher);
                } else if ("MAPPER".equalsIgnoreCase(scope)) {
                    TenantIgnoreRegistry.addIgnoreMapperMethod(matcher);
                }
            }
        } catch (Exception ignored) {
            TenantIgnoreRegistry.reset();
        }
    }
}
