/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.workflow.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.workflow.domain.dto.WfDashboardUserShareDTO;
import com.forgex.workflow.domain.dto.WfDashboardWeeklyResultDTO;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批任务执行Mapper接口。
 * <p>
 * 提供审批任务执行的数据访问操作。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Mapper
@DS("workflow")
public interface WfTaskExecutionMapper extends BaseMapper<WfTaskExecution> {

    /**
     * 查询审批首页近 7 日审批结果趋势。
     *
     * @param tenantId  租户 ID
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（不含）
     * @return 聚合结果
     */
    @Select("""
            SELECT DATE_FORMAT(end_time, '%Y-%m-%d') AS date,
                   SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS approvedCount,
                   SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS rejectedCount
            FROM wf_task_execution
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
              AND status IN (2, 3)
              AND end_time >= #{startTime}
              AND end_time < #{endTime}
            GROUP BY DATE_FORMAT(end_time, '%Y-%m-%d')
            ORDER BY DATE_FORMAT(end_time, '%Y-%m-%d')
            """)
    List<WfDashboardWeeklyResultDTO> selectDashboardWeeklyResults(@Param("tenantId") Long tenantId,
                                                                  @Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查询审批首页发起人审批数量占比。
     *
     * @param tenantId 租户 ID
     * @return 发起人聚合结果
     */
    @Select("""
            SELECT initiator_id AS initiatorId,
                   COALESCE(NULLIF(initiator_name, ''), CONCAT('用户', initiator_id)) AS initiatorName,
                   COUNT(*) AS count
            FROM wf_task_execution
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
            GROUP BY initiator_id, initiator_name
            ORDER BY count DESC, initiator_id ASC
            """)
    List<WfDashboardUserShareDTO> selectDashboardUserShares(@Param("tenantId") Long tenantId);
}
