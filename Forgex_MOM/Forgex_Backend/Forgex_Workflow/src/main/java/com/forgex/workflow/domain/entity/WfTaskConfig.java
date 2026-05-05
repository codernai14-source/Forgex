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
package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批任务配置实体。
 * <p>
 * 对应表 {@code wf_task_config}，用于维护审批任务的基础配置、表单信息
 * 以及流程消息模板等内容。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task_config")
public class WfTaskConfig extends BaseEntity {

    /**
     * 审批任务名称。
     */
    private String taskName;

    /**
     * 任务名称多语言 JSON。
     * <p>
     * 示例：{"zh-CN":"请假审批","en-US":"Leave Approval"}
     * </p>
     */
    private String taskNameI18nJson;

    /**
     * 审批任务编码。
     * <p>
     * 在租户范围内保持唯一，供业务系统发起流程时使用。
     * </p>
     */
    private String taskCode;

    /**
     * 分类编码。
     */
    private String categoryCode;

    /**
     * 审批任务解释器 Bean 名称。
     * <p>
     * 业务侧可实现 {@code IApprovalInterpreter} 接口，用于扩展流程解释逻辑。
     * </p>
     *
     * @see com.forgex.workflow.service.interpreter.IApprovalInterpreter
     */
    private String interpreterBean;

    /**
     * 审批结束 HTTP 回调地址。
     */
    private String callbackUrl;

    /**
     * 审批结束回调 Bean 名称。
     */
    private String callbackBean;

    /**
     * 表单类型。
     * <p>
     * 1 表示自定义表单，2 表示低代码表单。
     * </p>
     */
    private Integer formType;

    /**
     * 表单路径。
     * <p>
     * 自定义表单场景下，前端根据该路径加载对应页面。
     * </p>
     */
    private String formPath;

    /**
     * 表单内容。
     * <p>
     * 低代码表单场景下保存表单 JSON 配置。
     * </p>
     */
    private String formContent;

    /**
     * 启用状态。
     * <p>
     * 0 表示禁用，1 表示启用。
     * </p>
     */
    private Integer status;

    /**
     * 版本号。
     * <p>
     * 用于并发更新控制。
     * </p>
     */
    private Integer version;

    /**
     * 配置阶段。
     */
    private String configStage;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 审批发起消息模板编码。
     * <p>
     * 用于流程发起时发送通知，支持占位符替换。
     * </p>
     */
    private String startMessageTemplateCode;

    /**
     * 审批通过消息模板编码。
     * <p>
     * 用于审批通过时发送通知，支持占位符替换。
     * </p>
     */
    private String approveMessageTemplateCode;

    /**
     * 审批驳回消息模板编码。
     * <p>
     * 用于审批驳回时发送通知，支持占位符替换。
     * </p>
     */
    private String rejectMessageTemplateCode;

    /**
     * 审批完成消息模板编码。
     * <p>
     * 用于流程结束时发送通知，支持占位符替换。
     * </p>
     */
    private String finishMessageTemplateCode;

    /**
     * 消息跳转基础地址。
     * <p>
     * 一般与执行实例 ID 拼接后生成前端跳转链接。
     * </p>
     */
    private String linkBaseUrl;
}
