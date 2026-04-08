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

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户 - 租户关联实体
 * <p>
 * 对应数据库表：sys_user_tenant，用于存储用户与租户的多对多关联关系。
 * </p>
 *
 * <p>
 * 一个用户可以属于多个租户，通过该表记录用户的租户列表以及偏好设置。
 * </p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 * @see com.forgex.auth.mapper.SysUserTenantMapper
 */
@Data
@TableName("sys_user_tenant")
public class SysUserTenant {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId
    private Long id;

    /**
     * 用户 ID
     * <p>关联到用户表的主键</p>
     */
    private Long userId;

    /**
     * 租户 ID
     * <p>关联到租户表的主键</p>
     */
    private Long tenantId;

    /**
     * 偏好排序
     * <p>数值越大排序越靠前，用于用户自定义租户显示顺序</p>
     */
    private Integer prefOrder;

    /**
     * 是否默认租户
     * <p>
     * true：默认租户（1）<br>
     * false：非默认租户（0）
     * </p>
     */
    private Boolean isDefault;

    /**
     * 最后使用时间
     * <p>记录用户最后一次访问该租户的时间</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.time.LocalDateTime lastUsed;
}
