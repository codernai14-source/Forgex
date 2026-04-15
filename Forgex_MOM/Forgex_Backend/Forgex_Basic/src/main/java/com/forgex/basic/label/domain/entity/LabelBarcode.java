package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 条码记录表实体类
 * <p>
 * 对应数据库表：label_barcode_record
 * 用于存储条码生成记录，支持条码追溯和业务场景管理
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@TableName("label_barcode_record")
public class LabelBarcode {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 条码号/序列号
     */
    @TableField("barcode_no")
    private String barcodeNo;

    /**
     * 模板类型
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 物料 ID
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 物料编码
     */
    @TableField("material_code")
    private String materialCode;

    /**
     * 物料名称
     */
    @TableField("material_name")
    private String materialName;

    /**
     * 供应商编码
     */
    @TableField("supplier_code")
    private String supplierCode;

    /**
     * 客户编码
     */
    @TableField("customer_code")
    private String customerCode;

    /**
     * 批次号
     */
    @TableField("batch_no")
    private String batchNo;

    /**
     * LOT 号
     */
    @TableField("lot_no")
    private String lotNo;

    /**
     * 工单号
     */
    @TableField("work_order_no")
    private String workOrderNo;

    /**
     * 工程卡号
     */
    @TableField("engineering_card_no")
    private String engineeringCardNo;

    /**
     * 生产线
     */
    @TableField("production_line")
    private String productionLine;

    /**
     * 业务场景
     * INCOMING=来料，PRODUCTION=生产，OUTBOUND=出库，OTHER=其他
     */
    @TableField("business_scene")
    private String businessScene;

    /**
     * 条码生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("generate_time")
    private LocalDateTime generateTime;

    /**
     * 状态：0-失效，1-有效
     */
    @TableField("status")
    private Integer status;

    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 工厂 ID
     */
    @TableField("factory_id")
    private Long factoryId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    private Boolean deleted;
}

