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

import com.forgex.common.enums.TenantTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户返回对象
 * <p>
 * 用于前端展示的租户信息
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysTenant
 * @see com.forgex.common.enums.TenantTypeEnum
 */
@Data
public class SysTenantDTO {
    
    /** 租户ID */
    private Long id;
    
    /** 租户名称 */
    private String tenantName;
    
    /** 租户编码 */
    private String tenantCode;
    
    /** 描述 */
    private String description;
    
    /** Logo */
    private String logo;
    
    /** 租户类别 */
    private TenantTypeEnum tenantType;
    
    /** 租户类别描述 */
    private String tenantTypeDesc;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 创建人 */
    private String createBy;
    
    /** 更新人 */
    private String updateBy;
}
