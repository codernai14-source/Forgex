package com.forgex.common.domain.dto.table;

import lombok.Data;

import java.time.LocalDateTime;
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
    private Boolean enabled;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}

