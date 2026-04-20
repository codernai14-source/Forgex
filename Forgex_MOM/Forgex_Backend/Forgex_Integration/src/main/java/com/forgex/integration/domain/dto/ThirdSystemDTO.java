package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方系统信息 DTO
 * <p>
 * 用于服务层数据传输
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ThirdSystemDTO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 第三方系统编码
     */
    private String systemCode;

    /**
     * 第三方系统名称
     */
    private String systemName;

    /**
     * 第三方系统 IP 地址
     */
    private String ipAddress;

    /**
     * 联系信息
     */
    private String contactInfo;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
