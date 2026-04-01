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
package com.forgex.common.domain.dto.table;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户级别表格配置 DTO
 * <p>
 * 封装用户个性化表格配置的完整信息，用于前端动态渲染表格。
 * 查询优先级：用户配置 > 租户配置 > 公共配置
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>tableCode - 表编码，唯一标识一个表格</li>
 *   <li>userId - 用户 ID</li>
 *   <li>tenantId - 租户 ID</li>
 *   <li>columns - 表格列配置列表</li>
 *   <li>queryFields - 查询字段配置列表</li>
 *   <li>pageSize - 用户自定义分页大小</li>
 *   <li>sortConfig - 排序配置</li>
 *   <li>version - 配置版本号</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableColumnDTO
 * @see FxTableQueryFieldDTO
 */
@Data
public class FxUserTableConfigDTO {
    
    /**
     * 表编码
     * <p>唯一标识一个表格，用于查询和配置管理。</p>
     */
    private String tableCode;
    
    /**
     * 用户 ID
     * <p>配置所属用户的标识。</p>
     */
    private Long userId;
    
    /**
     * 租户 ID
     * <p>配置所属租户的标识。</p>
     */
    private Long tenantId;
    
    /**
     * 表格列配置列表
     * <p>包含表格所有列的配置信息，用于渲染表格列。</p>
     */
    private List<FxTableColumnDTO> columns;
    
    /**
     * 查询字段配置列表
     * <p>包含表格所有可查询字段的配置信息，用于渲染查询条件。</p>
     */
    private List<FxTableQueryFieldDTO> queryFields;
    
    /**
     * 分页大小
     * <p>用户自定义的每页显示条数。</p>
     */
    private Integer pageSize;
    
    /**
     * 排序配置（JSON 格式）
     * <p>包含表格的排序规则配置。</p>
     */
    private String sortConfig;
    
    /**
     * 配置版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
