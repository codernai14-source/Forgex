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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户隔离跳过配置（持久化对象）
 * <p>
 * 对应数据库表：sys_tenant_ignore，用于配置哪些表、服务或方法需要跳过租户隔离检查。
 * </p>
 *
 * <p>
 * 适用场景：
 * <ul>
 *     <li>公共数据表（如字典表）需要跨租户共享</li>
 *     <li>某些特殊方法需要跨租户查询数据</li>
 * </ul>
 * </p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.config.TenantIgnoreLoader
 * @see com.forgex.auth.service.TenantIgnoreService
 */
@Data
@TableName("sys_tenant_ignore")
public class SysTenantIgnore {
    /**
     * 主键 ID（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置作用域
     * <p>
     * 可选值：
     * <ul>
     *     <li>TABLE：表级别忽略，matcher 填写表名</li>
     *     <li>SERVICE：服务级别忽略，matcher 填写全限定类名</li>
     *     <li>MAPPER：数据访问层忽略，matcher 填写全限定类名#方法名</li>
     * </ul>
     * </p>
     */
    private String scope;

    /**
     * 匹配内容
     * <p>
     * 根据 scope 不同，填写不同的匹配内容：
     * <ul>
     *     <li>TABLE：填写表名（如：sys_dict）</li>
     *     <li>SERVICE：填写全限定类名（如：com.forgex.sys.service.ISysUserService）</li>
     *     <li>MAPPER：填写全限定类名#方法名（如：com.forgex.sys.mapper.SysUserMapper#getUserById）</li>
     * </ul>
     * </p>
     */
    private String matcher;

    /**
     * 是否启用
     * <p>
     * true：启用（1）<br>
     * false：禁用（0）
     * </p>
     */
    private Boolean enabled;

    /**
     * 备注说明
     * <p>用于记录该配置的作用和原因</p>
     */
    private String remark;

    /**
     * 创建时间
     * <p>由 MyBatis-Plus 自动填充</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>由 MyBatis-Plus 自动填充</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * <p>
     * true：已删除<br>
     * false：未删除
     * </p>
     */
    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;
}
