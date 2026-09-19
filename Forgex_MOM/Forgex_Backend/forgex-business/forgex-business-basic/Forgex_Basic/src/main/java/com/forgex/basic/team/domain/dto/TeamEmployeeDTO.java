package com.forgex.basic.team.domain.dto;

import com.forgex.basic.team.domain.entity.BasicTeamEmployee;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班组人员 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamEmployeeDTO extends BasicTeamEmployee {

    /** 工号。 */
    private String employeeNo;

    /** 姓名。 */
    private String employeeName;
}
