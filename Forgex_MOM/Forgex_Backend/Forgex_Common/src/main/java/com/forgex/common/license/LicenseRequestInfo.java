package com.forgex.common.license;

import lombok.Data;

/**
 * 授权请求信息
 * <p>
 * 对应客户端现场生成的 request-info.json 文件结构。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class LicenseRequestInfo {

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
     * 机器码。
     */
    private String machineCode;

    /**
     * 操作系统类型。
     */
    private String osType;

    /**
     * 主机名。
     */
    private String hostname;

    /**
     * 生成时间。
     */
    private String generatedAt;

    /**
     * 工具版本。
     */
    private String toolVersion;
}
