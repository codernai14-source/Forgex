package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户消息白名单实体类
 * <p>
 * 用于控制跨租户消息发送权限。
 * 只有在白名单中的租户对才能互相发送消息。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant_message_whitelist")
public class SysTenantMessageWhitelist extends BaseEntity {

    /**
     * 发送方租户ID
     */
    @TableField("sender_tenant_id")
    private Long senderTenantId;

    /**
     * 接收方租户ID
     */
    @TableField("receiver_tenant_id")
    private Long receiverTenantId;

    /**
     * 是否启用（true-启用，false-禁用）
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;
}



