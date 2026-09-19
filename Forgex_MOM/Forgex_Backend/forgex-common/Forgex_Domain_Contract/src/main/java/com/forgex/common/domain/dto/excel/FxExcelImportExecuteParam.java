package com.forgex.common.domain.dto.excel;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Excel 公共导入执行参数。
 * <p>
 * 前端完成文件解析和基础校验后，将导入模式、导入配置和按 Sheet 分组的数据传给公共导入接口。
 * 公共接口再根据配置中的处理器 Bean 名称分发给具体业务处理器。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Data
public class FxExcelImportExecuteParam {

    /**
     * 表编码。
     */
    private String tableCode;

    /**
     * 导入模式，取值：ADD、UPDATE、COVER。
     */
    private String importMode;

    /**
     * 导入配置。
     */
    private FxExcelImportConfigDTO importConfig;

    /**
     * 导入数据。
     * <p>
     * Key 为 Sheet 编码，Value 为该 Sheet 下的行数据；单 Sheet 默认使用 {@code main}。
     * </p>
     */
    private Map<String, List<Map<String, Object>>> importData;
}
