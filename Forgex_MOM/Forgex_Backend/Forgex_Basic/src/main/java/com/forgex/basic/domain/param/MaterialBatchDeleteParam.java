package com.forgex.basic.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 物料批量删除请求参数类
 * <p>
 * 用于接收前端勾选多条物料进行批量删除时的 ID 列表
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料批量删除参数")
public class MaterialBatchDeleteParam {

    /**
     * 待删除的物料 ID 集合
     */
    @NotEmpty(message = "请选择要删除的物料")
    @Schema(description = "物料 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;
}

