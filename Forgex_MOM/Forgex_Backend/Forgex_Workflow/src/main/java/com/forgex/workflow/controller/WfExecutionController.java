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
package com.forgex.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
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
import com.forgex.workflow.enums.WorkflowPromptEnum;
import com.forgex.workflow.service.IWfExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 审批执行控制器。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/wf/execution")
@RequiredArgsConstructor
public class WfExecutionController {

    private final IWfExecutionService executionService;

    /**
     * 发起审批执行
     * <p>
     * 根据任务配置发起一个新的审批流程，创建审批执行实例
     * </p>
     *
     * @param param 审批执行启动参数，包含任务编码、业务数据、发起人信息等（必填）
     * @return 新创建的审批执行 ID
     * @throws I18nBusinessException 当参数校验失败或发起审批失败时抛出业务异常
     * @see IWfExecutionService#startExecution(WfExecutionStartParam) 发起审批服务方法
     * @see WfExecutionStartParam 审批执行启动参数
     */
    @PostMapping("/start")
    @RequirePerm("wf:execution:start")
    public R<Long> startExecution(@Validated @RequestBody WfExecutionStartParam param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, executionService.startExecution(param));
    }

    /**
     * 内部接口：发起审批执行。
     *
     * @param request 发起审批参数
     * @return 执行 ID
     */
    @PostMapping("/internal/start")
    public R<Long> internalStartExecution(@Validated @RequestBody WorkflowExecutionStartRequestDTO request) {
        WfExecutionStartParam param = new WfExecutionStartParam();
        param.setTaskCode(request.getTaskCode());
        param.setFormContent(request.getFormContent());
        param.setSelectedApprovers(request.getSelectedApprovers());
        return R.ok(executionService.startExecution(param));
    }

    /**
     * 审批通过
     * <p>
     * 对当前待办审批任务进行审批通过操作，流转到下一节点或结束流程
     * </p>
     *
     * @param param 审批参数，包含执行 ID、审批意见、审批动作等（必填）
     * @return 是否审批成功
     * @throws I18nBusinessException 当参数校验失败或审批失败时抛出业务异常
     * @see IWfExecutionService#approve(WfExecutionApproveParam) 审批通过服务方法
     * @see WfExecutionApproveParam 审批参数
     */
    @PostMapping("/approve")
    @RequirePerm("wf:execution:approve")
    public R<Boolean> approve(@Validated @RequestBody WfExecutionApproveParam param) {
        return R.ok(WorkflowPromptEnum.WF_APPROVE_SUCCESS, executionService.approve(param));
    }

    /**
     * 审批驳回
     * <p>
     * 对当前待办审批任务进行驳回操作，可驳回到发起人或指定节点
     * </p>
     *
     * @param param 审批参数，包含执行 ID、驳回意见、驳回目标节点等（必填）
     * @return 是否驳回成功
     * @throws I18nBusinessException 当参数校验失败或驳回失败时抛出业务异常
     * @see IWfExecutionService#reject(WfExecutionApproveParam) 审批驳回服务方法
     * @see WfExecutionApproveParam 审批参数
     */
    @PostMapping("/reject")
    @RequirePerm("wf:execution:reject")
    public R<Boolean> reject(@Validated @RequestBody WfExecutionApproveParam param) {
        return R.ok(WorkflowPromptEnum.WF_REJECT_SUCCESS, executionService.reject(param));
    }

    /**
     * 审批转交
     * <p>
     * 将当前待办审批任务转交给其他人处理
     * </p>
     *
     * @param param 转交参数，包含执行 ID、转交目标用户 ID、转交意见等（必填）
     * @return 是否转交成功
     * @throws I18nBusinessException 当参数校验失败或转交失败时抛出业务异常
     * @see IWfExecutionService#transfer(WfExecutionTransferParam) 审批转交服务方法
     * @see WfExecutionTransferParam 转交参数
     */
    @PostMapping("/transfer")
    @RequirePerm("wf:execution:transfer")
    public R<Boolean> transfer(@Validated @RequestBody WfExecutionTransferParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.transfer(param));
    }

    /**
     * 审批加签
     * <p>
     * 在当前审批节点增加审批人，支持前加签和后加签
     * </p>
     *
     * @param param 加签参数，包含执行 ID、加签用户 ID、加签类型（前加签/后加签）、加签意见等（必填）
     * @return 是否加签成功
     * @throws I18nBusinessException 当参数校验失败或加签失败时抛出业务异常
     * @see IWfExecutionService#addSign(WfExecutionAddSignParam) 审批加签服务方法
     * @see WfExecutionAddSignParam 加签参数
     */
    @PostMapping("/addSign")
    @RequirePerm("wf:execution:addSign")
    public R<Boolean> addSign(@Validated @RequestBody WfExecutionAddSignParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.addSign(param));
    }

    /**
     * 批量审批通过
     * <p>
     * 批量审批通过多个待办审批任务
     * </p>
     *
     * @param param 批量审批参数，包含执行 ID 列表、审批意见等（必填）
     * @return 是否批量审批成功
     * @throws I18nBusinessException 当参数校验失败或批量审批失败时抛出业务异常
     * @see IWfExecutionService#batchApprove(WfExecutionBatchApproveParam) 批量审批通过服务方法
     * @see WfExecutionBatchApproveParam 批量审批参数
     */
    @PostMapping("/batch/approve")
    @RequirePerm("wf:execution:approve")
    public R<Boolean> batchApprove(@Validated @RequestBody WfExecutionBatchApproveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.batchApprove(param));
    }

    /**
     * 批量转交
     * <p>
     * 批量转交多个待办审批任务给同一人
     * </p>
     *
     * @param param 批量转交参数，包含执行 ID 列表、转交目标用户 ID、转交意见等（必填）
     * @return 是否批量转交成功
     * @throws I18nBusinessException 当参数校验失败或批量转交失败时抛出业务异常
     * @see IWfExecutionService#batchTransfer(WfExecutionBatchTransferParam) 批量转交服务方法
     * @see WfExecutionBatchTransferParam 批量转交参数
     */
    @PostMapping("/batch/transfer")
    @RequirePerm("wf:execution:transfer")
    public R<Boolean> batchTransfer(@Validated @RequestBody WfExecutionBatchTransferParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.batchTransfer(param));
    }

    /**
     * 批量提醒
     * <p>
     * 批量发送审批提醒通知给多个待办任务的审批人
     * </p>
     *
     * @param param 批量提醒参数，包含执行 ID 列表、提醒方式等（必填）
     * @return 是否批量提醒成功
     * @throws I18nBusinessException 当参数校验失败或批量提醒失败时抛出业务异常
     * @see IWfExecutionService#batchRemind(WfExecutionBatchRemindParam) 批量提醒服务方法
     * @see WfExecutionBatchRemindParam 批量提醒参数
     */
    @PostMapping("/batch/remind")
    @RequirePerm("wf:execution:remind")
    public R<Boolean> batchRemind(@Validated @RequestBody WfExecutionBatchRemindParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.batchRemind(param));
    }

    /**
     * 补偿审批执行
     * <p>
     * 对异常或失败的审批执行进行补偿处理，用于恢复或修正执行状态
     * </p>
     *
     * @param param 补偿参数，包含执行 ID、补偿类型、补偿参数等（必填）
     * @return 是否补偿成功
     * @throws I18nBusinessException 当参数校验失败或补偿失败时抛出业务异常
     * @see IWfExecutionService#compensateExecution(WfExecutionCompensateParam) 补偿审批执行服务方法
     * @see WfExecutionCompensateParam 补偿参数
     */
    @PostMapping("/compensate")
    @RequirePerm("wf:dashboard:view")
    public R<Boolean> compensateExecution(@RequestBody WfExecutionCompensateParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.compensateExecution(param));
    }

    /**
     * 重试超时任务
     * <p>
     * 重试因超时未处理的审批任务，重新触发审批流程
     * </p>
     *
     * @param param 补偿参数，包含执行 ID、重试参数等（必填）
     * @return 是否重试成功
     * @throws I18nBusinessException 当参数校验失败或重试失败时抛出业务异常
     * @see IWfExecutionService#retryTimeoutJobs(WfExecutionCompensateParam) 重试超时任务服务方法
     * @see WfExecutionCompensateParam 补偿参数
     */
    @PostMapping("/timeout/retry")
    @RequirePerm("wf:dashboard:view")
    public R<Boolean> retryTimeoutJobs(@RequestBody WfExecutionCompensateParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.retryTimeoutJobs(param));
    }

    /**
     * 保存审批委托
     * <p>
     * 设置用户的审批委托关系，在用户无法审批时由受托人代为审批
     * </p>
     *
     * @param param 委托保存参数，包含委托人 ID、受托人 ID、委托时间范围等（选填）
     * @return 是否保存成功
     * @throws I18nBusinessException 当参数校验失败或保存失败时抛出业务异常
     * @see IWfExecutionService#saveDelegate(WfExecutionDelegateSaveParam) 保存审批委托服务方法
     * @see WfExecutionDelegateSaveParam 委托保存参数
     */
    @PostMapping("/delegate/save")
    @RequirePerm("wf:execution:approve")
    public R<Boolean> saveDelegate(@Validated @RequestBody WfExecutionDelegateSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.saveDelegate(param));
    }

    /**
     * 取消审批委托
     * <p>
     * 取消用户的审批委托关系
     * </p>
     *
     * @param params 请求参数，包含 delegatorUserId（委托人 ID，必填）
     * @return 是否取消成功
     * @throws I18nBusinessException 当参数无效或取消失败时抛出业务异常
     * @see IWfExecutionService#cancelDelegate(Long) 取消审批委托服务方法
     */
    @PostMapping("/delegate/cancel")
    @RequirePerm("wf:execution:approve")
    public R<Boolean> cancelDelegate(@RequestBody Map<String, Object> params) {
        Long delegatorUserId = Long.valueOf(params.get("delegatorUserId").toString());
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.cancelDelegate(delegatorUserId));
    }

    /**
     * 取消审批执行
     * <p>
     * 取消进行中的审批执行，终止审批流程
     * </p>
     *
     * @param params 请求参数，包含 executionId（审批执行 ID，必填）
     * @return 是否取消成功
     * @throws I18nBusinessException 当参数无效或取消失败时抛出业务异常
     * @see IWfExecutionService#cancelExecution(Long) 取消审批执行服务方法
     */
    @PostMapping("/cancel")
    @RequirePerm("wf:execution:cancel")
    public R<Boolean> cancelExecution(@RequestBody Map<String, Object> params) {
        Long executionId = Long.valueOf(params.get("executionId").toString());
        return R.ok(CommonPrompt.UPDATE_SUCCESS, executionService.cancelExecution(executionId));
    }

    /**
     * 查询审批执行详情
     * <p>
     * 通过审批执行 ID 获取详细的执行信息，包含流程状态、当前节点等
     * </p>
     *
     * @param params 请求参数，包含 executionId（审批执行 ID，必填）
     * @return 审批执行详情，如果不存在返回 NOT_FOUND
     * @throws I18nBusinessException 当参数无效或查询失败时抛出业务异常
     * @see IWfExecutionService#getExecutionDetail(Long) 查询审批执行详情服务方法
     * @see WfExecutionDTO 审批执行数据传输对象
     */
    @PostMapping("/detail")
    public R<WfExecutionDTO> getExecutionDetail(@RequestBody Map<String, Object> params) {
        Long executionId = Long.valueOf(params.get("executionId").toString());
        WfExecutionDTO detail = executionService.getExecutionDetail(executionId);
        return detail == null ? R.fail(CommonPrompt.NOT_FOUND) : R.ok(detail);
    }

    /**
     * 查询审批实例列表
     * <p>
     * 获取审批执行的所有审批实例，包含各节点的审批信息
     * </p>
     *
     * @param params 请求参数，包含 executionId（审批执行 ID，必填）
     * @return 审批实例列表
     * @throws I18nBusinessException 当参数无效或查询失败时抛出业务异常
     * @see IWfExecutionService#listApprovalInstances(Long) 查询审批实例列表服务方法
     * @see WfApprovalInstanceDTO 审批实例数据传输对象
     */
    @PostMapping("/instances")
    public R<List<WfApprovalInstanceDTO>> listApprovalInstances(@RequestBody Map<String, Object> params) {
        Long executionId = Long.valueOf(params.get("executionId").toString());
        return R.ok(executionService.listApprovalInstances(executionId));
    }

    /**
     * 查询审批操作日志列表
     * <p>
     * 获取审批执行的所有操作日志，记录审批历史
     * </p>
     *
     * @param params 请求参数，包含 executionId（审批执行 ID，必填）
     * @return 审批操作日志列表
     * @throws I18nBusinessException 当参数无效或查询失败时抛出业务异常
     * @see IWfExecutionService#listApprovalActionLogs(Long) 查询审批操作日志服务方法
     * @see WfApprovalActionLogDTO 审批操作日志数据传输对象
     */
    @PostMapping("/actions")
    public R<List<WfApprovalActionLogDTO>> listApprovalActionLogs(@RequestBody Map<String, Object> params) {
        Long executionId = Long.valueOf(params.get("executionId").toString());
        return R.ok(executionService.listApprovalActionLogs(executionId));
    }

    /**
     * 分页查询我发起的审批
     * <p>
     * 查询当前用户发起的所有审批执行记录
     * </p>
     *
     * @param param 查询参数，包含页码、页数、筛选条件等（必填）
     * @return 分页结果，包含我发起的审批执行列表
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see IWfExecutionService#pageMyInitiated(WfExecutionQueryParam) 分页查询我发起的审批服务方法
     * @see WfExecutionQueryParam 审批执行查询参数
     */
    @PostMapping("/my/initiated")
    @RequirePerm("wf:myTask:initiated")
    public R<Page<WfExecutionDTO>> pageMyInitiated(@RequestBody WfExecutionQueryParam param) {
        return R.ok(executionService.pageMyInitiated(param));
    }

    /**
     * 分页查询我的待办审批
     * <p>
     * 查询当前用户待处理的所有审批执行记录
     * </p>
     *
     * @param param 查询参数，包含页码、页数、筛选条件等（必填）
     * @return 分页结果，包含我的待办审批列表
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see IWfExecutionService#pageMyPending(WfExecutionQueryParam) 分页查询我的待办审批服务方法
     * @see WfExecutionQueryParam 审批执行查询参数
     */
    @PostMapping("/my/pending")
    @RequirePerm("wf:myTask:pending")
    public R<Page<WfExecutionDTO>> pageMyPending(@RequestBody WfExecutionQueryParam param) {
        return R.ok(executionService.pageMyPending(param));
    }

    /**
     * 分页查询我的已办审批
     * <p>
     * 查询当前用户已处理的所有审批执行记录
     * </p>
     *
     * @param param 查询参数，包含页码、页数、筛选条件等（必填）
     * @return 分页结果，包含我的已办审批列表
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see IWfExecutionService#pageMyProcessed(WfExecutionQueryParam) 分页查询我的已办审批服务方法
     * @see WfExecutionQueryParam 审批执行查询参数
     */
    @PostMapping("/my/processed")
    @RequirePerm("wf:myTask:processed")
    public R<Page<WfExecutionDTO>> pageMyProcessed(@RequestBody WfExecutionQueryParam param) {
        return R.ok(executionService.pageMyProcessed(param));
    }

    /**
     * 分页查询我的抄送审批
     * <p>
     * 查询当前用户被抄送的所有审批执行记录
     * </p>
     *
     * @param param 查询参数，包含页码、页数、筛选条件等（必填）
     * @return 分页结果，包含我的抄送审批列表
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see IWfExecutionService#pageMyCc(WfExecutionQueryParam) 分页查询我的抄送审批服务方法
     * @see WfExecutionQueryParam 审批执行查询参数
     */
    @PostMapping("/my/cc")
    @RequirePerm("wf:dashboard:view")
    public R<Page<WfExecutionDTO>> pageMyCc(@RequestBody WfExecutionQueryParam param) {
        return R.ok(executionService.pageMyCc(param));
    }

    /**
     * 分页查询补偿中心列表。
     * <p>
     * 查询当前租户下需要人工补偿或可触发超时重试的执行单列表，
     * 供三期治理能力中的补偿中心后台页统一使用。
     * </p>
     *
     * @param param 查询参数
     * @return 补偿中心分页结果
     */
    @PostMapping("/compensation/page")
    @RequirePerm("wf:dashboard:view")
    public R<Page<WfExecutionDTO>> pageCompensationCenter(@RequestBody WfExecutionQueryParam param) {
        return R.ok(executionService.pageCompensationCenter(param));
    }

    /**
     * 加载仪表盘汇总数据
     * <p>
     * 获取审批仪表盘的汇总统计数据，包含待办数、已办数、发起数等
     * </p>
     *
     * @return 仪表盘汇总数据
     * @throws I18nBusinessException 当加载失败时抛出业务异常
     * @see IWfExecutionService#loadDashboardSummary() 加载仪表盘汇总数据服务方法
     * @see WfDashboardSummaryVO 仪表盘汇总视图对象
     */
    @PostMapping("/dashboard/summary")
    @RequirePerm("wf:dashboard:view")
    public R<WfDashboardSummaryVO> loadDashboardSummary() {
        return R.ok(executionService.loadDashboardSummary());
    }

    /**
     * 加载仪表盘分析数据
     * <p>
     * 获取审批仪表盘的分析图表数据，包含趋势分析、分布统计等
     * </p>
     *
     * @return 仪表盘分析数据
     * @throws I18nBusinessException 当加载失败时抛出业务异常
     * @see IWfExecutionService#loadDashboardAnalytics() 加载仪表盘分析数据服务方法
     * @see WfDashboardAnalyticsVO 仪表盘分析视图对象
     */
    @PostMapping("/dashboard/analytics")
    @RequirePerm("wf:dashboard:view")
    public R<WfDashboardAnalyticsVO> loadDashboardAnalytics() {
        return R.ok(executionService.loadDashboardAnalytics());
    }
}
