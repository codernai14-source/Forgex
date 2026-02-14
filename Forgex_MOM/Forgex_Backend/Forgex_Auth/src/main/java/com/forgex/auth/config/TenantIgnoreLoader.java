/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
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
 * 租户隔离跳过配置加载器
 * <p>
 * 在应用启动时从数据库加载租户隔离跳过配置，并注册到TenantIgnoreRegistry中。
 * 支持表、Service和Mapper方法级别的租户隔离跳过配置。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>在应用启动时自动加载启用的租户隔离跳过配置</li>
 *   <li>支持表级别的租户隔离跳过</li>
 *   <li>支持Service级别的租户隔离跳过</li>
 *   <li>支持Mapper方法级别的租户隔离跳过</li>
 *   <li>使用@DS注解指定数据源为"admin"</li>
 * </ul>
 * <p><strong>配置类型：</strong></p>
 * <ul>
 *   <li>{@code TABLE} - 表级别，跳过指定表的租户过滤</li>
 *   <li>{@code SERVICE} - Service级别，跳过指定Service的所有方法的租户过滤</li>
 *   <li>{@code MAPPER} - Mapper方法级别，跳过指定Mapper方法的租户过滤</li>
 * </ul>
 * <p><strong>执行流程：</strong></p>
 * <ol>
 *   <li>应用启动时，在SqlSessionFactory初始化完成后执行</li>
 *   <li>从数据库查询所有启用的租户隔离跳过配置</li>
 *   <li>根据配置类型，分别注册到TenantIgnoreRegistry</li>
 *   <li>如果加载失败，重置TenantIgnoreRegistry</li>
 * </ol>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see TenantIgnoreRegistry
 * @see SysTenantIgnore
 */
@Component
@DependsOn("sqlSessionFactory")
@DS("admin")
public class TenantIgnoreLoader {
    /**
     * 租户隔离跳过配置Mapper
     */
    @Autowired
    private SysTenantIgnoreMapper mapper;

    /**
     * 初始化加载启用的配置项
     * <p>
     * 在应用启动时自动执行，从数据库加载所有启用的租户隔离跳过配置，
     * 并注册到TenantIgnoreRegistry中。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>从数据库查询所有enabled=true的配置项</li>
     *   <li>遍历配置项列表</li>
     *   <li>根据配置类型（TABLE/SERVICE/MAPPER）分别注册</li>
     *   <li>如果发生异常，重置TenantIgnoreRegistry</li>
     * </ol>
     */
    @PostConstruct
    public void init() {
        try {
            // 从数据库查询所有启用的配置项
            List<SysTenantIgnore> list = mapper.selectList(new LambdaQueryWrapper<SysTenantIgnore>()
                    .eq(SysTenantIgnore::getEnabled, true));
            
            // 遍历配置项列表
            for (SysTenantIgnore c : list) {
                // 获取配置范围和匹配器
                String scope = c.getScope();
                String matcher = c.getMatcher();
                
                // 根据配置类型分别注册
                if ("TABLE".equalsIgnoreCase(scope)) {
                    // 表级别：跳过指定表的租户过滤
                    TenantIgnoreRegistry.addIgnoreTable(matcher);
                } else if ("SERVICE".equalsIgnoreCase(scope)) {
                    // Service级别：跳过指定Service的所有方法的租户过滤
                    TenantIgnoreRegistry.addIgnoreService(matcher);
                } else if ("MAPPER".equalsIgnoreCase(scope)) {
                    // Mapper方法级别：跳过指定Mapper方法的租户过滤
                    TenantIgnoreRegistry.addIgnoreMapperMethod(matcher);
                }
            }
        } catch (Exception ignored) {
            // 加载失败，重置TenantIgnoreRegistry
            TenantIgnoreRegistry.reset();
        }
    }
}
