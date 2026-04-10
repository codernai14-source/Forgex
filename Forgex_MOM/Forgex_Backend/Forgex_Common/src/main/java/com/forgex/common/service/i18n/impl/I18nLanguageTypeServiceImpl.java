package com.forgex.common.service.i18n.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.mapper.i18n.FxI18nLanguageTypeMapper;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
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
public class I18nLanguageTypeServiceImpl implements I18nLanguageTypeService {

    private final FxI18nLanguageTypeMapper languageTypeMapper;
    private final ExcelConfigService excelConfigService;
    private final ExcelFileService excelFileService;

    @Override
    public List<FxI18nLanguageType> listEnabled() {
        return languageTypeMapper.selectList(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getEnabled, true)
                .eq(FxI18nLanguageType::getDeleted, false)
                .orderByAsc(FxI18nLanguageType::getOrderNum));
    }

    @Override
    public List<FxI18nLanguageType> listAll() {
        return languageTypeMapper.selectList(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getDeleted, false)
                .orderByAsc(FxI18nLanguageType::getOrderNum));
    }

    @Override
    public FxI18nLanguageType getByLangCode(String langCode) {
        return languageTypeMapper.selectOne(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getLangCode, langCode)
                .eq(FxI18nLanguageType::getDeleted, false)
                .last("limit 1"));
    }

    @Override
    public FxI18nLanguageType getDefault() {
        return languageTypeMapper.selectOne(new LambdaQueryWrapper<FxI18nLanguageType>()
                .eq(FxI18nLanguageType::getIsDefault, true)
                .eq(FxI18nLanguageType::getEnabled, true)
                .eq(FxI18nLanguageType::getDeleted, false)
                .last("limit 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(FxI18nLanguageType languageType) {
        return languageTypeMapper.insert(languageType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(FxI18nLanguageType languageType) {
        return languageTypeMapper.updateById(languageType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        FxI18nLanguageType languageType = new FxI18nLanguageType();
        languageType.setId(id);
        languageType.setDeleted(true);
        return languageTypeMapper.updateById(languageType) > 0;
    }

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

    @Override
    public FxI18nLanguageType getById(Long id) {
        return languageTypeMapper.selectById(id);
    }

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
}
