package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 计量单位换算请求 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
public class UnitConvertRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待换算实体数据，Feign 调用时以 Map 承载。
     */
    private Map<String, Object> entity = new LinkedHashMap<>();

    /**
     * 目标计量单位编码。
     */
    private String targetUnitCode;

    /**
     * 实体内计量单位字段名。
     */
    private String unitFieldName;

    /**
     * 单位字段类型：ID 或 CODE。
     */
    private String unitFieldType;

    /**
     * 需要换算的数字字段名数组。
     */
    private List<String> targetFieldNames = new ArrayList<>();
}
