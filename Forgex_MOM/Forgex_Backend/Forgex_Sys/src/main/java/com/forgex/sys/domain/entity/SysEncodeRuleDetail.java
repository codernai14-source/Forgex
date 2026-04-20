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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编码规则明细表实体类
 * <p>
 * 映射表：{@code fx_encode_rule_detail}。用于定义编码规则中各组成部分的详细配置。
 * 支持固定字符、日期段、序列号、自定义表达式等多种类型的编码段配置。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>定义编码段的类型（固定字符、日期、序列号、表达式）</li>
 *   <li>配置编码段的值或生成规则</li>
 *   <li>设置编码段的排序和连接符</li>
 *   <li>支持条件触发（仅在特定条件下生效）</li>
 * </ul>
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-10
 * @see SysEncodeRule 编码规则主表实体
 * @see com.forgex.common.base.BaseEntity 继承的基类
 */
@Data
@TableName("fx_encode_rule_detail")
public class SysEncodeRuleDetail {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户 ID（多租户隔离）
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 规则 ID（关联 fx_encode_rule.id）
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 段序号（从 1 开始，决定各段在编码中的顺序）
     */
    @TableField("segment_order")
    private Integer segmentOrder;

    /**
     * 段类型
     * <p>枚举值：FIXED（固定字符）、DATE（日期）、SERIAL（序列号）、EXPRESSION（表达式）</p>
     */
    @TableField("segment_type")
    private String segmentType;

    /**
     * 段值
     * <p>根据类型不同含义不同：
     * <ul>
     *   <li>FIXED：固定的字符值，如 "SO"</li>
     *   <li>DATE：日期格式，如 "yyyyMM"</li>
     *   <li>SERIAL：序列号长度，如 4 表示 0001-9999</li>
     *   <li>EXPRESSION：自定义表达式</li>
     * </ul>
     * </p>
     */
    @TableField("segment_value")
    private String segmentValue;

    /**
     * 连接符
     * <p>用于连接当前段与下一段，如 "-"、"/" 等，最后一段无效</p>
     */
    @TableField("connector")
    private String connector;

    /**
     * 是否必填：false=可选，true=必填
     */
    @TableField("is_required")
    private Boolean isRequired;

    /**
     * 条件表达式（可选）
     * <p>当满足条件时此段才生效，支持 SpEL 表达式</p>
     */
    @TableField("condition_expression")
    private String conditionExpression;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间（自动填充）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人（自动填充）
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新时间（自动填充）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 更新人（自动填充）
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除：false=未删除，true=已删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
