package com.forgex.basic.workshop.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车间分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkshopPageParam extends BaseGetParam {

    /** 车间编码。 */
    private String workshopCode;

    /** 车间名称。 */
    private String workshopName;

    /** 所属工厂 ID。 */
    private Long factoryId;

    /** 是否启用。 */
    private Boolean status;
}
