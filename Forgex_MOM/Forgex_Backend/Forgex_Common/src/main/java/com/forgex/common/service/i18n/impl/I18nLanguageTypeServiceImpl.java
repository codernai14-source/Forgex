package com.forgex.common.service.i18n.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.enums.FxExcelImportMode;
import com.forgex.common.mapper.i18n.FxI18nLanguageTypeMapper;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.service.i18n.I18nLanguageTypeImportService;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 多语言类型配置服务实现类
 * <p>
 * 提供多语言类型配置的增删改查功能实现。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nLanguageTypeService
 * @see com.forgex.common.domain.entity.i18n.FxI18nLanguageType
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class I18nLanguageTypeServiceImpl implements I18nLanguageTypeService, I18nLanguageTypeImportService {

    private final FxI18nLanguageTypeMapper languageTypeMapper;
    private final ExcelConfigService excelConfigService;
    private final ExcelFileService excelFileService;

    /**
     * 查询启用数据列表。
     *
     * @return 列表数据
     */
    @Override
    public List<FxI18nLanguageType> listEnabled() {
        return languageTypeMapper.selectList(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getEnabled, true)
                .eq(FxI18nLanguageType::getDeleted, false)
                .orderByAsc(FxI18nLanguageType::getOrderNum));
    }

    /**
     * 查询全部数据列表。
     *
     * @return 列表数据
     */
    @Override
    public List<FxI18nLanguageType> listAll() {
        return languageTypeMapper.selectList(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getDeleted, false)
                .orderByAsc(FxI18nLanguageType::getOrderNum));
    }

    /**
     * 获取by语言编码。
     *
     * @param langCode 语言编码
     * @return 处理结果
     */
    @Override
    public FxI18nLanguageType getByLangCode(String langCode) {
        return languageTypeMapper.selectOne(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getLangCode, langCode)
                .eq(FxI18nLanguageType::getDeleted, false)
                .last("limit 1"));
    }

    /**
     * 获取默认。
     *
     * @return 处理结果
     */
    @Override
    public FxI18nLanguageType getDefault() {
        return languageTypeMapper.selectOne(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getIsDefault, true)
                .eq(FxI18nLanguageType::getEnabled, true)
                .eq(FxI18nLanguageType::getDeleted, false)
                .last("limit 1"));
    }

    /**
     * 创建数据。
     *
     * @param languageType 语言类型
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(FxI18nLanguageType languageType) {
        return languageTypeMapper.insert(languageType) > 0;
    }

    /**
     * 更新数据。
     *
     * @param languageType 语言类型
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(FxI18nLanguageType languageType) {
        return languageTypeMapper.updateById(languageType) > 0;
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        FxI18nLanguageType languageType = new FxI18nLanguageType();
        languageType.setId(id);
        languageType.setDeleted(true);
        return languageTypeMapper.updateById(languageType) > 0;
    }

    /**
     * 设置默认汇率类型。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefault(Long id) {
        FxI18nLanguageType target = languageTypeMapper.selectById(id);
        if (target == null) {
            return false;
        }

        // 先将所有语言的isDefault设置为false
        FxI18nLanguageType updateEntity = new FxI18nLanguageType();
        updateEntity.setIsDefault(false);
        languageTypeMapper.update(updateEntity, null);

        // 再将目标语言的isDefault设置为true
        target.setIsDefault(true);
        return languageTypeMapper.updateById(target) > 0;
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
        Page<FxI18nLanguageType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FxI18nLanguageType> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询条件
        if (StringUtils.isNotBlank(langCode)) {
            wrapper.like(FxI18nLanguageType::getLangCode, langCode);
        }
        if (StringUtils.isNotBlank(langName)) {
            wrapper.like(FxI18nLanguageType::getLangName, langName);
        }
        if (enabled != null) {
            wrapper.eq(FxI18nLanguageType::getEnabled, enabled);
        }

        // 未删除且按排序号排序
        wrapper.eq(FxI18nLanguageType::getDeleted, false)
               .orderByAsc(FxI18nLanguageType::getOrderNum);

        return languageTypeMapper.selectPage(page, wrapper);
    }

    /**
     * 获取byID。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public FxI18nLanguageType getById(Long id) {
        return languageTypeMapper.selectById(id);
    }

    /**
     * 导入 Excel 数据。
     *
     * @param file 文件
     * @return 映射结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importExcel(MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", 0);
        result.put("failCount", 0);

        // 获取导入配置
        FxExcelImportConfigDTO config = excelConfigService.getImportConfigByCode("I18nLanguageTypeTable");
        if (config == null) {
            throw new RuntimeException("未找到导入配置，请先配置 I18nLanguageTypeTable 的导入模板");
        }

        // 使用公共方法解析 Excel 文件
        InputStream inputStream = file.getInputStream();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) (List<?>) excelFileService.parseImportFile(config, inputStream, Map.class);

        // 批量导入
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        for (Map<String, Object> data : dataList) {
            try {
                // 获取字段值
                String langCode = (String) data.get("langCode");
                String langName = (String) data.get("langName");
                String langNameEn = (String) data.get("langNameEn");
                String icon = (String) data.get("icon");
                Object orderNumObj = data.get("orderNum");
                Object enabledObj = data.get("enabled");
                Object isDefaultObj = data.get("isDefault");

                // 校验必填字段
                if (StringUtils.isBlank(langCode)) {
                    errorMsg.append("语言代码不能为空; ");
                    failCount++;
                    continue;
                }
                if (StringUtils.isBlank(langName)) {
                    errorMsg.append("语言名称不能为空; ");
                    failCount++;
                    continue;
                }

                // 检查是否已存在
                FxI18nLanguageType existing = languageTypeMapper.selectOne(
                    new LambdaQueryWrapper<FxI18nLanguageType>()
                        .eq(FxI18nLanguageType::getLangCode, langCode)
                        .eq(FxI18nLanguageType::getDeleted, false)
                );

                if (existing != null) {
                    errorMsg.append("语言代码已存在：").append(langCode).append("; ");
                    failCount++;
                    continue;
                }

                // 转换为实体并保存
                FxI18nLanguageType languageType = new FxI18nLanguageType();
                languageType.setLangCode(langCode);
                languageType.setLangName(langName);
                languageType.setLangNameEn(langNameEn);
                languageType.setIcon(icon);
                languageType.setOrderNum(orderNumObj != null ? ((Number) orderNumObj).intValue() : 0);
                languageType.setEnabled(enabledObj != null ? (Boolean) enabledObj : true);
                languageType.setIsDefault(isDefaultObj != null ? (Boolean) isDefaultObj : false);

                if (languageTypeMapper.insert(languageType) > 0) {
                    successCount++;
                } else {
                    failCount++;
                    errorMsg.append("保存失败：").append(langCode).append("; ");
                }
            } catch (Exception e) {
                failCount++;
                errorMsg.append("导入失败：").append(data.get("langCode")).append(" - ").append(e.getMessage()).append("; ");
                log.error("导入语言类型失败", e);
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);

        if (!errorMsg.isEmpty()) {
            result.put("errorMessages", errorMsg.toString());
        }

        return result;
    }

    /**
     * 执行通用导入。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param) {
        return handle(param);
    }

    /**
     * 处理导入数据。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        FxExcelImportMode mode = FxExcelImportMode.parse(param == null ? null : param.getImportMode());
        List<Map<String, Object>> rows = param == null || param.getImportData() == null
                ? Collections.emptyList()
                : param.getImportData().getOrDefault("main", Collections.emptyList());
        FxExcelImportResultDTO result = new FxExcelImportResultDTO();
        result.setTotalCount(rows.size());
        if (mode == FxExcelImportMode.COVER) {
            FxI18nLanguageType update = new FxI18nLanguageType();
            update.setDeleted(true);
            languageTypeMapper.update(update, new LambdaQueryWrapper<FxI18nLanguageType>().eq(FxI18nLanguageType::getDeleted, false));
        }
        for (Map<String, Object> row : rows) {
            try {
                handleLanguageTypeRow(mode, row, result);
            } catch (Exception ex) {
                result.addError(row == null ? "UNKNOWN" : String.valueOf(row.get("langCode")));
            }
        }
        return result;
    }

    /**
     * 按公共导入模式处理语言类型行。
     *
     * @param mode   导入模式
     * @param row    行数据
     * @param result 导入结果
     */
    private void handleLanguageTypeRow(FxExcelImportMode mode, Map<String, Object> row, FxExcelImportResultDTO result) {
        String langCode = toStringValue(row == null ? null : row.get("langCode"));
        if (StringUtils.isBlank(langCode)) {
            result.addError("langCode");
            return;
        }
        FxI18nLanguageType existing = getByLangCode(langCode);
        if (existing == null) {
            if (mode == FxExcelImportMode.UPDATE) {
                result.increaseSkipped();
                return;
            }
            languageTypeMapper.insert(toLanguageType(null, row));
            result.increaseCreated();
            return;
        }
        if (mode == FxExcelImportMode.ADD) {
            result.increaseSkipped();
            return;
        }
        FxI18nLanguageType update = toLanguageType(existing.getId(), row);
        languageTypeMapper.updateById(update);
        result.increaseUpdated();
    }

    /**
     * 转换语言类型实体。
     *
     * @param id  主键 ID
     * @param row 行数据
     * @return 语言类型实体
     */
    private FxI18nLanguageType toLanguageType(Long id, Map<String, Object> row) {
        FxI18nLanguageType languageType = new FxI18nLanguageType();
        languageType.setId(id);
        languageType.setLangCode(toStringValue(row.get("langCode")));
        languageType.setLangName(toStringValue(row.get("langName")));
        languageType.setLangNameEn(toStringValue(row.get("langNameEn")));
        languageType.setIcon(toStringValue(row.get("icon")));
        languageType.setOrderNum(toIntegerValue(row.get("orderNum"), 0));
        languageType.setEnabled(toBooleanValue(row.get("enabled"), true));
        languageType.setIsDefault(toBooleanValue(row.get("isDefault"), false));
        return languageType;
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.isBlank(text) ? null : text;
    }

    private Integer toIntegerValue(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = toStringValue(value);
        return StringUtils.isBlank(text) ? defaultValue : Integer.valueOf(text);
    }

    private Boolean toBooleanValue(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = toStringValue(value);
        if (StringUtils.isBlank(text)) {
            return defaultValue;
        }
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "是".equals(text) || "启用".equals(text) || "默认".equals(text);
    }
}
