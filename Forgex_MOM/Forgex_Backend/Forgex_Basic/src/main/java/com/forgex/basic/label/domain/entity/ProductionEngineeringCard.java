package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 工程卡表实体类
 * <p>
 * 对应数据库表：production_engineering_card
 * 用于存储工程卡信息，包含工程卡号、物料信息、工单信息、生产计划等核心字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_production_engineering_card")
public class ProductionEngineeringCard extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工程卡号（租户内唯一）
     */
    @TableField("card_no")
    private String cardNo;

    /**
     * 物料 ID
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 物料编码（冗余字段，便于查询）
     */
    @TableField("material_code")
    private String materialCode;

    /**
     * 物料名称（冗余字段）
     */
    @TableField("material_name")
    private String materialName;

    /**
     * 工单号
     */
    @TableField("work_order_no")
    private String workOrderNo;

    /**
     * 销售订单号
     */
    @TableField("sales_order_no")
    private String salesOrderNo;

    /**
     * 客户 ID
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 客户编码（冗余）
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
     * 计划数量
     */
    @TableField("plan_qty")
    private BigDecimal planQty;

    /**
     * 实际数量
     */
    @TableField("actual_qty")
    private BigDecimal actualQty;

    /**
     * SPQ（标准包装数量）
     */
    @TableField("spq")
    private BigDecimal spq;

    /**
     * PQ（包装数量）
     */
    @TableField("pq")
    private BigDecimal pq;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 生产线
     */
    @TableField("production_line")
    private String productionLine;

    /**
     * 工作中心
     */
    @TableField("work_center")
    private String workCenter;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 周别代码（如：2026W15）
     */
    @TableField("week_code")
    private String weekCode;

    /**
     * 状态
     * CREATED=已创建，IN_PROGRESS=生产中，COMPLETED=已完成，CLOSED=已关闭
     */
    @TableField("status")
    private String status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 工厂 ID
     */
    @TableField("factory_id")
    private Long factoryId;
}
