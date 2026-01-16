package com.forgex.common.service.i18n.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.mapper.i18n.FxI18nLanguageTypeMapper;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
@Service
@RequiredArgsConstructor
public class I18nLanguageTypeServiceImpl implements I18nLanguageTypeService {

    private final FxI18nLanguageTypeMapper languageTypeMapper;

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
}
