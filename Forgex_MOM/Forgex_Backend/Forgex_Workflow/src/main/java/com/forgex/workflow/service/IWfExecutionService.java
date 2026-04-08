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
import com.forgex.workflow.domain.dto.WfDashboardSummaryVO;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.domain.param.WfExecutionStartParam;

/**
 * 审批执行 Service 接口。
 * <p>
 * 提供审批执行的业务逻辑接口，包含发起审批、审批处理等功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IWfExecutionService {
    
    /**
     * 发起审批
     * 
     * @param param 发起审批参数
     * @return 执行 ID
     */
    Long startExecution(WfExecutionStartParam param);
    
    /**
     * 审批同意
     * 
     * @param param 审批参数
     * @return 是否成功
     */
    Boolean approve(WfExecutionApproveParam param);
    
    /**
     * 审批驳回
     * 
     * @param param 审批参数
     * @return 是否成功
     */
    Boolean reject(WfExecutionApproveParam param);
    
    /**
     * 撤销审批
     * 
     * @param executionId 执行 ID
     * @return 是否成功
     */
    Boolean cancelExecution(Long executionId);
    
    /**
     * 获取审批详情
     * 
     * @param executionId 执行 ID
     * @return 审批详情
     */
    WfExecutionDTO getExecutionDetail(Long executionId);
    
    /**
     * 分页查询我发起的审批
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    Page<WfExecutionDTO> pageMyInitiated(WfExecutionQueryParam param);
    
    /**
     * 分页查询我待处理的审批
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    Page<WfExecutionDTO> pageMyPending(WfExecutionQueryParam param);
    
    /**
     * 分页查询我已处理的审批
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    Page<WfExecutionDTO> pageMyProcessed(WfExecutionQueryParam param);

    /**
     * 分页查询抄送给我的待阅任务（节点审核类型为抄送）。
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<WfExecutionDTO> pageMyCc(WfExecutionQueryParam param);

    /**
     * 审批工作台首页汇总：待办、昨日已处理、抄送待阅。
     *
     * @return 汇总数据，各列表最多 6 条
     */
    WfDashboardSummaryVO loadDashboardSummary();
}