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
import com.forgex.common.crypto.FieldEncryptInterceptor;
import com.forgex.common.dataperm.DataPermissionInterceptor;
import com.forgex.common.tenant.TenantMetaObjectHandler;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantIgnoreRegistry;
import com.forgex.common.tenant.TenantContextIgnore;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置
 * 注册分页拦截器、租户隔离拦截器、数据权限拦截器与通用元对象处理器
 */
@Configuration
public class MybatisPlusConfig {
    
    @Autowired
    private ApplicationContext applicationContext;

    @Lazy
    @Autowired
    private ConfigService configService;

    /**
     * 注册字段透明加密拦截器
     * <p>
     * 自动加密/解密标注了 {@link com.forgex.common.crypto.FieldEncrypt} 的字段
     * </p>
     * @return FieldEncryptInterceptor
     * @see com.forgex.common.crypto.FieldEncrypt
     * @see com.forgex.common.crypto.FieldEncryptInterceptor
     */
    @Bean
    public FieldEncryptInterceptor fieldEncryptInterceptor() {
        return new FieldEncryptInterceptor(configService);
    }

    /**
     * 注册MyBatis-Plus拦截器
     * @return MybatisPlusInterceptor
     * @see com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
     * @see com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor
     * @see com.forgex.common.dataperm.DataPermissionInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 租户隔离拦截器（查询与更新自动带上租户条件）
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
        
        // 2. 数据权限拦截器（根据用户角色自动过滤数据）
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(applicationContext));
        
        // 3. 分页拦截器
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
