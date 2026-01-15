package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("fx_table_config")
public class FxTableConfig extends BaseEntity {
    private String tableCode;
    private String tableNameI18nJson;
    private String tableType;
    private String rowKey;
    private Integer defaultPageSize;
    private String defaultSortJson;
    private Boolean enabled;
    private Integer version;
}

