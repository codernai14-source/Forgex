package com.forgex.common.domain.dto.table;

import lombok.Data;

@Data
public class FxTableColumnDTO {
    private String field;
    private String title;
    private String align;
    private Integer width;
    private String fixed;
    private Boolean ellipsis;
    private Boolean sortable;
    private String sorterField;
    private Boolean queryable;
    private String queryType;
    private String queryOperator;
    private String dictCode;
    private String renderType;
    private String permKey;
}

