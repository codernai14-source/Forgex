package com.forgex.basic.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 扩展配置查询参数类
 * <p>
 * 用于查询指定模块下的扩展字段配置
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "扩展配置查询参数")
public class ExtendConfigParam {

    /**
     * 模块编码（必填）
     * 例如：PURCHASE=采购，INVENTORY=库存，PRODUCTION=生产，SALES=销售
     */
    @NotBlank(message = "模块编码不能为空")
    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String module;
}

