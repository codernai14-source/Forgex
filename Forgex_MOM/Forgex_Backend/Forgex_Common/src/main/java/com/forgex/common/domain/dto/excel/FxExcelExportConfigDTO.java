package com.forgex.common.domain.dto.excel;

import lombok.Data;

import java.util.List;

/**
 * Excel 导出配置 DTO（主 + 子）。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class FxExcelExportConfigDTO {

    private Long id;

    private String tableName;

    private String tableCode;

    private String headerStyleJson;

    private String title;

    private String subtitle;

    private String exportFormat;

    private Boolean enableTotal;

    private Integer version;

    private List<FxExcelExportConfigItemDTO> items;
}

