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
 * 映射表：{@code wf_task_config}。用于配置审批任务的基础信息。
 * </p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>{@code taskName} 审批任务名称</li>
 *   <li>{@code taskNameI18nJson} 任务名称多语言JSON</li>
 *   <li>{@code taskCode} 审批任务编码（租户内唯一）</li>
 *   <li>{@code interpreterBean} 审批任务解释器Bean名称</li>
 *   <li>{@code formType} 表单类型：1=自定义，2=低代码</li>
 *   <li>{@code formPath} 表单路径（静态表单路由）</li>
 *   <li>{@code formContent} 表单内容（低代码表单JSON）</li>
 *   <li>{@code status} 状态：0=禁用，1=启用</li>
 *   <li>{@code version} 版本号</li>
 *   <li>{@code remark} 备注</li>
 * </ul>
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
     * 审批任务名称
     */
    private String taskName;

    /**
     * 任务名称多语言JSON
     * <p>
     * 格式：{"zh-CN": "中文", "en-US": "English", ...}
     * </p>
     */
    private String taskNameI18nJson;

    /**
     * 审批任务编码
     * <p>
     * 租户内唯一，用于业务系统调用
     * </p>
     */
    private String taskCode;

    /**
     * 审批任务解释器Bean名称
     * <p>
     * 业务可实现 IApprovalInterpreter 接口，在审批流转时执行自定义逻辑
     * </p>
     *
     * @see com.forgex.workflow.service.interpreter.IApprovalInterpreter
     */
    private String interpreterBean;

    /**
     * 表单类型
     * <p>
     * 1=自定义表单（配置表单路由）<br>
     * 2=低代码表单（配置表单JSON）
     * </p>
     */
    private Integer formType;

    /**
     * 表单路径
     * <p>
     * 静态表单路由，前端根据此路径加载对应页面
     * </p>
     */
    private String formPath;

    /**
     * 表单内容
     * <p>
     * 低代码表单JSON，前端使用表单引擎渲染
     * </p>
     */
    private String formContent;

    /**
     * 状态
     * <p>
     * 0=禁用<br>
     * 1=启用
     * </p>
     */
    private Integer status;

    /**
     * 版本号
     * <p>
     * 用于乐观锁控制
     * </p>
     */
    private Integer version;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审批开始消息模板编码
     * <p>
     * 用于审批开始时发送通知，支持 ${initiatorName}、${taskName}、${formContent} 等占位符
     * </p>
     */
    private String startMessageTemplateCode;

    /**
     * 审批通过消息模板编码
     * <p>
     * 用于审批通过时发送通知，支持 ${approverName}、${taskName}、${comment} 等占位符
     * </p>
     */
    private String approveMessageTemplateCode;

    /**
     * 审批驳回消息模板编码
     * <p>
     * 用于审批驳回时发送通知，支持 ${approverName}、${taskName}、${rejectReason} 等占位符
     * </p>
     */
    private String rejectMessageTemplateCode;

    /**
     * 审批完成消息模板编码
     * <p>
     * 用于审批完成时发送通知，支持 ${initiatorName}、${taskName}、${status} 等占位符
     * </p>
     */
    private String finishMessageTemplateCode;

    /**
     * 消息链接基础 URL
     * <p>
     * 用于生成消息跳转链接，如：http://localhost:5173/approval/detail/
     * 最终链接 = linkBaseUrl + executionId
     * </p>
     */
    private String linkBaseUrl;
}