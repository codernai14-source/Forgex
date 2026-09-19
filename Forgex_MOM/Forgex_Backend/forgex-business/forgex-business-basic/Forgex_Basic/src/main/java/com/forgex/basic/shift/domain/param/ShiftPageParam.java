package com.forgex.basic.shift.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班次分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPageParam extends BaseGetParam {

    /** 班次名称。 */
    private String shiftName;

    /** 班次编码。 */
    private String shiftCode;

    /** 是否启用。 */
    private Boolean status;
}
