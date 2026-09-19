package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口参数配置信息实体类
 * <p>
 * 对应数据库表：fx_api_param_config
 * 用于存储接口参数的树形结构配置，支持 OBJECT、ARRAY、FIELD 三种节点类型
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_param_config")
public class ApiParamConfig extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口配置表 ID（关联 fx_api_config 表）
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 父节点 ID（树形结构，根节点为 null）
     */
    private Long parentId;

    /**
     * 参数方向：REQUEST-请求，RESPONSE-响应
     */
    private String direction;

    /**
     * 节点类型：OBJECT-集合，ARRAY-数组，FIELD-字段
     */
    private String nodeType;

    /**
     * 字段名称（传参的字段名）
     */
    private String fieldName;

    /**
     * 字段显示名称
     */
    private String fieldLabel;

    /**
     * 字段类型（string, number, boolean, array, object）
     */
    private String fieldType;

    /**
     * 字段完整路径（如：data.user.name）
     */
    private String fieldPath;

    /**
     * 是否必填：0-否，1-是
     */
    private Integer required;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 字典编码（用于字典翻译）
     */
    private String dictCode;

    /**
     * 排序号。
     */
    private Integer orderNum;

    /**
     * 备注信息
     */
    private String remark;
}
