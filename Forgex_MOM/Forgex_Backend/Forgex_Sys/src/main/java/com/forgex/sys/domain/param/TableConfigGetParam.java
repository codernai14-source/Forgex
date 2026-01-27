package com.forgex.sys.domain.param;

import lombok.Data;

@Data
public class TableConfigGetParam {
    private String tableCode;
    
    private Long current;
    
    private Long size;
    
    private String tableName;
    
    private String tableType;
    
    private Boolean enabled;
}

