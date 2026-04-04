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
package com.forgex.workflow.service.interpreter.impl;

import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 空审批解释器实现。
 * <p>
 * 当审批任务不需要自定义逻辑时使用。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Component("emptyApprovalInterpreter")
public class EmptyApprovalInterpreter implements IApprovalInterpreter {
    
    @Override
    public void onStart(ApprovalContext context) {
        log.debug("审批开始：taskCode={}, executionId={}", context.getTaskCode(), context.getExecutionId());
    }
    
    @Override
    public void onApprove(ApprovalContext context) {
        log.debug("审批通过：executionId={}, nodeId={}, approver={}", 
                context.getExecutionId(), context.getCurrentNodeId(), context.getApproverName());
    }
    
    @Override
    public void onReject(ApprovalContext context) {
        log.debug("审批驳回：executionId={}, nodeId={}, reason={}", 
                context.getExecutionId(), context.getCurrentNodeId(), context.getComment());
    }
    
    @Override
    public void onEnd(ApprovalContext context) {
        log.debug("审批结束：executionId={}, status={}", context.getExecutionId(), context.getStatus());
    }
}