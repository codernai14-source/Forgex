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
 * 请假审批解释器示例。
 * <p>
 * 演示如何实现自定义的审批解释器。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Component("leaveApprovalInterpreter")
public class LeaveApprovalInterpreter implements IApprovalInterpreter {
    
    @Override
    public void onStart(ApprovalContext context) {
        log.info("请假审批开始，发起人：{}，任务名称：{}", 
                context.getInitiatorName(), context.getTaskName());
        
        // TODO: 可以在此处发送审批开始通知
        // 例如：发送邮件、短信或企业微信消息给审批人
    }
    
    @Override
    public void onApprove(ApprovalContext context) {
        log.info("请假审批通过，审批人：{}，审批意见：{}", 
                context.getApproverName(), context.getComment());
        
        // TODO: 可以在此处记录审批日志或发送通知
    }
    
    @Override
    public void onReject(ApprovalContext context) {
        log.info("请假审批驳回，审批人：{}，驳回原因：{}", 
                context.getApproverName(), context.getComment());
        
        // TODO: 可以在此处发送驳回通知给发起人
    }
    
    @Override
    public void onEnd(ApprovalContext context) {
        if (context.getStatus() == 2) {
            log.info("请假审批完成，发起人：{}", context.getInitiatorName());
            
            // TODO: 可以在此处更新请假数据
            // 例如：更新请假状态、扣减假期余额等
        } else if (context.getStatus() == 3) {
            log.info("请假审批被驳回，发起人：{}", context.getInitiatorName());
        }
    }
}