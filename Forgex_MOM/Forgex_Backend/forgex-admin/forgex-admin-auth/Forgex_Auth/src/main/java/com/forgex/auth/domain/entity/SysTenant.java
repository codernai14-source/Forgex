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
import com.forgex.common.enums.TenantTypeEnum;
import lombok.Data;

/**
 * 租户实体（持久化对象）
 * <p>
 * 对应数据库表：sys_tenant，用于存储系统租户的基本信息。
 * </p>
 *
 * <p>
 * 租户是多租户架构的核心概念，每个租户拥有独立的数据空间。
 * </p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.common.enums.TenantTypeEnum
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 * @see com.forgex.auth.mapper.SysTenantMapper
 */
@Data
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {

    /**
     * 租户名称
     * <p>租户的显示名称，用于前端展示</p>
     */
    private String tenantName;

    /**
     * 描述
     * <p>租户的详细介绍信息</p>
     */
    private String description;

    /**
     * 租户状态
     * <p>
     * false：禁用（该租户下用户无法登录）<br>
     * true：启用（正常状态）
     * </p>
     */
    private Boolean status;

    /**
     * 租户编码
     * <p>租户的唯一标识符，用于代码中识别租户</p>
     */
    private String tenantCode;

    /**
     * 租户 Logo
     * <p>租户的 Logo 图片 URL 地址</p>
     */
    private String logo;

    /**
     * 租户类别
     * <p>取值见 {@link com.forgex.common.enums.TenantTypeEnum}</p>
     */
    private TenantTypeEnum tenantType;

    /**
     * 租户 ID（非持久化字段）
     * <p>
     * 该字段不映射到数据库，用于在内存中存储当前登录租户的 ID
     * </p>
     */
    @TableField(exist = false)
    private Long tenantId;
}
