package com.forgex.sys.service.impl;

import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import com.forgex.sys.service.SysI18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<FxI18nLanguageType> listEnabled() {
        return i18nLanguageTypeService.listEnabled();
    }

    @Override
    public List<FxI18nLanguageType> listAll() {
        return i18nLanguageTypeService.listAll();
    }

    @Override
    public FxI18nLanguageType getByLangCode(String langCode) {
        return i18nLanguageTypeService.getByLangCode(langCode);
    }

    @Override
    public FxI18nLanguageType getDefault() {
        return i18nLanguageTypeService.getDefault();
    }

    @Override
    public Boolean create(FxI18nLanguageType languageType) {
        return i18nLanguageTypeService.create(languageType);
    }

    @Override
    public Boolean update(FxI18nLanguageType languageType) {
        return i18nLanguageTypeService.update(languageType);
    }

    @Override
    public Boolean delete(Long id) {
        return i18nLanguageTypeService.delete(id);
    }

    @Override
    public Boolean setDefault(Long id) {
        return i18nLanguageTypeService.setDefault(id);
    }
}
