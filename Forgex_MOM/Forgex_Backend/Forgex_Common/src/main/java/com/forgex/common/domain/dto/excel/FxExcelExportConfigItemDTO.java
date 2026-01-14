package com.forgex.common.domain.dto.excel;

import lombok.Data;

/**
 * Excel 导出配置子项 DTO。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class FxExcelExportConfigItemDTO {

    private Long id;

    private Long configId;

    private String exportField;

    private String fieldName;

    private String i18nJson;

    private String headerStyleJson;

    private String cellStyleJson;

    private Integer orderNum;
}

