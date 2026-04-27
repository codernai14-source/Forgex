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
package com.forgex.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.workflow.domain.dto.WfApprovalActionLogDTO;
import com.forgex.workflow.domain.dto.WfApprovalInstanceDTO;
import com.forgex.workflow.domain.dto.WfDashboardAnalyticsVO;
import com.forgex.workflow.domain.dto.WfDashboardSummaryVO;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.param.WfExecutionAddSignParam;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.domain.param.WfExecutionBatchApproveParam;
import com.forgex.workflow.domain.param.WfExecutionBatchRemindParam;
import com.forgex.workflow.domain.param.WfExecutionBatchTransferParam;
import com.forgex.workflow.domain.param.WfExecutionCompensateParam;
import com.forgex.workflow.domain.param.WfExecutionDelegateSaveParam;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.domain.param.WfExecutionStartParam;
import com.forgex.workflow.domain.param.WfExecutionTransferParam;

import java.util.List;

/**
 * 审批执行服务接口。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IWfExecutionService {

    /**
     * 发起审批流程。
     * <p>
     * 根据任务编码查找已发布的任务配置，创建审批执行记录并初始化流程实例。
     * </p>
     *
     * @param param 发起审批参数，包含任务编码、表单内容、选择的审批人等
     * @return 执行记录 ID
     * @throws I18nBusinessException 当任务配置不存在、已禁用或参数不合法时抛出业务异常
     * @see WfExecutionStartParam
     */
    Long startExecution(WfExecutionStartParam param);

    /**
     * 审批同意。
     * <p>
     * 处理审批人的同意操作，记录审批意见并流转至下一个节点。
     * </p>
     *
     * @param param 审批参数，包含执行 ID、审批实例 ID、审批意见等
     * @return 审批结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在、用户无权限或参数不合法时抛出业务异常
     * @see WfExecutionApproveParam
     */
    Boolean approve(WfExecutionApproveParam param);

    /**
     * 审批驳回。
     * <p>
     * 处理审批人的驳回操作，记录驳回意见并退回或结束流程。
     * </p>
     *
     * @param param 审批参数，包含执行 ID、审批实例 ID、驳回类型、驳回意见等
     * @return 审批结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在、用户无权限或参数不合法时抛出业务异常
     * @see WfExecutionApproveParam
     */
    Boolean reject(WfExecutionApproveParam param);

    /**
     * 审批转交。
     * <p>
     * 将审批任务转交给其他用户处理。
     * </p>
     *
     * @param param 转交参数，包含执行 ID、审批实例 ID、目标审批人 ID、转交意见等
     * @return 转交结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在、用户无权限或参数不合法时抛出业务异常
     * @see WfExecutionTransferParam
     */
    Boolean transfer(WfExecutionTransferParam param);

    /**
     * 审批加签。
     * <p>
     * 在当前节点增加审批人，支持会签或或签模式。
     * </p>
     *
     * @param param 加签参数，包含执行 ID、审批实例 ID、目标审批人 ID、加签意见等
     * @return 加签结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在、用户无权限或参数不合法时抛出业务异常
     * @see WfExecutionAddSignParam
     */
    Boolean addSign(WfExecutionAddSignParam param);

    /**
     * 批量审批。
     * <p>
     * 批量处理多个审批实例的同意或驳回操作。
     * </p>
     *
     * @param param 批量审批参数，包含执行 ID 列表、审批状态、审批意见等
     * @return 批量审批结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在或参数不合法时抛出业务异常
     * @see WfExecutionBatchApproveParam
     */
    Boolean batchApprove(WfExecutionBatchApproveParam param);

    /**
     * 批量转交。
     * <p>
     * 批量转交多个审批实例给指定用户。
     * </p>
     *
     * @param param 批量转交参数，包含执行 ID 列表、目标审批人 ID、转交意见等
     * @return 批量转交结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在或参数不合法时抛出业务异常
     * @see WfExecutionBatchTransferParam
     */
    Boolean batchTransfer(WfExecutionBatchTransferParam param);

    /**
     * 批量提醒。
     * <p>
     * 批量发送催办提醒给多个审批实例的处理人。
     * </p>
     *
     * @param param 批量提醒参数，包含执行 ID 列表、提醒意见等
     * @return 批量提醒结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在或参数不合法时抛出业务异常
     * @see WfExecutionBatchRemindParam
     */
    Boolean batchRemind(WfExecutionBatchRemindParam param);

    /**
     * 补偿执行记录。
     * <p>
     * 补偿处理未激活的审批实例，用于异常情况下的数据修复。
     * </p>
     *
     * @param param 补偿参数，包含执行 ID 等
     * @return 补偿结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在或参数不合法时抛出业务异常
     * @see WfExecutionCompensateParam
     */
    Boolean compensateExecution(WfExecutionCompensateParam param);

    /**
     * 重试超时任务。
     * <p>
     * 重新处理超时的审批实例，支持超时自动通过或自动驳回。
     * </p>
     *
     * @param param 重试参数，包含执行 ID 等
     * @return 重试结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在或参数不合法时抛出业务异常
     * @see WfExecutionCompensateParam
     */
    Boolean retryTimeoutJobs(WfExecutionCompensateParam param);

    /**
     * 保存委托设置。
     * <p>
     * 设置用户的审批委托关系，将审批任务委托给他人处理。
     * </p>
     *
     * @param param 委托保存参数，包含委托人 ID、受托人 ID、委托备注等
     * @return 保存结果，成功返回 true
     * @throws I18nBusinessException 当参数不合法时抛出业务异常
     * @see WfExecutionDelegateSaveParam
     */
    Boolean saveDelegate(WfExecutionDelegateSaveParam param);

    /**
     * 取消委托。
     * <p>
     * 取消用户的审批委托设置，恢复本人处理审批任务。
     * </p>
     *
     * @param delegatorUserId 委托人用户 ID
     * @return 取消结果，成功返回 true
     * @throws I18nBusinessException 当参数不合法时抛出业务异常
     */
    Boolean cancelDelegate(Long delegatorUserId);

    /**
     * 撤销审批流程。
     * <p>
     * 发起人撤销正在审批中的流程实例。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @return 撤销结果，成功返回 true
     * @throws I18nBusinessException 当执行记录不存在或用户无权限时抛出业务异常
     */
    Boolean cancelExecution(Long executionId);

    /**
     * 获取执行记录详情。
     * <p>
     * 查询审批执行记录的完整信息，包含审批实例和操作日志。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @return 执行记录 DTO
     * @throws I18nBusinessException 当执行记录不存在时抛出业务异常
     * @see WfExecutionDTO
     */
    WfExecutionDTO getExecutionDetail(Long executionId);

    /**
     * 查询审批实例列表。
     * <p>
     * 查询执行记录下的所有审批实例，按创建时间升序排列。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @return 审批实例 DTO 列表
     * @throws I18nBusinessException 当执行记录不存在时抛出业务异常
     * @see WfApprovalInstanceDTO
     */
    List<WfApprovalInstanceDTO> listApprovalInstances(Long executionId);

    /**
     * 查询审批操作日志列表。
     * <p>
     * 查询执行记录下的所有审批操作日志，按创建时间降序排列。
     * </p>
     *
     * @param executionId 执行记录 ID
     * @return 审批操作日志 DTO 列表
     * @throws I18nBusinessException 当执行记录不存在时抛出业务异常
     * @see WfApprovalActionLogDTO
     */
    List<WfApprovalActionLogDTO> listApprovalActionLogs(Long executionId);

    /**
     * 分页查询我的发起。
     * <p>
     * 查询当前用户发起的所有审批实例，按发起时间降序排列。
     * </p>
     *
     * @param param 查询参数，包含任务名称、任务编码、状态等过滤条件
     * @return 执行记录分页结果
     * @throws I18nBusinessException 当查询参数不合法时抛出业务异常
     * @see WfExecutionQueryParam
     */
    Page<WfExecutionDTO> pageMyInitiated(WfExecutionQueryParam param);

    /**
     * 分页查询我的待办。
     * <p>
     * 查询当前用户待处理的所有审批实例，按审批实例更新时间降序排列。
     * </p>
     *
     * @param param 查询参数，包含任务名称、任务编码、状态等过滤条件
     * @return 执行记录分页结果
     * @throws I18nBusinessException 当查询参数不合法时抛出业务异常
     * @see WfExecutionQueryParam
     */
    Page<WfExecutionDTO> pageMyPending(WfExecutionQueryParam param);

    /**
     * 分页查询我的已办。
     * <p>
     * 查询当前用户已处理的所有审批实例，按审批时间降序排列。
     * </p>
     *
     * @param param 查询参数，包含任务名称、任务编码、状态、审批时间范围等过滤条件
     * @return 执行记录分页结果
     * @throws I18nBusinessException 当查询参数不合法时抛出业务异常
     * @see WfExecutionQueryParam
     */
    Page<WfExecutionDTO> pageMyProcessed(WfExecutionQueryParam param);

    /**
     * 分页查询我的抄送。
     * <p>
     * 查询当前用户抄送的所有审批实例，按审批实例更新时间降序排列。
     * </p>
     *
     * @param param 查询参数，包含任务名称、任务编码、状态等过滤条件
     * @return 执行记录分页结果
     * @throws I18nBusinessException 当查询参数不合法时抛出业务异常
     * @see WfExecutionQueryParam
     */
    Page<WfExecutionDTO> pageMyCc(WfExecutionQueryParam param);

    /**
     * 分页查询补偿中心列表。
     * <p>
     * 查询当前租户下需要人工补偿或可触发超时重试的执行单，
     * 主要覆盖“存在未激活待办实例”与“存在已超时激活实例”两类场景。
     * </p>
     *
     * @param param 查询参数，包含分页、任务名称、任务编码与状态等过滤条件
     * @return 补偿中心执行单分页结果
     */
    Page<WfExecutionDTO> pageCompensationCenter(WfExecutionQueryParam param);

    /**
     * 加载审批工作台摘要。
     * <p>
     * 加载审批工作台首页的摘要数据，包含待办、昨日已办、抄送列表。
     * </p>
     *
     * @return 工作台摘要 VO
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see WfDashboardSummaryVO
     */
    WfDashboardSummaryVO loadDashboardSummary();

    /**
     * 加载审批工作台分析。
     * <p>
     * 加载审批工作台首页的分析数据，包含近 7 日审批趋势、用户审批排行等。
     * </p>
     *
     * @return 工作台分析 VO
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see WfDashboardAnalyticsVO
     */
    WfDashboardAnalyticsVO loadDashboardAnalytics();
}
