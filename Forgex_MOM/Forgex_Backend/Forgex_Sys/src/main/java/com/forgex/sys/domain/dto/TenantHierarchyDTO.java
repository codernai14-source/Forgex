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

import com.forgex.sys.domain.entity.SysTenant;
import lombok.Data;

import java.util.List;

/**
 * 租户层级关系 DTO
 * <p>
 * 用于表示租户的父子关系，包含当前租户、父租户和子租户列表。
 * 
 * @author coder_nai
 * @version 1.0
 * @see SysTenant
 */
@Data
public class TenantHierarchyDTO {
    
    /**
     * 当前租户
     */
    private SysTenant currentTenant;
    
    /**
     * 父租户（如果存在）
     */
    private SysTenant parentTenant;
    
    /**
     * 子租户列表
     */
    private List<SysTenant> childTenants;
}
