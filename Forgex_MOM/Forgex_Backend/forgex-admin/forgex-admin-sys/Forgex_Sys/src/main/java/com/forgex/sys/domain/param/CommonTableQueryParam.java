package com.forgex.sys.domain.param;

import lombok.Data;

import java.util.Map;

@Data
public class CommonTableQueryParam {
    private String tableCode;
    private Long current;
    private Long size;
    private Map<String, Object> query;
    private Map<String, Object> sorter;
}

