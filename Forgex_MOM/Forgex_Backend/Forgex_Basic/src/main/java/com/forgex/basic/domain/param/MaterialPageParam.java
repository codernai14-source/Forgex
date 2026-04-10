package com.forgex.basic.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料分页查询参数类
 * <p>
 * 用于接收前端分页搜索物料时的条件参数
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料分页查询参数")
public class MaterialPageParam extends BaseGetParam {

    /**
     * 物料编码（模糊查询）
     */
    @Schema(description = "物料编码")
    private String materialCode;

    /**
     * 物料名称（模糊查询）
     */
    @Schema(description = "物料名称")
    private String materialName;

    /**
     * 物料类型
     */
    @Schema(description = "物料类型")
    private String materialType;

    /**
     * 物料分类
     */
    @Schema(description = "物料分类")
    private String materialCategory;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;
    /**
     * 审批状态（NO_APPROVAL_REQUIRED=无需审批，PENDING=未审批，APPROVED=已审批，REJECTED=已驳回）
     */
    @Schema(description = "审批状态")
    private String approvalStatus;
}

