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
package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编码规则明细视图对象（VO）
 * <p>
 * 用于向前端展示编码规则明细数据，包含段类型、段值等配置信息的展示。
 * 支持段类型等字段的文本展示。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-10
 * @see EncodeRuleVO 主表 VO
 */
@Data
public class EncodeRuleDetailVO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 规则 ID
     */
    private Long ruleId;

    /**
     * 段序号
     */
    private Integer segmentOrder;

    /**
     * 段类型（FIXED、DATE、SERIAL、EXPRESSION）
     */
    private String segmentType;

    /**
     * 段类型文本（字典翻译结果）
     */
    private String segmentTypeText;

    /**
     * 段值
     */
    private String segmentValue;

    /**
     * 连接符
     */
    private String connector;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 必填文本（字典翻译结果）
     */
    private String isRequiredText;

    /**
     * 条件表达式
     */
    private String conditionExpression;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private String updateBy;
}
