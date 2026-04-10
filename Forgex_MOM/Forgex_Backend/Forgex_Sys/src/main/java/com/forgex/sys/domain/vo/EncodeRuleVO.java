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
import java.util.List;

/**
 * 编码规则视图对象（VO）
 * <p>
 * 用于向前端展示编码规则数据，包含展示需要的衍生字段和格式化数据。
 * 支持重置周期、启用状态等字段的文本展示。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see EncodeRuleDetailVO 明细 VO
 */
@Data
public class EncodeRuleVO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 规则代码
     */
    private String ruleCode;

    /**
     * 规则名称
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
     * 重置周期文本（字典翻译结果）
     */
    private String resetCycleText;

    /**
     * 当前序列号值
     */
    private Integer currentSerial;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 启用状态文本（字典翻译结果）
     */
    private String isEnabledText;

    /**
     * 排序号
     */
    private Integer sortOrder;

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

    /**
     * 关联的明细规则列表
     */
    private List<EncodeRuleDetailVO> detailList;
}
