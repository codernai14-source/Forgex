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
package com.forgex.sys.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色DTO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
public class SysRoleDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码（对应数据库role_key字段）
     */
    private String roleCode;
    
    /**
     * 角色键（数据库字段）
     */
    private String roleKey;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}
