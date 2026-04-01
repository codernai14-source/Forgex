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
package com.forgex.workflow.service.interpreter;

/**
 * 审批任务解释器接口。
 * <p>
 * 业务可实现此接口，在审批任务流转时执行自定义逻辑。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @Component
 * public class LeaveApprovalInterpreter implements IApprovalInterpreter {
 *     
 *     @Override
 *     public void onStart(ApprovalContext context) {
 *         // 审批开始时触发，可用于发送通知、记录日志等
 *         log.info("请假审批开始，发起人：{}", context.getInitiatorName());
 *     }
 *     
 *     @Override
 *     public void onApprove(ApprovalContext context) {
 *         // 每个审批节点审批后触发
 *         if (context.getApproveStatus() == 1) {
 *             log.info("审批同意，审批人：{}", context.getApproverName());
 *         }
 *     }
 *     
 *     @Override
 *     public void onReject(ApprovalContext context) {
 *         // 审批驳回时触发
 *         log.info("审批驳回，原因：{}", context.getComment());
 *     }
 *     
 *     @Override
 *     public void onEnd(ApprovalContext context) {
 *         // 审批结束时触发，可用于更新业务数据、发送通知等
 *         if (context.getStatus() == 2) {
 *             log.info("审批完成，更新请假数据");
 *         }
 *     }
 * }
 * }
 * </pre>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IApprovalInterpreter {
    
    /**
     * 任务开始时触发。
     * <p>
     * 在审批任务发起时调用，可用于：
     * <ul>
     *   <li>发送审批通知</li>
     *   <li>记录审批开始日志</li>
     *   <li>执行业务前置操作</li>
     * </ul>
     * </p>
     *
     * @param context 审批上下文
     */
    void onStart(ApprovalContext context);
    
    /**
     * 任务审批时触发。
     * <p>
     * 在每个审批节点审批后调用，可用于：
     * <ul>
     *   <li>记录审批日志</li>
     *   <li>发送审批结果通知</li>
     *   <li>执行业务逻辑</li>
     * </ul>
     * </p>
     *
     * @param context 审批上下文
     */
    void onApprove(ApprovalContext context);
    
    /**
     * 任务驳回时触发。
     * <p>
     * 在审批被驳回时调用，可用于：
     * <ul>
     *   <li>记录驳回原因</li>
     *   <li>发送驳回通知</li>
     *   <li>执行业务回滚操作</li>
     * </ul>
     * </p>
     *
     * @param context 审批上下文
     */
    void onReject(ApprovalContext context);
    
    /**
     * 任务结束时触发。
     * <p>
     * 在审批任务完成或终止时调用，可用于：
     * <ul>
     *   <li>更新业务数据</li>
     *   <li>发送完成通知</li>
     *   <li>清理临时数据</li>
     * </ul>
     * </p>
     *
     * @param context 审批上下文
     */
    void onEnd(ApprovalContext context);
}