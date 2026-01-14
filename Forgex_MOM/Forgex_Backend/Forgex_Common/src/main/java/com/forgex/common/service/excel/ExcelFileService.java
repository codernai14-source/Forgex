package com.forgex.common.service.excel;

import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;

import java.util.List;
import java.util.Map;

/**
 * Excel 文件生成服务。
 * <p>
 * 用于：\n
 * 1) 根据导入配置生成“导入模板”；\n
 * 2) 根据导出配置生成“导出文件（xlsx/csv）”。\n
 * </p>
 *
 * <p>
 * 说明：样式字段以 JSON 形式配置，当前实现按“可用优先”原则先支持基础样式与列头文本，
 * 后续可逐步增强样式与字典下拉等能力。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public interface ExcelFileService {

    /**
     * 生成导入模板文件（xlsx）。
     *
     * @param config 导入配置（主+子）
     * @return xlsx 文件字节数组
     */
    byte[] buildImportTemplateXlsx(FxExcelImportConfigDTO config);

    /**
     * 生成导出文件（xlsx/csv）。
     *
     * @param config 导出配置（主+子）
     * @param rows   数据行（每行以字段名->值映射）
     * @return 文件字节数组
     */
    byte[] buildExportFile(FxExcelExportConfigDTO config, List<Map<String, Object>> rows);
}

