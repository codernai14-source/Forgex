package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口参数映射信息实体类
 * <p>
 * 对应数据库表：fx_api_param_mapping
 * 用于存储接口参数映射关系，支持源字段到目标字段的转换
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_param_mapping")
public class ApiParamMapping extends BaseEntity {

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
     * 源字段路径
     */
    private String sourceFieldPath;

    /**
     * 目标字段路径
     */
    private String targetFieldPath;

    /**
     * 转换规则（JSON 表达式或函数名）
     * 示例：toUpperCase(), formatDate('YYYY-MM-DD')
     */
    private String transformRule;

    /**
     * 映射方向：INBOUND-外对内，OUTBOUND-内调外
     */
    private String direction;

    /**
     * 备注信息
     */
    private String remark;
}
