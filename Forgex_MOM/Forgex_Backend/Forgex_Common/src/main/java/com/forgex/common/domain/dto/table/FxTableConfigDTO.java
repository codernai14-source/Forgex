package com.forgex.common.domain.dto.table;

import lombok.Data;

import java.util.List;

@Data
public class FxTableConfigDTO {
    private String tableCode;
    private String tableName;
    private String tableType;
    private String rowKey;
    private Integer defaultPageSize;
    private String defaultSortJson;
    private List<FxTableColumnDTO> columns;
    private List<FxTableQueryFieldDTO> queryFields;
    private Integer version;
}

