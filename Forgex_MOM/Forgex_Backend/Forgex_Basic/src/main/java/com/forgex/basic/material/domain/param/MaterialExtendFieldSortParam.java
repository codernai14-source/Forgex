package com.forgex.basic.material.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 物料附属字段排序参数。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Data
@Schema(description = "物料附属字段排序参数")
public class MaterialExtendFieldSortParam {

    /**
     * 字段排序项。
     */
    @Schema(description = "字段排序项")
    private List<Item> items;

    /**
     * 排序项。
     */
    @Data
    @Schema(description = "排序项")
    public static class Item {

        /**
         * 字段配置 ID。
         */
        @Schema(description = "字段配置 ID")
        private Long id;

        /**
         * 排序号。
         */
        @Schema(description = "排序号")
        private Integer orderNum;
    }
}
