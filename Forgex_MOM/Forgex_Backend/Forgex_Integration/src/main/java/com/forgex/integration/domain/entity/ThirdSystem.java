package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方系统信息实体类
 * <p>
 * 对应数据库表：fx_third_system
 * 用于存储第三方系统的基础信息，包括系统编码、名称、IP 地址等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_third_system")
public class ThirdSystem extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 第三方系统编码（唯一标识）
     */
    private String systemCode;

    /**
     * 第三方系统名称
     */
    private String systemName;

    /**
     * 第三方系统 IP 地址（多个 IP 用逗号分隔）
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
     * @see com.forgex.common.constant.SystemConstants#STATUS_DISABLED
     * @see com.forgex.common.constant.SystemConstants#STATUS_NORMAL
     */
    private Integer status;
}
