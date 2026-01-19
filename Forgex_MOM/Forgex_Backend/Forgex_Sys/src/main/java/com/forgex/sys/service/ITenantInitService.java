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
package com.forgex.sys.service;

import com.forgex.common.enums.TenantTypeEnum;

/**
 * 租户初始化服务接口
 * <p>
 * 提供租户初始化的业务逻辑接口，用于在新增租户时自动初始化模块、菜单、角色、用户等基础数据
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 */
public interface ITenantInitService {
    
    /**
     * 初始化租户
     * <p>
     * 在新增租户时自动初始化以下内容：
     * <ul>
     *   <li>复制系统模块到新租户</li>
     *   <li>复制系统模块下的菜单到新租户</li>
     *   <li>创建系统管理员角色</li>
     *   <li>创建管理员账号（主租户创建admin，其它租户创建administrator）</li>
     *   <li>将管理员账号关联到系统管理员角色</li>
     *   <li>将系统管理员角色关联到所有菜单</li>
     * </ul>
     * <p>
     * <strong>业务规则：</strong>
 * <ul>
 *   <li>主租户：创建admin账号，设置租户ID为主租户ID，可关联多个租户</li>
 *   <li>其它租户（客户租户、供应商租户）：创建administrator账号，设置租户ID为该租户，只能在该租户下使用</li>
 * </ul>
     * </p>
     * 
     * @param tenantId 租户ID
     * @param tenantName 租户名称
     * @param tenantType 租户类型
     */
    void initTenant(Long tenantId, String tenantName, TenantTypeEnum tenantType);
}
