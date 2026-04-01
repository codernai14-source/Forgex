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
package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 用户级别表格配置实体
 * <p>
 * 封装用户个性化的表格配置，用于动态渲染表格。
 * 查询优先级：用户配置 > 租户配置 > 公共配置（tenant_id=0）
 * </p>
 * <p>
 * 主要字段：
 * <ul>
 *   <li>tableCode - 表编码，唯一标识一个表格</li>
 *   <li>userId - 用户 ID，标识配置所属用户</li>
 *   <li>tenantId - 租户 ID，标识配置所属租户</li>
 *   <li>columnConfig - 列配置（JSON 格式）</li>
 *   <li>queryConfig - 查询字段配置（JSON 格式）</li>
 *   <li>sortConfig - 排序配置（JSON 格式）</li>
 *   <li>pageSize - 分页大小</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfig
 * @see BaseEntity
 */
@Data
@TableName("fx_user_table_config")
public class FxUserTableConfig extends BaseEntity {
    
    /**
     * 表编码
     * <p>唯一标识一个表格，用于查询和配置管理。</p>
     */
    private String tableCode;
    
    /**
     * 用户 ID
     * <p>标识配置所属用户，实现用户级别的个性化配置。</p>
     */
    private Long userId;
    
    /**
     * 租户 ID
     * <p>标识配置所属租户，与用户 ID 共同确定配置的唯一性。</p>
     */
    private Long tenantId;
    
    /**
     * 列配置（JSON 格式）
     * <p>包含列的显示/隐藏、排序、宽度等配置信息。</p>
     */
    private String columnConfig;
    
    /**
     * 查询字段配置（JSON 格式）
     * <p>包含查询字段的配置信息，如查询条件、操作符等。</p>
     */
    private String queryConfig;
    
    /**
     * 排序配置（JSON 格式）
     * <p>包含表格的排序规则配置。</p>
     */
    private String sortConfig;
    
    /**
     * 分页大小
     * <p>用户自定义的每页显示条数，默认为 20。</p>
     */
    private Integer pageSize;
    
    /**
     * 配置版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;
}
