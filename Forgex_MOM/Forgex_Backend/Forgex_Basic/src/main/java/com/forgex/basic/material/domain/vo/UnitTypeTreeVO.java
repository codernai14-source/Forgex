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

    private Long id;

    private String unitTypeCode;

    private String unitTypeName;

    private Long parentId;

    private String levelPath;

    private List<UnitTypeTreeVO> children = new ArrayList<>();
}
