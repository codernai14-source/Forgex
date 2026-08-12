package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户联系人同步 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerContactSyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long customerId;

    private String contactName;

    private String contactPosition;

    private String contactPhone;
}
