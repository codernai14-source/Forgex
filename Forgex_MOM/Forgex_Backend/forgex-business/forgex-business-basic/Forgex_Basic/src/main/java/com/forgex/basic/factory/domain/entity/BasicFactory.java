package com.forgex.basic.factory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工厂信息表实体类
 * <p>
 * 对应数据库表：basic_factory
 * 用于存储工厂基础信息，包含工厂编码、名称、类型、地址等核心字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_factory")
public class BasicFactory extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工厂编码（租户内唯一）
     */
    @TableField("factory_code")
    private String factoryCode;

    /**
     * 工厂名称
     */
    @TableField("factory_name")
    private String factoryName;

    /**
     * 工厂类型
     * MANUFACTURING=制造厂，ASSEMBLY=装配厂，WAREHOUSE=仓库
     */
    @TableField("factory_type")
    private String factoryType;

    /**
     * 工厂地址
     */
    @TableField("address")
    private String address;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

