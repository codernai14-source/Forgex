package com.forgex.basic.material.domain.response;

import com.forgex.basic.material.domain.dto.MaterialDTO;
import com.forgex.basic.material.domain.vo.MaterialExtendVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料详情响应类
 * <p>
 * 用于返回物料的完整详细信息，包含基础信息、扩展字段、审批记录等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料详情响应")
public class MaterialDetailResponse {

    /**
     * 物料基础信息
     */
    @Schema(description = "物料基础信息")
    private MaterialDTO baseInfo;

    /**
     * 扩展字段数据
     * <p>
     * 格式示例：
     * [
     *   { "fieldName": "color", "fieldLabel": "颜色", "fieldValue": "红色", "fieldType": "STRING" },
     *   { "fieldName": "weight", "fieldLabel": "重量", "fieldValue": "2.5", "fieldType": "NUMBER" }
     * ]
     * </p>
     */
    @Schema(description = "扩展字段数据")
    private List<ExtendFieldItem> extendFields;

    /**
     * 审批状态详情
     */
    @Schema(description = "审批状态详情")
    private ApprovalInfo approvalInfo;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
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
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    /**
     * 扩展信息列表
     * 注意：原字段名 extends 是 Java 关键字，已更名为 extendList
     */
    @Schema(description = "扩展信息列表")
    private List<MaterialExtendVO> extendList;
    /**
     * 扩展字段条目
     */
    @Data
    @Schema(description = "扩展字段条目")
    public static class ExtendFieldItem {
        /**
         * 字段标识
         */
        @Schema(description = "字段标识")
        private String fieldName;

        /**
         * 字段标签
         */
        @Schema(description = "字段标签")
        private String fieldLabel;

        /**
         * 字段值
         */
        @Schema(description = "字段值")
        private String fieldValue;

        /**
         * 字段类型
         */
        @Schema(description = "字段类型")
        private String fieldType;
    }

    /**
     * 审批信息
     */
    @Data
    @Schema(description = "审批信息")
    public static class ApprovalInfo {
        /**
         * 审批状态
         */
        @Schema(description = "审批状态")
        private String approvalStatus;

        /**
         * 审批人
         */
        @Schema(description = "审批人")
        private String approver;

        /**
         * 审批时间
         */
        @Schema(description = "审批时间")
        private LocalDateTime approvalTime;

        /**
         * 审批意见
         */
        @Schema(description = "审批意见")
        private String approvalComment;
    }
}

