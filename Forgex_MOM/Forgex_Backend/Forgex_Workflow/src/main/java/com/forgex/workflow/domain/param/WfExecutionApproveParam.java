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

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批处理参数。
 * <p>
 * 用于前端提交审批同意/驳回请求。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class WfExecutionApproveParam {

    /**
     * 审批执行 ID
     */
    @NotNull(message = "执行 ID 不能为空")
    private Long executionId;

    /**
     * 审批状态
     * <p>
     * 1=同意<br>
     * 2=不同意
     * </p>
     */
    @NotNull(message = "审批状态不能为空")
    private Integer approveStatus;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 驳回类型
     * <p>
     * 1=驳回任务<br>
     * 2=返回上一节点
     * </p>
     * （不同意时必填）
     */
    private Integer rejectType;
}