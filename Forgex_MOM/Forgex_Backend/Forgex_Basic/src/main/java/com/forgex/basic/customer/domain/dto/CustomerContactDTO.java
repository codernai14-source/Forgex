package com.forgex.basic.customer.domain.dto;

import lombok.Data;

/**
 * 客户联系人数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CustomerContactDTO {
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 客户 ID。
     */
    private Long customerId;
    /**
     * 联系人名称。
     */
    private String contactName;
    /**
     * 联系人position。
     */
    private String contactPosition;
    /**
     * 联系人手机号。
     */
    private String contactPhone;
}
