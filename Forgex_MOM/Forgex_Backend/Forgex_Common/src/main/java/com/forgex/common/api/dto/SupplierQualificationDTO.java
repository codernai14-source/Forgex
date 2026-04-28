package com.forgex.common.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 供应商资质 DTO
 * <p>
 * 封装供应商资质管理信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierQualificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资质主表 ID
     */
    private Long id;

    /**
     * 供应商主表 ID
     */
    private Long supplierId;

    /**
     * 资质类型
     */
    private String qualificationType;

    /**
     * 证书编号
     */
    private String certificateNo;

    /**
     * 发证日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate issueDate;

    /**
     * 有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expireDate;

    /**
     * 附件引用
     */
    private String attachment;

    /**
     * 是否有效
     */
    private Boolean valid;
}
