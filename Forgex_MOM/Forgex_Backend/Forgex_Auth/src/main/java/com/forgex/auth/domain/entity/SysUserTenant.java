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
package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-租户关联实体
 * 对应数据库表：sys_user_tenant
 */
@Data
@TableName("sys_user_tenant")
public class SysUserTenant {
    /** 用户ID */
    private Long userId;
    /** 租户ID */
    private Long tenantId;
    /** 喜好排序（越大越靠前） */
    private Integer prefOrder;
    /** 是否默认租户：1默认 0非默认 */
    private Boolean isDefault;
    /** 最后使用时间 */
    private java.time.LocalDateTime lastUsed;
}
