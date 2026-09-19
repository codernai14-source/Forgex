package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用 ID 参数类
 * <p>
 * 用于接收单个 ID 参数的请求
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "通用 ID 参数")
public class IdParam {

    /**
     * ID 值
     */
    @NotNull(message = "ID 不能为空")
    @Schema(description = "ID 值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
