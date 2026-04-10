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
package com.forgex.sys.domain.dto;

import com.forgex.sys.domain.entity.SysEncodeRule;
import com.forgex.sys.domain.entity.SysEncodeRuleDetail;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 编码规则数据传输对象（DTO）
 * <p>
 * 用于编码规则业务逻辑层的数据传输，包含主表和明细表的完整信息。
 * 支持一对多关联关系的数据封装。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see SysEncodeRule 编码规则主表实体
 * @see SysEncodeRuleDetail 编码规则明细实体
 * @see EncodeRuleDetailDTO 明细 DTO
 */
@Data
public class EncodeRuleDTO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 规则代码（唯一标识）
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
     * 当前序列号值
     */
    private Integer currentSerial;

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

    /**
     * 关联的明细规则列表
     */
    private List<EncodeRuleDetailDTO> detailList;
}
