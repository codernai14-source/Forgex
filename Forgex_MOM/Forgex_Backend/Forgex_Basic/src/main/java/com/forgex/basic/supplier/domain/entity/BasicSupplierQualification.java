package com.forgex.basic.supplier.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 供应商资质实体。
 * <p>
 * 对应表 {@code basic_supplier_qualification}，保存供应商证书与附件引用。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_supplier_qualification")
public class BasicSupplierQualification extends BaseEntity {

    /**
     * 供应商主表 ID。
     */
    @TableField("supplier_id")
    private Long supplierId;

    /**
     * 资质类型。
     */
    @TableField("qualification_type")
    private String qualificationType;

    /**
     * 证书编号。
     */
    @TableField("certificate_no")
    private String certificateNo;

    /**
     * 发证日期。
     */
    @TableField("issue_date")
    private LocalDate issueDate;

    /**
     * 有效期至。
     */
    @TableField("expire_date")
    private LocalDate expireDate;

    /**
     * 附件引用。
     */
    @TableField("attachment")
    private String attachment;

    /**
     * 是否有效。
     */
    @TableField("valid")
    private Boolean valid;
}
