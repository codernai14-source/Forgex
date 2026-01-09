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

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单查询DTO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenuQueryDTO extends BaseGetParam {
    
    /**
     * 模块ID
     */
    private Long moduleId;
    
    /**
     * 菜单名称（模糊查询）
     */
    private String name;
    
    /**
     * 菜单类型：catalog=目录, menu=菜单, button=按钮
     */
    private String type;
    
    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}
