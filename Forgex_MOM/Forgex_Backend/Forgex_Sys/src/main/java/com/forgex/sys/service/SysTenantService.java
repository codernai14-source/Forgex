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

import com.forgex.sys.domain.dto.tenant.SysTenantDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantQueryDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantSaveParam;

import java.util.List;

/**
 * 租户Service接口
 * <p>
 * 提供租户管理的业务逻辑接口，包含租户的增删改查功能
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysTenant
 * @see com.forgex.common.enums.TenantTypeEnum
 */
public interface SysTenantService {
    
    /**
     * 查询租户列表
     * 
     * @param queryDTO 查询参数
     * @return 租户列表
     */
    List<SysTenantDTO> list(SysTenantQueryDTO queryDTO);
    
    /**
     * 根据ID获取租户详情
     * 
     * @param id 租户ID
     * @return 租户详情
     */
    SysTenantDTO getById(Long id);
    
    /**
     * 新增租户
     * <p>
     * 新增租户时会进行以下校验：
     * <ul>
     *   <li>租户编码唯一性校验</li>
     *   <li>主租户唯一性校验（系统中只能有一个主租户）</li>
     * </ul>
     * </p>
     * 
     * @param param 租户参数
     * @return 租户ID
     * @throws RuntimeException 当租户编码已存在或主租户已存在时抛出异常
     */
    Long create(SysTenantSaveParam param);
    
    /**
     * 更新租户
     * <p>
     * 更新租户时会进行以下校验：
     * <ul>
     *   <li>租户编码唯一性校验（排除自己）</li>
     *   <li>主租户唯一性校验（如果修改为其他类型的主租户，系统中只能有一个主租户）</li>
     * </ul>
     * </p>
     * 
     * @param param 租户参数
     * @return 是否成功
     * @throws RuntimeException 当租户编码已存在或主租户已存在时抛出异常
     */
    Boolean update(SysTenantSaveParam param);
    
    /**
     * 删除租户
     * <p>
     * 删除租户时会进行以下校验：
     * <ul>
     *   <li>主租户不允许删除</li>
     *   <li>租户下存在用户时不允许删除</li>
     * </ul>
     * </p>
     * 
     * @param id 租户ID
     * @return 是否成功
     * @throws RuntimeException 当删除主租户或租户下存在用户时抛出异常
     */
    Boolean delete(Long id);
    
    /**
     * 获取主租户
     * 
     * @return 主租户信息，如果不存在则返回null
     */
    SysTenantDTO getMainTenant();
}
