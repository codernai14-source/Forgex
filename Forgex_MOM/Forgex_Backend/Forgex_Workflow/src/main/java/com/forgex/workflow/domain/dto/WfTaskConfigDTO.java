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
package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批任务配置DTO。
 * <p>
 * 用于服务间传输审批任务配置数据。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class WfTaskConfigDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 审批任务名称
     */
    private String taskName;

    /**
     * 任务名称多语言JSON
     */
    private String taskNameI18nJson;

    /**
     * 审批任务编码
     */
    private String taskCode;

    /**
     * 审批任务解释器Bean名称
     */
    private String interpreterBean;

    /**
     * 表单类型：1=自定义，2=低代码
     */
    private Integer formType;

    /**
     * 表单路径
     */
    private String formPath;

    /**
     * 表单内容
     */
    private String formContent;

    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 閰嶇疆闃舵
     */
    private String configStage;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private String updateBy;
}
