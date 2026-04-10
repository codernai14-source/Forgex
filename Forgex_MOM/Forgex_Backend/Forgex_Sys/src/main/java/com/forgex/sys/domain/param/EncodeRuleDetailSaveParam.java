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
package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 编码规则明细保存参数
 * <p>
 * 用于创建或更新编码规则明细，包含编码段的详细配置信息。
 * 支持固定字符、日期、序列号、表达式等多种段类型。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see EncodeRuleSaveParam 主表保存参数
 */
@Data
public class EncodeRuleDetailSaveParam {

    /**
     * 主键 ID（新增时为空，修改时必填）
     */
    private Long id;

    /**
     * 规则 ID（关联主表）
     */
    private Long ruleId;

    /**
     * 段序号
     */
    private Integer segmentOrder;

    /**
     * 段类型
     */
    private String segmentType;

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
     * 条件表达式
     */
    private String conditionExpression;

    /**
     * 备注说明
     */
    private String remark;
}
