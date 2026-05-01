package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 多语言类型配置服务接口
 * <p>
 * 提供多语言类型配置的增删改查功能，用于管理系统支持的语言类型。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.domain.entity.i18n.FxI18nLanguageType
 */
public interface SysI18nLanguageTypeService {

    /**
     * 获取所有启用的语言类型列表
     *
     * @return 启用的语言类型列表
     */
    List<FxI18nLanguageType> listEnabled();

    /**
     * 获取所有语言类型列表
     *
     * @return 所有语言类型列表
     */
    List<FxI18nLanguageType> listAll();

    /**
     * 根据语言代码获取语言类型
     *
     * @param langCode 语言代码
     * @return 语言类型实体
     */
    FxI18nLanguageType getByLangCode(String langCode);

    /**
     * 获取默认语言类型
     *
     * @return 默认语言类型实体
     */
    FxI18nLanguageType getDefault();

    /**
     * 创建语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否创建成功
     */
    Boolean create(FxI18nLanguageType languageType);

    /**
     * 更新语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否更新成功
     */
    Boolean update(FxI18nLanguageType languageType);

    /**
     * 删除语言类型
     *
     * @param id 语言类型ID
     * @return 是否删除成功
     */
    Boolean delete(Long id);

    /**
     * 设置默认语言
     *
     * @param id 语言类型ID
     * @return 是否设置成功
     */
    Boolean setDefault(Long id);

    /**
     * 分页查询语言类型列表
     * <p>
     * 支持按语言代码、语言名称模糊查询，按排序号排序
     * </p>
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param langCode 语言代码（可选，模糊查询）
     * @param langName 语言名称（可选，模糊查询）
     * @param enabled 是否启用（可选）
     * @return 分页结果
     * @see FxI18nLanguageType
     */
    IPage<FxI18nLanguageType> pageQuery(int pageNum, int pageSize, String langCode, String langName, Boolean enabled);

    /**
     * 根据 ID 获取语言类型
     *
     * @param id 语言类型 ID
     * @return 语言类型实体
     * @see FxI18nLanguageType
     */
    FxI18nLanguageType getById(Long id);

    /**
     * 导入 Excel 文件
     * <p>
     * 解析 Excel 文件并批量创建语言类型配置
     * </p>
     *
     * @param file Excel 文件
     * @return 导入结果，包含成功数量和失败数量
     * @throws Exception 导入失败时抛出异常
     */
    Map<String, Object> importExcel(MultipartFile file) throws Exception;

    /**
     * 执行公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param);
}
