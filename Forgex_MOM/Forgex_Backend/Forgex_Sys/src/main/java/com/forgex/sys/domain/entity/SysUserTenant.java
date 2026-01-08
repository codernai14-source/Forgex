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

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-租户绑定实体。
 * <p>
 * 映射表：{@code sys_user_tenant}。字段：
 * - {@code userId} 用户ID；
 * - {@code tenantId} 租户ID；
 * - {@code prefOrder} 偏好排序；
 * - {@code isDefault} 是否默认；
 * - {@code lastUsed} 最近使用时间。
 */
@Data
@TableName("sys_user_tenant")
public class SysUserTenant {
    /** 主键ID */
    @TableId
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 租户ID */
    private Long tenantId;
    /** 偏好排序 */
    private Integer prefOrder;
    /** 是否默认 */
    private Integer isDefault;
    /** 最近使用时间 */
    private java.time.LocalDateTime lastUsed;
}
