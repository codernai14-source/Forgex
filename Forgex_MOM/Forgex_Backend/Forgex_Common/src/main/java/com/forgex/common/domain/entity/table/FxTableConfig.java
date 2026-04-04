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
 * 表格配置实体
 * <p>
 * 封装表格的完整配置信息，用于动态渲染表格。
 * 支持租户级别配置，tenant_id=0 表示公共配置。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>tableCode - 表编码，唯一标识一个表格</li>
 *   <li>tenantId - 租户 ID（0 表示公共配置）</li>
 *   <li>tableNameI18nJson - 表名称国际化 JSON</li>
 *   <li>tableType - 表格类型，如"tree"、"table"等</li>
 *   <li>rowKey - 行键，用于标识表格行的唯一字段</li>
 *   <li>defaultPageSize - 默认分页大小</li>
 *   <li>defaultSortJson - 默认排序配置（JSON 格式）</li>
 *   <li>enabled - 是否启用</li>
 *   <li>version - 配置版本号</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableColumnConfig
 */
@Data
@TableName("fx_table_config")
public class FxTableConfig extends BaseEntity {
    
    /**
     * 表编码
     * <p>唯一标识一个表格，用于查询和配置管理。</p>
     */
    private String tableCode;
    
    /**
     * 租户 ID
     * <p>0 表示公共配置，其他表示租户级别配置。</p>
     */
    private Long tenantId;
    
    /**
     * 表名称国际化 JSON
     * <p>包含多语言的表名称配置，用于国际化显示表格标题。</p>
     */
    private String tableNameI18nJson;
    
    /**
     * 表格类型
     * <p>指定表格的类型，如"tree"（树形表格）、"table"（普通表格）等。</p>
     */
    private String tableType;
    
    /**
     * 行键
     * <p>用于标识表格行的唯一字段，默认为"id"。</p>
     */
    private String rowKey;
    
    /**
     * 默认分页大小
     * <p>指定表格的默认每页显示条数，默认为 20。</p>
     */
    private Integer defaultPageSize;
    
    /**
     * 默认排序配置
     * <p>JSON 格式的默认排序配置，用于初始化表格的排序状态。</p>
     */
    private String defaultSortJson;
    
    /**
     * 是否启用
     * <p>标识表格配置是否启用，false 时不显示表格。</p>
     */
    private Boolean enabled;
    
    /**
     * 配置版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;
}
