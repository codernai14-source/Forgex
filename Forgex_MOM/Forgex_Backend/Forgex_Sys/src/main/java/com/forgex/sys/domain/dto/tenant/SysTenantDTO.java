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
 * 绉熸埛杩斿洖瀵硅薄
 * <p>
 * 鐢ㄤ簬鍓嶇灞曠ず鐨勭鎴蜂俊鎭?
 * </p>
 *
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysTenant
 * @see com.forgex.common.enums.TenantTypeEnum
 */
@Data
public class SysTenantDTO {

    /** 绉熸埛ID */
    private Long id;

    /** 绉熸埛鍚嶇О */
    private String tenantName;

    /** 绉熸埛缂栫爜 */
    private String tenantCode;

    /** 鎻忚堪 */
    private String description;

    /** Logo */
    private String logo;

    /** 绉熸埛绫诲埆 */
    private TenantTypeEnum tenantType;

    /** 绉熸埛绫诲埆鎻忚堪 */
    private String tenantTypeDesc;

    /** 父租户 ID */
    private Long parentTenantId;

    /** 父租户名称 */
    private String parentTenantName;

    /** 鐘舵€侊細false=绂佺敤锛宼rue=鍚敤 */
    private Boolean status;

    /** 鍒涘缓鏃堕棿 */
    private LocalDateTime createTime;

    /** 鏇存柊鏃堕棿 */
    private LocalDateTime updateTime;

    /** 鍒涘缓浜?*/
    private String createBy;

    /** 鏇存柊浜?*/
    private String updateBy;
}
