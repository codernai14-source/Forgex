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
import java.util.List;

/**
 * 编码规则主表实体类
 * <p>
 * 映射表：{@code fx_encode_rule}。用于定义和管理系统各类单据、业务对象的编码生成规则。
 * 支持自定义编码前缀、日期格式、序列号长度、重置周期等配置。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>定义编码规则的基本信息（规则代码、规则名称、适用模块）</li>
 *   <li>配置编码格式（前缀、日期格式、序列号长度）</li>
 *   <li>设置序列号重置周期（每日、每月、每年、从不）</li>
 *   <li>管理规则启用状态和排序</li>
 *   <li>支持一对多关联明细规则</li>
 * </ul>
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see SysEncodeRuleDetail 编码规则明细实体
 * @see com.forgex.common.base.BaseEntity 继承的基类
 */
@Data
@TableName("fx_encode_rule")
public class SysEncodeRule {

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
     * 规则代码（唯一标识，用于业务调用）
     * <p>示例：SALE_ORDER、PURCHASE_ORDER</p>
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 规则名称（中文描述）
     * <p>示例：销售订单编码规则、采购订单编码规则</p>
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 适用模块
     * <p>示例：SYS（系统）、OMS（订单）、WMS（仓储）</p>
     */
    @TableField("module")
    private String module;

    /**
     * 编码前缀
     * <p>示例：SO（销售订单）、PO（采购订单）</p>
     */
    @TableField("prefix")
    private String prefix;

    /**
     * 日期格式
     * <p>示例：yyyyMMdd、yyyyMM、yyyy、空（不使用日期）</p>
     */
    @TableField("date_format")
    private String dateFormat;

    /**
     * 序列号长度（不含前缀和日期）
     * <p>示例：4 表示 0001-9999</p>
     */
    @TableField("serial_length")
    private Integer serialLength;

    /**
     * 序列号重置周期
     * <p>枚举值：DAILY（每日）、MONTHLY（每月）、YEARLY（每年）、NEVER（从不）</p>
     */
    @TableField("reset_cycle")
    private String resetCycle;

    /**
     * 当前序列号值（用于 Redis 缓存同步）
     */
    @TableField("current_serial")
    private Integer currentSerial;

    /**
     * 是否启用：false=禁用，true=启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /**
     * 排序号（数值越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder;

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

    /**
     * 关联的明细规则列表（非持久化字段，用于关联查询）
     */
    @TableField(exist = false)
    private List<SysEncodeRuleDetail> detailList;
}
