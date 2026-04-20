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
import java.util.List;

/**
 * 编码规则保存参数
 * <p>
 * 用于创建或更新编码规则，包含主表和明细表的完整配置信息。
 * 支持新增和修改两种场景（通过 id 字段区分）。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-10
 * @see EncodeRuleDetailSaveParam 明细保存参数
 */
@Data
public class EncodeRuleSaveParam {

    /**
     * 主键 ID（新增时为空，修改时必填）
     */
    private Long id;

    /**
     * 规则代码（唯一标识，必填）
     */
    private String ruleCode;

    /**
     * 规则名称（必填）
     */
    private String ruleName;

    /**
     * 适用模块
     */
    private String module;

    /**
     * 编码前缀
     */
    private String prefix;

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 序列号长度
     */
    private Integer serialLength;

    /**
     * 序列号重置周期
     */
    private String resetCycle;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 关联的明细规则列表
     */
    private List<EncodeRuleDetailSaveParam> detailList;
}
