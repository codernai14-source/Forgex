package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("fx_user_table_pref")
public class FxUserTablePref extends BaseEntity {
    private Long userId;
    private String tableCode;
    private String hiddenFieldsJson;
    private String fieldOrderJson;
    private Integer pageSize;
    private String sortJson;
}

