package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 登录日志导出请求 DTO。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class ExcelLoginLogExportDTO {

    /**
     * 导出配置表编号。
     */
    private String tableCode;

    /**
     * 查询条件。
     */
    private LoginLogQueryDTO query;
}

