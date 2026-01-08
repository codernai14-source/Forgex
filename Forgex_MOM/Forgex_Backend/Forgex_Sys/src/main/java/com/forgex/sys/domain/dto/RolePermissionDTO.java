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

import java.util.List;

/**
 * 角色权限DTO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
public class RolePermissionDTO {
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 菜单ID列表（包含菜单和按钮权限）
     */
    private List<Long> menuIds;
}
