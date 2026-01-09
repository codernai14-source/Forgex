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

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户DTO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
public class SysUserDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 账号
     */
    private String account;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;
    
    /**
     * 入职时间
     */
    private LocalDate entryDate;
    
    /**
     * 所属部门ID
     */
    private Long departmentId;
    
    /**
     * 部门名称（关联查询）
     */
    private String departmentName;
    
    /**
     * 职位ID
     */
    private Long positionId;
    
    /**
     * 职位名称（关联查询）
     */
    private String positionName;
    
    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}
