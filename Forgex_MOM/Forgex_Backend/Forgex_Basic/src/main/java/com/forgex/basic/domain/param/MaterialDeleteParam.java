package com.forgex.basic.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 物料单个删除请求参数类
 * <p>
 * 用于接收前端删除单个物料时的 ID
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料删除参数")
public class MaterialDeleteParam {

    /**
     * 待删除的物料 ID
     */
    @NotNull(message = "物料 ID 不能为空")
    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
