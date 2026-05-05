package com.forgex.basic.material.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 计量单位类型树节点。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
public class UnitTypeTreeVO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 单位类型编码。
     */
    private String unitTypeCode;

    /**
     * 单位类型名称。
     */
    private String unitTypeName;

    /**
     * 父级 ID。
     */
    private Long parentId;

    /**
     * 等级路径。
     */
    private String levelPath;

    /**
     * 子节点列表。
     */
    private List<UnitTypeTreeVO> children = new ArrayList<>();
}
