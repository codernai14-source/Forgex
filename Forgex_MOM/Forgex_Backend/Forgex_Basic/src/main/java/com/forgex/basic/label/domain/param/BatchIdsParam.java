package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量 ID 参数类
 * <p>
 * 用于接收批量 ID 参数的请求
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "批量 ID 参数")
public class BatchIdsParam {

    /**
     * ID 列表
     */
    @NotEmpty(message = "ID 列表不能为空")
    @Schema(description = "ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;
}
