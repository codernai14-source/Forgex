package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签打印记录表实体类
 * <p>
 * 对应数据库表：label_print_record
 * 用于存储标签打印历史记录，支持追溯和补打
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@TableName("basic_label_print_record")
public class LabelPrintRecord {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 打印流水号
     */
    @TableField("print_no")
    private String printNo;

    /**
     * 使用的模板 ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 模板编码（冗余）
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板名称（冗余）
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板版本（冗余，用于追溯）
     */
    @TableField("template_version")
    private Integer templateVersion;

    /**
     * 模板类型（冗余）
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 打印类型
     * NORMAL=正常打印，REPRINT=补打
     */
    @TableField("print_type")
    private String printType;

    /**
     * 条码号/序列号
     */
    @TableField("barcode_no")
    private String barcodeNo;

    /**
     * 序列号
     */
    @TableField("serial_no")
    private String serialNo;

    /**
     * 工程卡号
     */
    @TableField("engineering_card_no")
    private String engineeringCardNo;

    /**
     * LOT 号
     */
    @TableField("lot_no")
    private String lotNo;

    /**
     * 批次号
     */
    @TableField("batch_no")
    private String batchNo;

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
     * 本次打印的数据快照（用于追溯和补打）
     */
    @TableField("data_snapshot")
    private String dataSnapshot;

    /**
     * 本次实际返回给前端的打印模板内容（填充后的完整 JSON）
     */
    @TableField("print_result_json")
    private String printResultJson;

    /**
     * 打印张数
     */
    @TableField("print_count")
    private Integer printCount;

    /**
     * 工厂 ID
     */
    @TableField("factory_id")
    private Long factoryId;

    /**
     * 操作人 ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 打印时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("print_time")
    private LocalDateTime printTime;

    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;

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
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    private Boolean deleted;
}
