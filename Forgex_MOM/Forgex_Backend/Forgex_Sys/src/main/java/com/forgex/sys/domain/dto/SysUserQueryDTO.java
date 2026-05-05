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


package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户查询DTO
 *
 * @author coder_nai@163.com
 * @date 2025-01-07
 *
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQueryDTO extends BaseGetParam {

    /**
     * 账号（模糊查询）
     */
    private String account;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 手机号（模糊查询）
     */
    private String phone;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 员工 ID。
     */
    private Long employeeId;

    /**
     * 用户来源。
     */
    private Integer userSource;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色ID列表（支持多角色筛选）。
     */
    private List<Long> roleIds;

    /**
     * 入职开始日期
     */
    private LocalDate entryDateStart;

    /**
     * 入职结束日期
     */
    private LocalDate entryDateEnd;

    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;

    /**
     * 租户ID
     */
    private Long tenantId;
}
