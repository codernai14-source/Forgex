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
package com.forgex.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.forgex.common.tenant.TenantMetaObjectHandler;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantIgnoreRegistry;
import com.forgex.common.tenant.TenantContextIgnore;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置
 * 注册分页拦截器与通用元对象处理器
 */
@Configuration
public class MybatisPlusConfig {
    /**
     * 注册分页拦截器
     * @return MybatisPlusInterceptor
     * @see com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 租户隔离拦截器（查询与更新自动带上租户条件）
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tid = TenantContext.get();
                return new LongValue(tid == null ? -1L : tid);
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                if (TenantContextIgnore.isIgnore()) {
                    return true;
                }
                return TenantIgnoreRegistry.ignoreTable(tableName);
            }
        }));
        // 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 注册通用字段自动填充处理器
     * @return TenantMetaObjectHandler
     * @see com.forgex.common.tenant.TenantMetaObjectHandler
     */
    @Bean
    public TenantMetaObjectHandler tenantMetaObjectHandler() {
        return new TenantMetaObjectHandler();
    }
}
