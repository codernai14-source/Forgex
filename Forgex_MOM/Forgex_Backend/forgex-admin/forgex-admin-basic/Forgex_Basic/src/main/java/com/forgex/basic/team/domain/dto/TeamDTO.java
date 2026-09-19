package com.forgex.basic.team.domain.dto;

import com.forgex.basic.team.domain.entity.BasicTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 班组主数据 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamDTO extends BasicTeam {

    /** 负责人姓名。 */
    private String leaderEmployeeName;

    /** 负责人工号。 */
    private String leaderEmployeeNo;

    /** 当前班次名称。 */
    private String currentShiftName;

    /** 所属车间名称。 */
    private String workshopName;

    /** 所属车间编码。 */
    private String workshopCode;

    /** 班组人员列表。 */
    private List<TeamEmployeeDTO> employeeList = new ArrayList<>();
}
