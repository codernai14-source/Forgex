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
 * 接口返回消息模板实体
 * <p>
 * 封装接口返回的消息模板，支持多语言和参数替换。
 * 查询优先级：租户配置 → 公共配置（tenant_id=0）
 * </p>
 * <p>
 * 主要字段：
 * <ul>
 *   <li>tenantId - 租户 ID（0 表示公共配置）</li>
 *   <li>templateCode - 模板编码，唯一标识一个模板</li>
 *   <li>templateName - 模板名称</li>
 *   <li>templateContent - 模板内容，支持参数占位符 {paramName}</li>
 *   <li>templateType - 模板类型：SUCCESS-成功，ERROR-错误，WARN-警告，INFO-信息</li>
 *   <li>lang - 语言：zh-CN、en-US 等</li>
 *   <li>params - 参数定义（JSON 格式）</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_response_message_template")
public class SysResponseMessageTemplate extends BaseEntity {
    
    /**
     * 租户 ID
     * <p>0 表示公共配置，其他表示租户级别配置。</p>
     */
    private Long tenantId;
    
    /**
     * 模板编码
     * <p>唯一标识一个模板，用于查询和配置管理。</p>
     */
    private String templateCode;
    
    /**
     * 模板名称
     * <p>模板的描述性名称。</p>
     */
    private String templateName;
    
    /**
     * 模板内容
     * <p>支持参数占位符，如：操作成功：{username}。</p>
     */
    private String templateContent;
    
    /**
     * 模板类型
     * <p>SUCCESS-成功，ERROR-错误，WARN-警告，INFO-信息。</p>
     */
    private String templateType;
    
    /**
     * 语言
     * <p>zh-CN、en-US、ja-JP 等。</p>
     */
    private String lang;
    
    /**
     * 参数定义（JSON 格式）
     * <p>定义模板中使用的参数，如：{"username": "用户名"}。</p>
     */
    private String params;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 备注
     */
    private String remark;
}
