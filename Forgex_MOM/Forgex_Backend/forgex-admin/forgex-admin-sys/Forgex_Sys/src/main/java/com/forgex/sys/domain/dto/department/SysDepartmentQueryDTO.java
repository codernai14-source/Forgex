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
package com.forgex.sys.domain.dto.department;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门查询DTO
 * 
 * 用于部门列表查询的参数封装
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDepartmentQueryDTO extends BaseGetParam {
    
    /** 租户ID */
    private Long tenantId;
    
    /** 父部门ID */
    private Long parentId;
    
    /** 部门名称（模糊查询） */
    private String deptName;
    
    /** 部门编码（模糊查询） */
    private String deptCode;
    
    /** 组织类型 */
    private String orgType;
    
    /** 状态 */
    private Boolean status;
}
