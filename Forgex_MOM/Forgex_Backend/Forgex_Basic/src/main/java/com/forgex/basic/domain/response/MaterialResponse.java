package com.forgex.basic.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 物料操作响应结果类
 * <p>
 * 专门用于接收物料导入、批量处理等操作的统计结果
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料操作响应结果")
public class MaterialResponse {

    /**
     * 操作总数量
     */
    @Schema(description = "操作总数量")
    private Integer total;

    /**
     * 成功数量
     */
    @Schema(description = "成功数量")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Schema(description = "失败数量")
    private Integer failCount;

    /**
     * 错误信息列表
     * 格式示例：["第 3 行：物料编码不能为空", "第 5 行：物料名称已存在"]
     */
    @Schema(description = "错误信息列表")
    private List<String> errorMessages;

}

