package com.forgex.common.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料主数据聚合 DTO。
 * <p>
 * 统一封装物料主表和附属信息，作为导入和第三方同步的数据载体。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@Data
public class MaterialAggregateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String materialCode;

    private String materialName;

    private String materialType;

    private String materialCategory;

    private String specification;

    private String unit;

    private String brand;

    private String imageUrl;

    private Integer orderNum;

    private String extendJson;

    private String remark;

    private String description;

    private Integer status;

    private String approvalStatus;

    private String createBy;

    @com.forgex.common.api.annotation.AutoFillUsername(userIdField = "createBy")
    private String createByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private List<MaterialExtendSyncDTO> extendList = new ArrayList<>();
}
