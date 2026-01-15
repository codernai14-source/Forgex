package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("fx_table_column_config")
public class FxTableColumnConfig extends BaseEntity {
    private String tableCode;
    private String field;
    private String titleI18nJson;
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
    private Integer orderNum;
    private Boolean enabled;
}

