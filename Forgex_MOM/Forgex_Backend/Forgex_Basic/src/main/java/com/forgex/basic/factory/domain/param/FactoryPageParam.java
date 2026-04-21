package com.forgex.basic.factory.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工厂分页查询参数类
 * <p>
 * 用于接收前端分页搜索工厂时的条件参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工厂分页查询参数")
public class FactoryPageParam extends BaseGetParam {

    /**
     * 工厂编码（模糊查询）
     */
    @Schema(description = "工厂编码")
    private String factoryCode;

    /**
     * 工厂名称（模糊查询）
     */
    @Schema(description = "工厂名称")
    private String factoryName;

    /**
     * 工厂类型
     * MANUFACTURING=制造厂，ASSEMBLY=装配厂，WAREHOUSE=仓库
     */
    @Schema(description = "工厂类型")
    private String factoryType;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;
}
