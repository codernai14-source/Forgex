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

import com.forgex.sys.domain.entity.SysEncodeRuleDetail;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编码规则明细数据传输对象（DTO）
 * <p>
 * 用于编码规则明细业务逻辑层的数据传输，包含明细表的完整配置信息。
 * 支持编码段的类型、值、排序等详细配置。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see SysEncodeRuleDetail 编码规则明细实体
 * @see EncodeRuleDTO 主表 DTO
 */
@Data
public class EncodeRuleDetailDTO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 规则 ID（关联主表）
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
