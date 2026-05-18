package com.forgex.basic.team.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班组主数据实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_team")
public class BasicTeam extends BaseEntity {

    /** 班组编码。 */
    @TableField("team_code")
    private String teamCode;

    /** 班组名称。 */
    @TableField("team_name")
    private String teamName;

    /** 负责人。 */
    @TableField("leader_employee_id")
    private Long leaderEmployeeId;

    /** 当前负责班次。 */
    @TableField("current_shift_id")
    private Long currentShiftId;

    /** 所属车间。 */
    @TableField("workshop_id")
    private Long workshopId;

    /** 是否启用。 */
    @TableField("status")
    private Boolean status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
