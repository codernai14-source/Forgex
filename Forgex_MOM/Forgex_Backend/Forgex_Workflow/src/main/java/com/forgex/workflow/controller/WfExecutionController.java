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
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.workflow.domain.dto.WfDashboardSummaryVO;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.domain.param.WfExecutionStartParam;
import com.forgex.workflow.service.IWfExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 审批执行 Controller。
 * <p>
 * 提供审批执行的 HTTP 接口，包含发起审批、审批处理等功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@RestController
@RequestMapping("/wf/execution")
@RequiredArgsConstructor
public class WfExecutionController {
    
    private final IWfExecutionService executionService;
    
    /**
     * 发起审批
     *
     * @param param 发起审批参数
     * @return 执行 ID
     */
    @PostMapping("/start")
    @RequirePerm("wf:execution:start")
    public R<Long> startExecution(@Validated @RequestBody WfExecutionStartParam param) {
        Long executionId = executionService.startExecution(param);
        return R.ok(CommonPrompt.CREATE_SUCCESS, executionId);
    }
    
    /**
     * 审批同意
     *
     * @param param 审批参数
     * @return 是否成功
     */
    @PostMapping("/approve")
    @RequirePerm("wf:execution:approve")
    public R<Boolean> approve(@Validated @RequestBody WfExecutionApproveParam param) {
        Boolean success = executionService.approve(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }
    
    /**
     * 审批驳回
     *
     * @param param 审批参数
     * @return 是否成功
     */
    @PostMapping("/reject")
    @RequirePerm("wf:execution:reject")
    public R<Boolean> reject(@Validated @RequestBody WfExecutionApproveParam param) {
        Boolean success = executionService.reject(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }
    
    /**
     * 撤销审批
     *
     * @param params 参数（executionId）
     * @return 是否成功
     */
    @PostMapping("/cancel")
    @RequirePerm("wf:execution:cancel")
    public R<Boolean> cancelExecution(@RequestBody Map<String, Object> params) {
        Long executionId = Long.valueOf(params.get("executionId").toString());
        Boolean success = executionService.cancelExecution(executionId);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }
    
    /**
     * 获取审批详情
     *
     * @param params 参数（executionId）
     * @return 审批详情
     */
    @PostMapping("/detail")
    public R<WfExecutionDTO> getExecutionDetail(@RequestBody Map<String, Object> params) {
        Long executionId = Long.valueOf(params.get("executionId").toString());
        WfExecutionDTO detail = executionService.getExecutionDetail(executionId);
        
        if (detail == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }
        
        return R.ok(detail);
    }
    
    /**
     * 分页查询我发起的审批
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/my/initiated")
    @RequirePerm("wf:myTask:initiated")
    public R<Page<WfExecutionDTO>> pageMyInitiated(@RequestBody WfExecutionQueryParam param) {
        Page<WfExecutionDTO> page = executionService.pageMyInitiated(param);
        return R.ok(page);
    }
    
    /**
     * 分页查询我待处理的审批
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/my/pending")
    @RequirePerm("wf:myTask:pending")
    public R<Page<WfExecutionDTO>> pageMyPending(@RequestBody WfExecutionQueryParam param) {
        Page<WfExecutionDTO> page = executionService.pageMyPending(param);
        return R.ok(page);
    }
    
    /**
     * 分页查询我已处理的审批
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/my/processed")
    @RequirePerm("wf:myTask:processed")
    public R<Page<WfExecutionDTO>> pageMyProcessed(@RequestBody WfExecutionQueryParam param) {
        Page<WfExecutionDTO> page = executionService.pageMyProcessed(param);
        return R.ok(page);
    }

    /**
     * 分页查询抄送给我的待阅任务（节点审核类型为抄送且仍处于待处理）。
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/my/cc")
    @RequirePerm("wf:dashboard:view")
    public R<Page<WfExecutionDTO>> pageMyCc(@RequestBody WfExecutionQueryParam param) {
        Page<WfExecutionDTO> page = executionService.pageMyCc(param);
        return R.ok(page);
    }

    /**
     * 审批工作台首页汇总：待办、昨日已处理、抄送待阅。
     *
     * @return 汇总数据
     */
    @PostMapping("/dashboard/summary")
    @RequirePerm("wf:dashboard:view")
    public R<WfDashboardSummaryVO> loadDashboardSummary() {
        WfDashboardSummaryVO summary = executionService.loadDashboardSummary();
        return R.ok(summary);
    }
}
