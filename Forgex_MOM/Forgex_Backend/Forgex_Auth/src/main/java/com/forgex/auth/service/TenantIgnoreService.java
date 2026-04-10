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
package com.forgex.auth.service;

import com.forgex.common.web.R;

/**
 * 租户隔离跳过配置服务接口
 * <p>
 * 提供租户隔离规则的热更新能力，支持运行时动态加载跳过配置。
 * 用于管理哪些表、服务方法或 Mapper 方法需要跳过租户隔离过滤。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #reload()} - 重新加载跳过配置</li>
 * </ul>
 * <p>跳过类型：</p>
 * <ul>
 *   <li>TABLE - 表级别跳过租户隔离</li>
 *   <li>SERVICE - 服务方法级别跳过租户隔离</li>
 *   <li>MAPPER - Mapper 方法级别跳过租户隔离</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see com.forgex.auth.service.impl.TenantIgnoreServiceImpl
 * @see com.forgex.common.tenant.TenantIgnoreRegistry
 * @see com.forgex.auth.domain.entity.SysTenantIgnore
 */
public interface TenantIgnoreService {
    /**
     * 重新加载跳过配置
     * <p>
     * 从数据库中读取所有启用的租户隔离跳过规则，并更新到注册表中。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>清空注册表中的所有现有规则</li>
     *   <li>从数据库查询所有 enabled=true 的跳过配置</li>
     *   <li>根据 scope 类型分别注册到 TenantIgnoreRegistry</li>
     *   <li>支持 TABLE、SERVICE、MAPPER 三种类型</li>
     * </ol>
     *
     * @return {@link R} 包含是否重新加载成功的统一返回结构
     * @see com.forgex.common.tenant.TenantIgnoreRegistry#reset()
     * @see com.forgex.common.tenant.TenantIgnoreRegistry#addIgnoreTable(String)
     * @see com.forgex.common.tenant.TenantIgnoreRegistry#addIgnoreService(String)
     * @see com.forgex.common.tenant.TenantIgnoreRegistry#addIgnoreMapperMethod(String)
     */
    R<Boolean> reload();
}

