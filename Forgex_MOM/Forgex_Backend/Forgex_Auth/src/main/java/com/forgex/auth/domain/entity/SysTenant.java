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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 租户实体（持久化对象）
 * 对应数据库表：sys_tenant
 */
@Data
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    /** 租户名称 */
    private String tenantName;
    /** 描述 */
    private String description;
    /** 状态：1启用 0禁用 */
    private Integer status;
    /** 编码 */
    private String tenantCode;
    /** Logo */
    private String logo;
    /** 租户ID */
    @TableField(exist = false)
    private Long tenantId;
}
