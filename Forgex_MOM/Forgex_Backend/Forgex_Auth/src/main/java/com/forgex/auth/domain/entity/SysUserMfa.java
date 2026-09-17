package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.crypto.FieldEncrypt;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户 MFA 密钥实体。
 * <p>
 * 映射表 {@code sys_user_mfa}。密钥与恢复码使用字段加密存储。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_user_mfa")
public class SysUserMfa {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户 ID，用户级 MFA 使用 0 */
    private Long tenantId;

    /** 用户 ID */
    private Long userId;

    /** TOTP 密钥密文 */
    @FieldEncrypt
    private String secretCiphertext;

    /** 是否已确认启用 */
    private Boolean enabled;

    /** 一次性恢复码密文，逗号分隔 */
    @FieldEncrypt
    private String recoveryCodesCiphertext;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
