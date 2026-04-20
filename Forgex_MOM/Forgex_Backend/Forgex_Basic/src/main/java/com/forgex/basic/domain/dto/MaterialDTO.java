package com.forgex.basic.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.basic.domain.vo.MaterialExtendVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料数据传输对象
 * <p>
 * 用于向前端返回物料详细信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料信息")
public class MaterialDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 物料编码
     */
    @Schema(description = "物料编码")
    private String materialCode;

    /**
     * 物料名称
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
     * 规格型号
     */
    @Schema(description = "规格型号")
    private String specification;

    /**
     * 计量单位
     */
    @Schema(description = "计量单位")
    private String unit;

    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private String brand;

    /**
     * 物料图片 URL
     */
    @Schema(description = "物料图片 URL")
    private String imageUrl;
    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer orderNum;

    /**
     * 扩展信息 JSON 数据
     * <p>
     * 用于存储物料在该模块下的实际扩展字段值
     * 格式示例：{"color": "红色", "weight": "2.5", "warranty": "12个月"}
     * </p>
     */
    @Schema(description = "扩展信息 JSON 数据")
    private String extendJson;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 详细描述
     */
    @Schema(description = "详细描述")
    private String description;
    /**
     * 扩展信息列表
     * 注意：原字段名 extends 是 Java 关键字，已更名为 extendList
     */
    @Schema(description = "扩展信息列表")
    private List<MaterialExtendVO> extendList;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态")
    private String approvalStatus;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID")
    private Long tenantId;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

