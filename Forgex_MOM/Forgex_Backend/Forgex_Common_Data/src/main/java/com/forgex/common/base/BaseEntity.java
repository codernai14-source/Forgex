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
package com.forgex.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用实体父类
 * <p>
 * 包含所有表的通用字段，并统一由 MyBatis-Plus 自动填充。
 * 字段说明：
 * <ul>
 *   <li>{@code id} 主键ID</li>
 *   <li>{@code tenantId} 租户ID</li>
 *   <li>{@code createTime} 创建时间</li>
 *   <li>{@code createBy} 创建人</li>
 *   <li>{@code updateTime} 更新时间</li>
 *   <li>{@code updateBy} 更新人</li>
 *   <li>{@code deleted} 逻辑删除标记</li>
 * </ul>
 */
@Data
public class BaseEntity {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 更新人 */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 逻辑删除：false=未删除 true=已删除 */
    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;
}
