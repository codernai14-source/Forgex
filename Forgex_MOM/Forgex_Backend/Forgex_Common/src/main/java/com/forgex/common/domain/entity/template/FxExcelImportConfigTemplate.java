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
package com.forgex.common.domain.entity.template;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Excel 导入配置模板实体
 * <p>
 * 封装 Excel 导入的配置模板，支持租户级别配置。
 * 查询优先级：租户配置 → 公共配置（tenant_id=0）
 * </p>
 * <p>
 * 主要字段：
 * <ul>
 *   <li>tenantId - 租户 ID（0 表示公共配置）</li>
 *   <li>templateCode - 模板编码</li>
 *   <li>templateName - 模板名称</li>
 *   <li>bizType - 业务类型</li>
 *   <li>configData - 配置数据（JSON 格式）</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_excel_import_config_template")
public class FxExcelImportConfigTemplate extends BaseEntity {
    
    /**
     * 租户 ID
     * <p>0 表示公共配置，其他表示租户级别配置。</p>
     */
    private Long tenantId;
    
    /**
     * 模板编码
     * <p>唯一标识一个模板。</p>
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 业务类型
     * <p>标识该模板适用的业务场景。</p>
     */
    private String bizType;
    
    /**
     * 配置数据（JSON 格式）
     * <p>包含导入的列配置、验证规则等。</p>
     */
    private String configData;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 备注
     */
    private String remark;
}
