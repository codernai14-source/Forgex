package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-租户关联实体
 * 对应数据库表：sys_user_tenant
 */
@Data
@TableName("sys_user_tenant")
public class SysUserTenant {
    /** 用户ID */
    private Long userId;
    /** 租户ID */
    private Long tenantId;
    /** 喜好排序（越大越靠前） */
    private Integer prefOrder;
    /** 是否默认租户：1默认 0非默认 */
    private Integer isDefault;
    /** 最后使用时间 */
    private java.time.LocalDateTime lastUsed;
}
