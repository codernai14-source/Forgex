package com.forgex.common.license;

import lombok.Data;

import java.util.List;

/**
 * License 载荷定义
 * <p>
 * 对应签名授权码中的 payloadJson 结构。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class LicensePayload {

    /**
     * 授权编号。
     */
    private String licenseId;

    /**
     * 产品名称。
     */
    private String product;

    /**
     * 版本类型。
     */
    private String edition;

    /**
     * 客户码。
     */
    private String customerCode;

    /**
     * 客户名称。
     */
    private String customerName;

    /**
     * 机器码。
     */
    private String machineCode;

    /**
     * 授权模块。
     */
    private List<String> modules;

    /**
     * 最大用户数。
     */
    private Integer maxUsers;

    /**
     * 最大租户数。
     */
    private Integer maxTenants;

    /**
     * 签发时间。
     */
    private String issuedAt;

    /**
     * 生效时间。
     */
    private String effectiveAt;

    /**
     * 到期时间。
     */
    private String expireAt;

    /**
     * 授权时长（天）。
     */
    private Integer durationDays;

    /**
     * 宽限期（天）。
     */
    private Integer graceDays;

    /**
     * 签发序号。
     */
    private Integer issueSerial;

    /**
     * 备注。
     */
    private String remark;
}
