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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.forgex.common.enums.TenantTypeEnum;
import lombok.Data;

/**
 * 管理库租户实体。
 * <p>
 * 映射表：{@code sys_tenant}。用于描述租户基本信息与展示属性。
 * 字段：
 * - {@code tenantName} 租户名称；
 * - {@code description} 描述；
 * - {@code tenantCode} 唯一编码；
 * - {@code logo} 展示 Logo（URL）；
 * - {@code tenantType} 租户类别（主租户、客户租户、供应商租户）。
 * <p>
 * 可扩展性：可按需增加联系人、到期时间、配额等业务字段。
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.common.enums.TenantTypeEnum
 */
@Data
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    
    /** 租户名称 */
    private String tenantName;
    
    /** 描述 */
    private String description;
    
    /** 唯一编码 */
    private String tenantCode;
    
    /** Logo URL */
    private String logo;
    
    /** 租户类别：MAIN_TENANT / CUSTOMER_TENANT / SUPPLIER_TENANT / PARTNER_TENANT 等，见 {@link TenantTypeEnum} */
    private TenantTypeEnum tenantType;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
}
