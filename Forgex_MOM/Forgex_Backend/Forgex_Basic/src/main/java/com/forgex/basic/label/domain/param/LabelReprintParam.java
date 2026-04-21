package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标签补打参数
 * <p>
 * 用于补打标签的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签补打参数")
public class LabelReprintParam {

    /**
     * 原始打印记录 ID
     */
    @NotNull(message = "打印记录 ID 不能为空")
    @Schema(description = "打印记录 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recordId;

    /**
     * 补打张数（默认为 1）
     */
    @Schema(description = "补打张数")
    private Integer reprintCount;
}
