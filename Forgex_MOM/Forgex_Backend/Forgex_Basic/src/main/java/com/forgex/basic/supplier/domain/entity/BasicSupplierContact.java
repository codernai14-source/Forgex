package com.forgex.basic.supplier.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商联系人实体。
 * <p>
 * 对应表 {@code basic_supplier_contact}，保存供应商多联系人信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_supplier_contact")
public class BasicSupplierContact extends BaseEntity {

    /**
     * 供应商主表 ID。
     */
    @TableField("supplier_id")
    private Long supplierId;

    /**
     * 联系人姓名。
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系人电话。
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系人职位。
     */
    @TableField("contact_position")
    private String contactPosition;

    /**
     * 联系人邮箱。
     */
    @TableField("contact_email")
    private String contactEmail;
}
