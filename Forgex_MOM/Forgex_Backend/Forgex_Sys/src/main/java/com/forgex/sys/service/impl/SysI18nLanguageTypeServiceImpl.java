package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.service.i18n.I18nLanguageTypeImportService;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import com.forgex.sys.service.SysI18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 多语言类型配置服务实现类
 * <p>
 * 提供多语言类型配置的增删改查功能实现，调用 common 模块的服务。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.service.SysI18nLanguageTypeService
 * @see com.forgex.common.service.i18n.I18nLanguageTypeService
 */
@Service
@RequiredArgsConstructor
public class SysI18nLanguageTypeServiceImpl implements SysI18nLanguageTypeService {

    private final I18nLanguageTypeService i18nLanguageTypeService;
    private final I18nLanguageTypeImportService i18nLanguageTypeImportService;

    /**
     * 查询启用数据列表。
     *
     * @return 列表数据
     */
    @Override
    public List<FxI18nLanguageType> listEnabled() {
        return i18nLanguageTypeService.listEnabled();
    }

    /**
     * 查询全部数据列表。
     *
     * @return 列表数据
     */
    @Override
    public List<FxI18nLanguageType> listAll() {
        return i18nLanguageTypeService.listAll();
    }

    /**
     * 获取by语言编码。
     *
     * @param langCode 语言编码
     * @return 处理结果
     */
    @Override
    public FxI18nLanguageType getByLangCode(String langCode) {
        return i18nLanguageTypeService.getByLangCode(langCode);
    }

    /**
     * 获取默认。
     *
     * @return 处理结果
     */
    @Override
    public FxI18nLanguageType getDefault() {
        return i18nLanguageTypeService.getDefault();
    }

    /**
     * 创建数据。
     *
     * @param languageType 语言类型
     * @return 是否处理成功
     */
    @Override
    public Boolean create(FxI18nLanguageType languageType) {
        return i18nLanguageTypeService.create(languageType);
    }

    /**
     * 更新数据。
     *
     * @param languageType 语言类型
     * @return 是否处理成功
     */
    @Override
    public Boolean update(FxI18nLanguageType languageType) {
        return i18nLanguageTypeService.update(languageType);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    public Boolean delete(Long id) {
        return i18nLanguageTypeService.delete(id);
    }

    /**
     * 设置默认汇率类型。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    public Boolean setDefault(Long id) {
        return i18nLanguageTypeService.setDefault(id);
    }

    /**
     * 分页查询数据。
     *
     * @param pageNum 分页数字
     * @param pageSize 分页size
     * @param langCode 语言编码
     * @param langName 语言名称
     * @param enabled 启用
     * @return 处理结果
     */
    @Override
    public IPage<FxI18nLanguageType> pageQuery(int pageNum, int pageSize, String langCode, String langName, Boolean enabled) {
        return i18nLanguageTypeService.pageQuery(pageNum, pageSize, langCode, langName, enabled);
    }

    /**
     * 获取byID。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public FxI18nLanguageType getById(Long id) {
        return i18nLanguageTypeService.getById(id);
    }

    /**
     * 导入 Excel 数据。
     *
     * @param file 文件
     * @return 映射结果
     */
    @Override
    public Map<String, Object> importExcel(MultipartFile file) throws Exception {
        return i18nLanguageTypeService.importExcel(file);
    }

    /**
     * 执行通用导入。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Override
    public FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param) {
        return i18nLanguageTypeImportService.executeCommonImport(param);
    }
}
