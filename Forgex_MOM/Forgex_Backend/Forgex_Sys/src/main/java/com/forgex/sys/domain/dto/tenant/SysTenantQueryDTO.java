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
package com.forgex.sys.domain.dto.tenant;

import com.forgex.common.base.BaseGetParam;
import com.forgex.common.enums.TenantTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户查询参数
 * <p>
 * 用于租户列表查询的参数封装
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysTenant
 * @see com.forgex.common.enums.TenantTypeEnum
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTenantQueryDTO extends BaseGetParam {
    
    /** 租户名称（模糊查询） */
    private String tenantName;
    
    /** 租户编码（模糊查询） */
    private String tenantCode;
    
    /** 租户类别 */
    private TenantTypeEnum tenantType;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
}
