package com.forgex.common.license;

import lombok.Data;

import java.util.List;

/**
 * 运行时授权状态
 * <p>
 * 供网关、系统管理页和健康检查接口统一使用。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class LicenseRuntimeInfo {

    /**
     * 当前部署实例编码。
     */
    private String instanceCode;

    /**
     * 授权状态。
     */
    private LicenseStatus status;

    /**
     * 当前是否允许执行业务访问。
     */
    private boolean valid;

    /**
     * 状态消息。
     */
    private String message;

    /**
     * 是否已配置公钥。
     */
    private boolean publicKeyConfigured;

    /**
     * 签名是否有效。
     */
    private boolean signatureValid;

    /**
     * 机器码是否匹配。
     */
    private boolean machineMatched;

    /**
     * 客户码是否匹配。
     */
    private boolean customerMatched;

    /**
     * 当前客户码。
     */
    private String customerCode;

    /**
     * 当前机器码。
     */
    private String machineCode;

    /**
     * 授权编号。
     */
    private String licenseId;

    /**
     * 客户名称。
     */
    private String customerName;

    /**
     * 版本类型。
     */
    private String edition;

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
     * 剩余天数。
     */
    private Long remainingDays;

    /**
     * 最近一次检查时间。
     */
    private String lastCheckedAt;

    /**
     * request-info 文件路径。
     */
    private String requestInfoPath;

    /**
     * license 文件路径。
     */
    private String licensePath;

    /**
     * 当前请求信息。
     */
    private LicenseRequestInfo requestInfo;
}
