package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 用户导出请求 DTO。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class ExcelUserExportDTO {

    /**
     * 导出配置表编号。
     */
    private String tableCode;

    /**
     * 查询条件。
     */
    private SysUserQueryDTO query;
}

