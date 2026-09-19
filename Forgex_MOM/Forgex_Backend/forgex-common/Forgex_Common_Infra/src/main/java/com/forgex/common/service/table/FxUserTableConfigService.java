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
package com.forgex.common.service.table;

import com.forgex.common.domain.dto.table.FxUserTableConfigDTO;

/**
 * 用户级别表格配置服务接口
 * <p>
 * 提供用户级别表格配置的管理功能，支持用户个性化配置表格。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>获取用户级别表格配置</li>
 *   <li>保存用户级别表格配置</li>
 *   <li>重置用户级别表格配置（恢复为租户配置）</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>用户自定义表格列的显示/隐藏、排序</li>
 *   <li>用户自定义分页大小</li>
 *   <li>用户自定义查询字段配置</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxUserTableConfigDTO
 */
public interface FxUserTableConfigService {
    
    /**
     * 获取用户级别表格配置
     * <p>
     * 根据表编码、租户 ID 和用户 ID 获取用户个性化的表格配置。
     * </p>
     * 
     * @param tableCode 表编码，不能为空
     * @param tenantId 租户 ID，不能为空
     * @param userId 用户 ID，不能为空
     * @return 用户级别表格配置 DTO，不存在时返回 null
     */
    FxUserTableConfigDTO getUserTableConfig(String tableCode, Long tenantId, Long userId);
    
    /**
     * 保存用户级别表格配置
     * <p>
     * 保存用户个性化的表格配置，如果配置已存在则更新，否则创建新配置。
     * </p>
     * 
     * @param dto 用户级别表格配置 DTO
     * @param userId 用户 ID
     * @return 配置 ID
     */
    Long saveUserTableConfig(FxUserTableConfigDTO dto, Long userId);
    
    /**
     * 删除用户级别表格配置
     * <p>
     * 删除用户个性化的表格配置，删除后用户将使用租户或公共配置。
     * </p>
     * 
     * @param tableCode 表编码
     * @param tenantId 租户 ID
     * @param userId 用户 ID
     * @return 是否删除成功
     */
    Boolean deleteUserTableConfig(String tableCode, Long tenantId, Long userId);
}
