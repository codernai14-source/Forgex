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
package com.forgex.common.tenant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 租户隔离跳过注册表
 * 配置哪些表不参与租户隔离（例如无 tenant_id 的全局表）
 */
public final class TenantIgnoreRegistry {
    /** 默认忽略的表（不含租户字段或需要跨租户查询） */
    private static final Set<String> IGNORE_TABLES = new HashSet<>();
    /** 忽略的服务实现类（全限定类名） */
    private static final Set<String> IGNORE_SERVICES = new HashSet<>();
    /** 忽略的Mapper方法（格式：全限定类名#方法名） */
    private static final Set<String> IGNORE_MAPPER_METHODS = new HashSet<>();

    static {
        // 用户、租户、用户-租户关联等表不带租户过滤
        IGNORE_TABLES.add("sys_user");
        IGNORE_TABLES.add("sys_tenant");
        IGNORE_TABLES.add("sys_user_tenant");
        // 用户-角色关联表不含租户字段，需忽略
        // 系统配置表使用公共库（global config），登录前需要读取，跳过租户隔离
        IGNORE_TABLES.add("sys_config");
        // 租户隔离跳过配置表本身不含租户字段
        IGNORE_TABLES.add("sys_tenant_ignore");
        // 租户菜单复制规则配置表，作为管理库全局配置，不参与租户隔离
        IGNORE_TABLES.add("sys_tenant_menu_copy_rule");
        // 国际化消息表，全局共享，不参与租户隔离
        IGNORE_TABLES.add("fx_i18n_message");
        // 表格配置表为全局共享配置，不参与租户隔离
        IGNORE_TABLES.add("fx_table_config");
        IGNORE_TABLES.add("fx_table_column_config");
        IGNORE_TABLES.add("basic_supplier");
        IGNORE_TABLES.add("basic_supplier_detail");
        IGNORE_TABLES.add("basic_supplier_contact");
        IGNORE_TABLES.add("basic_supplier_qualification");
        IGNORE_TABLES.add("basic_customer");
        IGNORE_TABLES.add("basic_customer_contact");
        IGNORE_TABLES.add("basic_customer_invoice");
        IGNORE_TABLES.add("basic_customer_extra");
    }

    /**
     * 判断是否忽略指定表
     * @param tableName 表名
     * @return true：忽略；false：参与隔离
     */
    public static boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName);
    }

    /**
     * 追加忽略表
     * @param tableName 表名
     */
    public static void addIgnoreTable(String tableName) {
        IGNORE_TABLES.add(tableName);
    }

    /** 添加忽略的服务实现类 */
    public static void addIgnoreService(String fqcn) {
        IGNORE_SERVICES.add(fqcn);
    }

    /** 添加忽略的Mapper方法（全限定类名#方法名） */
    public static void addIgnoreMapperMethod(String fqn) {
        IGNORE_MAPPER_METHODS.add(fqn);
    }

    /** 是否忽略服务实现类 */
    public static boolean isServiceIgnored(String fqcn) {
        return IGNORE_SERVICES.contains(fqcn);
    }

    /** 是否忽略Mapper方法 */
    public static boolean isMapperMethodIgnored(String fqn) {
        return IGNORE_MAPPER_METHODS.contains(fqn);
    }

    /**
     * 获取忽略表集合（只读）
     * @return 表集合
     */
    public static Set<String> getIgnoreTables() {
        return Collections.unmodifiableSet(IGNORE_TABLES);
    }

    /** 重置所有忽略集合（用于热更新） */
    public static void reset() {
        IGNORE_TABLES.clear();
        IGNORE_SERVICES.clear();
        IGNORE_MAPPER_METHODS.clear();
        // 默认忽略表
        IGNORE_TABLES.add("sys_user");
        IGNORE_TABLES.add("sys_tenant");
        IGNORE_TABLES.add("sys_user_tenant");
        IGNORE_TABLES.add("sys_config");
        IGNORE_TABLES.add("sys_tenant_ignore");
        IGNORE_TABLES.add("sys_tenant_menu_copy_rule");
        // 国际化消息表，全局共享，不参与租户隔离
        IGNORE_TABLES.add("fx_i18n_message");
        // 表格配置表为全局共享配置，不参与租户隔离
        IGNORE_TABLES.add("fx_table_config");
        IGNORE_TABLES.add("fx_table_column_config");
        IGNORE_TABLES.add("basic_supplier");
        IGNORE_TABLES.add("basic_supplier_detail");
        IGNORE_TABLES.add("basic_supplier_contact");
        IGNORE_TABLES.add("basic_supplier_qualification");
        IGNORE_TABLES.add("basic_customer");
        IGNORE_TABLES.add("basic_customer_contact");
        IGNORE_TABLES.add("basic_customer_invoice");
        IGNORE_TABLES.add("basic_customer_extra");
    }
}
