package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 用户实体（持久化对象）
 * 对应数据库表：sys_user
 */
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String account;
    /** 用户名 */
    private String username;
    /** 加密后的密码 */
    private String password;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 状态：1启用 0禁用 */
    private Integer status;

    @TableField(exist = false)
    private Long tenantId;
}
