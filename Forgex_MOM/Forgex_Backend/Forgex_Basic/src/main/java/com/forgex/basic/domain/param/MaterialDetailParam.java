package com.forgex.basic.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 物料详情查询参数类
 * <p>
 * 用于接收前端查询物料详情时的 ID 参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料详情查询参数")
public class MaterialDetailParam {

    /**
     * 物料 ID
     */
    @NotNull(message = "物料 ID 不能为空")
    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


}

