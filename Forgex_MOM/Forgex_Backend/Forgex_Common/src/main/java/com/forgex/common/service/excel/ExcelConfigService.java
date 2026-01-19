package com.forgex.common.service.excel;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;

/**
 * Excel 导入导出配置服务。
 * <p>
 * 配置存储在 {@code forgex_common} 库，采用主子结构管理导入模板/导出字段及样式。\n
 * 该服务供 Sys 模块配置管理页面与“下载模板/导出”能力复用。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public interface ExcelConfigService {

    /**
     * 分页查询导出配置列表。
     *
     * @param page     分页参数
     * @param tableName 表名（可选，模糊匹配）
     * @param tableCode 表编号（可选，模糊匹配）
     * @return 分页结果
     */
    IPage<FxExcelExportConfigDTO> pageExportConfig(Page<FxExcelExportConfigDTO> page, String tableName, String tableCode);

    /**
     * 获取导出配置（包含子项）。
     *
     * @param id 主表ID
     * @return 配置DTO
     */
    FxExcelExportConfigDTO getExportConfig(Long id);

    /**
     * 根据表编号获取导出配置（包含子项）。
     *
     * @param tableCode 表编号
     * @return 配置DTO
     */
    FxExcelExportConfigDTO getExportConfigByCode(String tableCode);

    /**
     * 保存导出配置（主 + 子，含新增/更新）。
     *
     * @param dto 配置DTO
     * @return 主表ID
     */
    Long saveExportConfig(FxExcelExportConfigDTO dto);

    /**
     * 删除导出配置（主 + 子）。
     *
     * @param id 主表ID
     * @return 是否成功
     */
    Boolean deleteExportConfig(Long id);

    /**
     * 分页查询导入配置列表。
     *
     * @param page     分页参数
     * @param tableName 表名（可选，模糊匹配）
     * @param tableCode 表编号（可选，模糊匹配）
     * @return 分页结果
     */
    IPage<FxExcelImportConfigDTO> pageImportConfig(Page<FxExcelImportConfigDTO> page, String tableName, String tableCode);

    /**
     * 获取导入配置（包含子项）。
     *
     * @param id 主表ID
     * @return 配置DTO
     */
    FxExcelImportConfigDTO getImportConfig(Long id);

    /**
     * 根据表编号获取导入配置（包含子项）。
     *
     * @param tableCode 表编号
     * @return 配置DTO
     */
    FxExcelImportConfigDTO getImportConfigByCode(String tableCode);

    /**
     * 保存导入配置（主 + 子，含新增/更新）。
     *
     * @param dto 配置DTO
     * @return 主表ID
     */
    Long saveImportConfig(FxExcelImportConfigDTO dto);

    /**
     * 删除导入配置（主 + 子）。
     *
     * @param id 主表ID
     * @return 是否成功
     */
    Boolean deleteImportConfig(Long id);
}

