package com.forgex.common.domain.dto.excel;

import lombok.Data;

/**
 * Excel 导入配置子项 DTO。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class FxExcelImportConfigItemDTO {

    private Long id;

    private Long configId;

    private String i18nJson;

    private String importField;

    private String fieldType;

    private String dictCode;

    private Boolean required;

    private Integer orderNum;
}

