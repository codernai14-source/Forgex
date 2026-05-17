package com.forgex.basic.team.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班组分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamPageParam extends BaseGetParam {

    /** 班组编码。 */
    private String teamCode;

    /** 班组名称。 */
    private String teamName;

    /** 负责人 ID。 */
    private Long leaderEmployeeId;

    /** 当前负责班次 ID。 */
    private Long currentShiftId;

    /** 所属车间 ID。 */
    private Long workshopId;

    /** 是否启用。 */
    private Boolean status;
}
