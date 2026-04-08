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
package com.forgex.workflow.domain.param;

import lombok.Data;

/**
 * 审批执行查询参数。
 * <p>
 * 用于前端提交审批执行的查询请求。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class WfExecutionQueryParam {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 任务名称（模糊查询）
     */
    private String taskName;

    /**
     * 任务编码（模糊查询）
     */
    private String taskCode;

    /**
     * 状态
     * <p>
     * 0=未审批<br>
     * 1=审批中<br>
     * 2=审批完成<br>
     * 3=驳回
     * </p>
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 我已处理列表：审批操作时间下限（含），格式 {@code yyyy-MM-dd HH:mm:ss}。
     * <p>与 {@link #approveTimeEnd} 同时用于筛选「昨日处理」等场景。</p>
     */
    private String approveTimeBegin;

    /**
     * 我已处理列表：审批操作时间上限（含），格式 {@code yyyy-MM-dd HH:mm:ss}。
     */
    private String approveTimeEnd;
}