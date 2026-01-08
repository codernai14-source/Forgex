package com.forgex.sys.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户DTO
 * 
 * @author Forgex Team
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
     * 真实姓名
     */
    private String realName;
    
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
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
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
