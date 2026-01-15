package com.forgex.common.domain.dto.table;

import lombok.Data;

@Data
public class FxTableQueryFieldDTO {
    private String field;
    private String label;
    private String queryType;
    private String queryOperator;
    private String dictCode;
}

