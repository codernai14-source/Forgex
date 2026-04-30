package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 供应商联系人 DTO
 * <p>
 * 用于封装供应商联系人信息，供基础数据、接口平台和其它服务统一复用。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierContactDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 联系人主表 ID
     */
    private Long id;

    /**
     * 供应商主表 ID
     */
    private Long supplierId;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人职位
     */
    private String contactPosition;

    /**
     * 联系人邮箱
     */
    private String contactEmail;
}
