package com.forgex.sys.domain.dto;

import lombok.Data;

@Data
public class ExcelOperationLogExportDTO {

    private String tableCode;

    private SysOperationLogQueryDTO query;
}
