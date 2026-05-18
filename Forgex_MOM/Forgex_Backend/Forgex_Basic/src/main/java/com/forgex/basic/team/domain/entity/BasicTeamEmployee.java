package com.forgex.basic.team.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班组人员实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_team_employee")
public class BasicTeamEmployee extends BaseEntity {

    /** 班组 ID。 */
    @TableField("team_id")
    private Long teamId;

    /** 人员 ID。 */
    @TableField("employee_id")
    private Long employeeId;
}
