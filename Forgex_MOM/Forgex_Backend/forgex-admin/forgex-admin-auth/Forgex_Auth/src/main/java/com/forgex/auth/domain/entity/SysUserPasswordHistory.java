package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户口令历史实体（Auth 侧）。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_user_password_history")
public class SysUserPasswordHistory {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID */
    private Long tenantId;

    /** 用户 ID */
    private Long userId;

    /** 历史口令哈希 */
    private String passwordHash;

    /** 写入时间 */
    private LocalDateTime createdTime;
}
