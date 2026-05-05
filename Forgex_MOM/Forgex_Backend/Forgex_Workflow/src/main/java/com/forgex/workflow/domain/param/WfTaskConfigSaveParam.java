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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批任务配置保存参数。
 * <p>
 * 用于前端提交审批任务配置的新增/修改请求。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
public class WfTaskConfigSaveParam {

    /**
     * 主键 ID（新增时为空，修改时必填）
     */
    private Long id;

    /**
     * 审批任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /**
     * 任务名称多语言JSON
     */
    private String taskNameI18nJson;

    /**
     * 审批任务编码
     */
    @NotBlank(message = "任务编码不能为空")
    private String taskCode;

    /**
     * 分类编码。
     */
    private String categoryCode;

    /**
     * 审批任务解释器Bean名称
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
     * 表单类型：1=自定义，2=低代码
     */
    @NotNull(message = "表单类型不能为空")
    private Integer formType;

    /**
     * 表单路径（静态表单路由）
     */
    private String formPath;

    /**
     * 表单内容（低代码表单JSON）
     */
    private String formContent;

    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
