package com.forgex.integration.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口参数树形结构 VO
 * <p>
 * 用于展示接口参数的树形结构，包含子节点列表
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ApiParamTreeVO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 接口配置表 ID（关联 fx_api_config 表）
     */
    private Long apiConfigId;

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
     * 排序号
     */
    private Integer orderNum;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 子节点列表
     */
    private List<ApiParamTreeVO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
