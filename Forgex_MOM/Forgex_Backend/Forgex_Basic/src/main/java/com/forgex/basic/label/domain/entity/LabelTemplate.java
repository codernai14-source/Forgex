package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签模板主表实体类
 * <p>
 * 对应数据库表：label_template
 * 用于存储标签模板信息，包含模板编码、名称、类型、版本、内容等核心字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_label_template")
public class LabelTemplate extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板类型
     * MATERIAL=物料标签，PRODUCT=产品标签，LOT=LOT标签，INCOMING=来料标签，
     * SUPPLIER=供应商标签，CUSTOMER_MARK=客户唛头，CUSTOMER_LABEL=客户定制标签，
     * WORKSTATION=工位标签，PERSONNEL=人员标签，EQUIPMENT=设备标签，
     * PROCESS_STEP=工步标签，LOCATION=库位标签，SPQ_INNER=SPQ内箱标签，
     * PQ_OUTER=PQ外箱标签，OVERSEAS_OUTER=海外外箱标签，SHIPPING_BOX=出货箱数标签，
     * ENG_CARD_PACKAGE=工程卡包装标签
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 模板版本号
     */
    @TableField("template_version")
    private Integer templateVersion;

    /**
     * 是否默认模板：0-否，1-是
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 模板内容 JSON（包含版式信息、占位符配置等）
     */
    @TableField("template_content")
    private String templateContent;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 工厂 ID（NULL 表示全局模板）
     */
    @TableField("factory_id")
    private Long factoryId;
}

