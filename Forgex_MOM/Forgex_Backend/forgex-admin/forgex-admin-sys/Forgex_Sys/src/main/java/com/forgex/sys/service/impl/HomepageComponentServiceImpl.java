package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.entity.SysHomepageComponentCategory;
import com.forgex.sys.domain.entity.SysHomepageComponentConfig;
import com.forgex.sys.domain.entity.SysHomepageComponentPreference;
import com.forgex.sys.domain.param.HomepageComponentPreferenceParam;
import com.forgex.sys.domain.param.HomepageComponentPullParam;
import com.forgex.sys.domain.param.HomepageComponentQueryParam;
import com.forgex.sys.domain.param.HomepageComponentSaveParam;
import com.forgex.sys.domain.vo.HomepageComponentVO;
import com.forgex.sys.mapper.SysHomepageComponentCategoryMapper;
import com.forgex.sys.mapper.SysHomepageComponentConfigMapper;
import com.forgex.sys.mapper.SysHomepageComponentPreferenceMapper;
import com.forgex.sys.service.HomepageComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 首页组件目录服务实现。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Service
@RequiredArgsConstructor
public class HomepageComponentServiceImpl implements HomepageComponentService {

    private static final String SCOPE_PUBLIC = "PUBLIC";
    private static final String SCOPE_TENANT = "TENANT";
    private static final String SCOPE_USER = "USER";
    private static final String DEFAULT_MODULE_CODE = "personal";
    private static final String DEFAULT_CATEGORY_CODE = "personal_common";
    private static final String DEFAULT_CATEGORY_NAME = "通用组件";

    private final SysHomepageComponentCategoryMapper categoryMapper;
    private final SysHomepageComponentConfigMapper configMapper;
    private final SysHomepageComponentPreferenceMapper preferenceMapper;

    @Override
    public IPage<HomepageComponentVO> pageComponents(Page<SysHomepageComponentConfig> page, HomepageComponentQueryParam param, Long tenantId) {
        HomepageComponentQueryParam condition = param == null ? new HomepageComponentQueryParam() : param;
        String scopeLevel = normalizeConfigScope(condition.getScopeLevel());
        Long scopeTenantId = SCOPE_PUBLIC.equals(scopeLevel) ? 0L : safeTenantId(tenantId);
        LambdaQueryWrapper<SysHomepageComponentConfig> wrapper = buildConfigWrapper(condition, scopeLevel, scopeTenantId);
        return configMapper.selectPage(page, wrapper).convert(config -> toConfigVO(config, scopeLevel));
    }

    @Override
    public List<HomepageComponentVO> listEffectiveComponents(Long userId, Long tenantId, HomepageComponentQueryParam param) {
        HomepageComponentQueryParam condition = param == null ? new HomepageComponentQueryParam() : param;
        Long currentTenantId = safeTenantId(tenantId);
        String moduleCode = normalizeModuleCode(condition.getModuleCode());
        List<SysHomepageComponentPreference> preferences = listPreferences(userId, currentTenantId, moduleCode, condition.getCategoryId());
        if (!preferences.isEmpty()) {
            return preferences.stream()
                .map(this::toPreferenceVO)
                .filter(item -> matchesQuery(item, condition))
                .filter(item -> matchesScope(item.getScopeLevel(), condition.getScopeLevel()))
                .sorted(componentComparator())
                .collect(Collectors.toList());
        }
        return listBaseEffectiveConfigs(currentTenantId, condition).stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .map(config -> toConfigVO(config, config.getScopeLevel()))
            .filter(item -> matchesScope(item.getScopeLevel(), condition.getScopeLevel()))
            .sorted(componentComparator())
            .collect(Collectors.toList());
    }

    @Override
    public List<HomepageComponentVO> listPersonalComponents(Long userId, Long tenantId, HomepageComponentQueryParam param) {
        return listEffectiveComponents(userId, tenantId, param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComponent(HomepageComponentSaveParam param, Long tenantId) {
        if (param == null || !StringUtils.hasText(param.getComponentCode()) || !StringUtils.hasText(param.getComponentName())) {
            return null;
        }
        String scopeLevel = normalizeConfigScope(param.getScopeLevel());
        Long scopeTenantId = SCOPE_PUBLIC.equals(scopeLevel) ? 0L : safeTenantId(tenantId);
        SysHomepageComponentConfig entity = param.getId() == null ? findConfigByCode(scopeTenantId, scopeLevel, param.getComponentCode()) : configMapper.selectById(param.getId());
        if (entity == null) {
            entity = new SysHomepageComponentConfig();
            entity.setTenantId(scopeTenantId);
            entity.setScopeLevel(scopeLevel);
        }
        entity.setCategoryId(resolveCategoryId(param));
        entity.setComponentCode(param.getComponentCode().trim());
        entity.setComponentName(param.getComponentName().trim());
        entity.setComponentPath(StringUtils.hasText(param.getComponentPath()) ? param.getComponentPath().trim() : entity.getComponentCode());
        entity.setIcon(param.getIcon());
        entity.setUseDesc(param.getUseDesc());
        entity.setDefaultParamsJson(param.getDefaultParamsJson());
        entity.setEnabled(param.getEnabled() == null ? Boolean.TRUE : param.getEnabled());
        entity.setOrderNum(param.getOrderNum() == null ? 0 : param.getOrderNum());
        entity.setRemark(param.getRemark());
        entity.setDeleted(Boolean.FALSE);
        if (entity.getId() == null) {
            configMapper.insert(entity);
        } else {
            configMapper.updateById(entity);
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComponent(Long id, Long tenantId) {
        return deleteComponent(id, tenantId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComponent(Long id, Long tenantId, String scopeLevel) {
        if (id == null) {
            return false;
        }
        SysHomepageComponentConfig entity = configMapper.selectById(id);
        if (entity == null) {
            return true;
        }
        String resolvedScope = StringUtils.hasText(scopeLevel) ? scopeLevel.trim().toUpperCase() : entity.getScopeLevel();
        if (!StringUtils.hasText(entity.getScopeLevel()) || !entity.getScopeLevel().equalsIgnoreCase(resolvedScope)) {
            return false;
        }
        if (SCOPE_TENANT.equalsIgnoreCase(entity.getScopeLevel()) && !Objects.equals(entity.getTenantId(), safeTenantId(tenantId))) {
            return false;
        }
        if (SCOPE_PUBLIC.equalsIgnoreCase(entity.getScopeLevel()) && !SCOPE_PUBLIC.equalsIgnoreCase(resolvedScope)) {
            return false;
        }
        return configMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pullPublicComponents(Long tenantId, HomepageComponentPullParam param) {
        Long currentTenantId = safeTenantId(tenantId);
        HomepageComponentQueryParam query = new HomepageComponentQueryParam();
        query.setModuleCode(param == null ? null : param.getModuleCode());
        query.setCategoryId(param == null ? null : param.getCategoryId());
        List<SysHomepageComponentConfig> publicConfigs = listConfigs(0L, SCOPE_PUBLIC, query);
        int count = 0;
        for (SysHomepageComponentConfig source : publicConfigs) {
            SysHomepageComponentConfig target = findConfigByCode(currentTenantId, SCOPE_TENANT, source.getComponentCode());
            if (target == null) {
                target = new SysHomepageComponentConfig();
                target.setTenantId(currentTenantId);
                target.setScopeLevel(SCOPE_TENANT);
            }
            copyConfigSnapshot(source, target);
            target.setScopeLevel(SCOPE_TENANT);
            target.setTenantId(currentTenantId);
            target.setDeleted(Boolean.FALSE);
            if (target.getId() == null) {
                configMapper.insert(target);
            } else {
                configMapper.updateById(target);
            }
            count++;
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pullTenantComponents(Long userId, Long tenantId, HomepageComponentPullParam param) {
        Long currentTenantId = safeTenantId(tenantId);
        clearPreferenceSnapshot(userId, currentTenantId, param == null ? null : param.getModuleCode(), param == null ? null : param.getCategoryId());
        return createPreferenceSnapshot(userId, currentTenantId, param == null ? null : param.getModuleCode(), param == null ? null : param.getCategoryId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean favoriteComponent(Long userId, Long tenantId, HomepageComponentPreferenceParam param) {
        SysHomepageComponentPreference preference = ensurePreference(userId, tenantId, param);
        if (preference == null) {
            return false;
        }
        preference.setFavorite(Boolean.TRUE.equals(param.getFavorite()));
        preferenceMapper.updateById(preference);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addComponent(Long userId, Long tenantId, HomepageComponentPreferenceParam param) {
        SysHomepageComponentPreference preference = ensurePreference(userId, tenantId, param);
        if (preference == null) {
            return false;
        }
        preference.setRemoved(Boolean.FALSE);
        preferenceMapper.updateById(preference);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeComponent(Long userId, Long tenantId, HomepageComponentPreferenceParam param) {
        SysHomepageComponentPreference preference = ensurePreference(userId, tenantId, param);
        if (preference == null) {
            return false;
        }
        preference.setRemoved(Boolean.TRUE);
        preferenceMapper.updateById(preference);
        return true;
    }

    private LambdaQueryWrapper<SysHomepageComponentConfig> buildConfigWrapper(HomepageComponentQueryParam param, String scopeLevel, Long tenantId) {
        LambdaQueryWrapper<SysHomepageComponentConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysHomepageComponentConfig::getScopeLevel, scopeLevel);
        wrapper.eq(SysHomepageComponentConfig::getTenantId, tenantId);
        wrapper.eq(param.getCategoryId() != null, SysHomepageComponentConfig::getCategoryId, param.getCategoryId());
        wrapper.eq(param.getEnabled() != null, SysHomepageComponentConfig::getEnabled, param.getEnabled());
        wrapper.like(StringUtils.hasText(param.getComponentCode()), SysHomepageComponentConfig::getComponentCode, trim(param.getComponentCode()));
        wrapper.like(StringUtils.hasText(param.getComponentName()), SysHomepageComponentConfig::getComponentName, trim(param.getComponentName()));
        if (StringUtils.hasText(param.getCategoryCode()) || StringUtils.hasText(param.getModuleCode())) {
            List<Long> categoryIds = findCategoryIds(param.getCategoryCode(), param.getModuleCode(), null);
            if (categoryIds.isEmpty()) {
                wrapper.eq(SysHomepageComponentConfig::getId, -1L);
            } else {
                wrapper.in(SysHomepageComponentConfig::getCategoryId, categoryIds);
            }
        }
        if (StringUtils.hasText(param.getCategoryName())) {
            List<Long> categoryIds = findCategoryIdsByKeyword(param.getCategoryName());
            if (categoryIds.isEmpty()) {
                wrapper.eq(SysHomepageComponentConfig::getId, -1L);
            } else {
                wrapper.in(SysHomepageComponentConfig::getCategoryId, categoryIds);
            }
        }
        if (StringUtils.hasText(param.getKeyword())) {
            String keyword = param.getKeyword().trim();
            List<Long> categoryIds = findCategoryIdsByKeyword(keyword);
            wrapper.and(item -> item.like(SysHomepageComponentConfig::getComponentCode, keyword)
                .or().like(SysHomepageComponentConfig::getComponentName, keyword)
                .or().like(SysHomepageComponentConfig::getUseDesc, keyword)
                .or(categoryIds != null && !categoryIds.isEmpty(), query -> query.in(SysHomepageComponentConfig::getCategoryId, categoryIds)));
        }
        wrapper.orderByAsc(SysHomepageComponentConfig::getOrderNum).orderByAsc(SysHomepageComponentConfig::getId);
        return wrapper;
    }

    private List<SysHomepageComponentConfig> listBaseEffectiveConfigs(Long tenantId, HomepageComponentQueryParam param) {
        List<SysHomepageComponentConfig> publicConfigs = listConfigs(0L, SCOPE_PUBLIC, param);
        List<SysHomepageComponentConfig> tenantConfigs = listConfigs(tenantId, SCOPE_TENANT, param);
        Map<String, SysHomepageComponentConfig> effectiveMap = new LinkedHashMap<>();
        publicConfigs.forEach(config -> {
            config.setScopeLevel(SCOPE_PUBLIC);
            effectiveMap.put(config.getComponentCode(), config);
        });
        tenantConfigs.forEach(config -> {
            config.setScopeLevel(SCOPE_TENANT);
            effectiveMap.put(config.getComponentCode(), config);
        });
        return new ArrayList<>(effectiveMap.values());
    }

    private List<SysHomepageComponentConfig> listConfigs(Long tenantId, String scopeLevel, HomepageComponentQueryParam param) {
        return configMapper.selectList(buildConfigWrapper(param == null ? new HomepageComponentQueryParam() : param, scopeLevel, tenantId));
    }

    private List<SysHomepageComponentPreference> listPreferences(Long userId, Long tenantId, String moduleCode, Long categoryId) {
        if (userId == null || tenantId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SysHomepageComponentPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysHomepageComponentPreference::getUserId, userId);
        wrapper.eq(SysHomepageComponentPreference::getTenantId, tenantId);
        wrapper.eq(categoryId != null, SysHomepageComponentPreference::getCategoryId, categoryId);
        if (StringUtils.hasText(moduleCode)) {
            List<Long> categoryIds = findCategoryIds(null, moduleCode, null);
            if (categoryIds.isEmpty()) {
                wrapper.eq(SysHomepageComponentPreference::getId, -1L);
            } else {
                wrapper.in(SysHomepageComponentPreference::getCategoryId, categoryIds);
            }
        }
        wrapper.orderByAsc(SysHomepageComponentPreference::getOrderNum).orderByAsc(SysHomepageComponentPreference::getId);
        return preferenceMapper.selectList(wrapper);
    }

    private SysHomepageComponentPreference ensurePreference(Long userId, Long tenantId, HomepageComponentPreferenceParam param) {
        if (userId == null || tenantId == null || param == null || !StringUtils.hasText(param.getComponentCode())) {
            return null;
        }
        Long currentTenantId = safeTenantId(tenantId);
        if (listPreferences(userId, currentTenantId, normalizeModuleCode(param.getModuleCode()), null).isEmpty()) {
            createPreferenceSnapshot(userId, currentTenantId, param.getModuleCode(), null);
        }
        SysHomepageComponentPreference preference = findPreference(userId, currentTenantId, param.getComponentCode());
        if (preference != null) {
            return preference;
        }
        SysHomepageComponentConfig source = listBaseEffectiveConfigs(currentTenantId, queryByModule(param.getModuleCode())).stream()
            .filter(item -> param.getComponentCode().equals(item.getComponentCode()))
            .findFirst()
            .orElse(null);
        if (source == null) {
            return null;
        }
        SysHomepageComponentPreference entity = createPreferenceEntity(userId, currentTenantId, source);
        preferenceMapper.insert(entity);
        return entity;
    }

    private int createPreferenceSnapshot(Long userId, Long tenantId, String moduleCode, Long categoryId) {
        HomepageComponentQueryParam query = new HomepageComponentQueryParam();
        query.setModuleCode(moduleCode);
        query.setCategoryId(categoryId);
        int count = 0;
        for (SysHomepageComponentConfig config : listBaseEffectiveConfigs(tenantId, query)) {
            if (!Boolean.TRUE.equals(config.getEnabled())) {
                continue;
            }
            SysHomepageComponentPreference entity = findPreference(userId, tenantId, config.getComponentCode());
            if (entity == null) {
                entity = createPreferenceEntity(userId, tenantId, config);
                preferenceMapper.insert(entity);
            } else {
                copyPreferenceSnapshot(config, entity);
                preferenceMapper.updateById(entity);
            }
            count++;
        }
        return count;
    }

    private void clearPreferenceSnapshot(Long userId, Long tenantId, String moduleCode, Long categoryId) {
        List<SysHomepageComponentPreference> preferences = listPreferences(userId, tenantId, normalizeModuleCode(moduleCode), categoryId);
        for (SysHomepageComponentPreference preference : preferences) {
            preferenceMapper.deleteById(preference.getId());
        }
    }

    private SysHomepageComponentPreference createPreferenceEntity(Long userId, Long tenantId, SysHomepageComponentConfig source) {
        SysHomepageComponentPreference entity = new SysHomepageComponentPreference();
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setFavorite(Boolean.FALSE);
        entity.setRemoved(Boolean.FALSE);
        entity.setDeleted(Boolean.FALSE);
        copyPreferenceSnapshot(source, entity);
        return entity;
    }

    private void copyPreferenceSnapshot(SysHomepageComponentConfig source, SysHomepageComponentPreference target) {
        target.setCategoryId(source.getCategoryId());
        target.setSourceComponentId(source.getId());
        target.setComponentCode(source.getComponentCode());
        target.setComponentName(source.getComponentName());
        target.setComponentPath(source.getComponentPath());
        target.setIcon(source.getIcon());
        target.setUseDesc(source.getUseDesc());
        target.setOrderNum(source.getOrderNum());
        target.setParamsJson(source.getDefaultParamsJson());
        target.setRemark(source.getRemark());
        target.setDeleted(Boolean.FALSE);
    }

    private void copyConfigSnapshot(SysHomepageComponentConfig source, SysHomepageComponentConfig target) {
        target.setCategoryId(source.getCategoryId());
        target.setComponentCode(source.getComponentCode());
        target.setComponentName(source.getComponentName());
        target.setComponentPath(source.getComponentPath());
        target.setIcon(source.getIcon());
        target.setUseDesc(source.getUseDesc());
        target.setDefaultParamsJson(source.getDefaultParamsJson());
        target.setEnabled(source.getEnabled());
        target.setOrderNum(source.getOrderNum());
        target.setRemark(source.getRemark());
    }

    private Long resolveCategoryId(HomepageComponentSaveParam param) {
        if (param.getCategoryId() != null) {
            return param.getCategoryId();
        }
        String moduleCode = normalizeModuleCode(param.getModuleCode());
        String categoryCode = StringUtils.hasText(param.getCategoryCode()) ? param.getCategoryCode().trim() : DEFAULT_CATEGORY_CODE;
        String categoryName = StringUtils.hasText(param.getCategoryName()) ? param.getCategoryName().trim() : DEFAULT_CATEGORY_NAME;
        SysHomepageComponentCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<SysHomepageComponentCategory>()
            .eq(SysHomepageComponentCategory::getTenantId, 0L)
            .eq(SysHomepageComponentCategory::getModuleCode, moduleCode)
            .eq(SysHomepageComponentCategory::getCategoryCode, categoryCode)
            .last("limit 1"));
        if (category != null) {
            return category.getId();
        }
        category = new SysHomepageComponentCategory();
        category.setTenantId(0L);
        category.setModuleCode(moduleCode);
        category.setCategoryCode(categoryCode);
        category.setCategoryName(categoryName);
        category.setDeleted(Boolean.FALSE);
        categoryMapper.insert(category);
        return category.getId();
    }

    private List<Long> findCategoryIds(String categoryCode, String moduleCode, Long categoryId) {
        if (categoryId != null) {
            return List.of(categoryId);
        }
        LambdaQueryWrapper<SysHomepageComponentCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(categoryCode), SysHomepageComponentCategory::getCategoryCode, categoryCode);
        wrapper.eq(StringUtils.hasText(moduleCode), SysHomepageComponentCategory::getModuleCode, normalizeModuleCode(moduleCode));
        return categoryMapper.selectList(wrapper).stream().map(SysHomepageComponentCategory::getId).collect(Collectors.toList());
    }

    private List<Long> findCategoryIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        String value = keyword.trim();
        LambdaQueryWrapper<SysHomepageComponentCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysHomepageComponentCategory::getCategoryCode, value)
            .or().like(SysHomepageComponentCategory::getCategoryName, value)
            .or().like(SysHomepageComponentCategory::getModuleCode, value);
        return categoryMapper.selectList(wrapper).stream().map(SysHomepageComponentCategory::getId).collect(Collectors.toList());
    }

    private SysHomepageComponentConfig findConfigByCode(Long tenantId, String scopeLevel, String componentCode) {
        if (!StringUtils.hasText(componentCode)) {
            return null;
        }
        return configMapper.selectOne(new LambdaQueryWrapper<SysHomepageComponentConfig>()
            .eq(SysHomepageComponentConfig::getTenantId, tenantId)
            .eq(SysHomepageComponentConfig::getScopeLevel, scopeLevel)
            .eq(SysHomepageComponentConfig::getComponentCode, componentCode.trim())
            .last("limit 1"));
    }

    private SysHomepageComponentPreference findPreference(Long userId, Long tenantId, String componentCode) {
        if (!StringUtils.hasText(componentCode)) {
            return null;
        }
        return preferenceMapper.selectOne(new LambdaQueryWrapper<SysHomepageComponentPreference>()
            .eq(SysHomepageComponentPreference::getTenantId, tenantId)
            .eq(SysHomepageComponentPreference::getUserId, userId)
            .eq(SysHomepageComponentPreference::getComponentCode, componentCode.trim())
            .last("limit 1"));
    }

    private HomepageComponentVO toConfigVO(SysHomepageComponentConfig entity, String sourceScope) {
        HomepageComponentVO vo = new HomepageComponentVO();
        BeanUtils.copyProperties(entity, vo);
        fillCategory(vo, entity.getCategoryId());
        vo.setScopeLevel(entity.getScopeLevel());
        vo.setSourceScope(sourceScope);
        vo.setFavorite(Boolean.FALSE);
        vo.setRemoved(Boolean.FALSE);
        vo.setSelected(Boolean.TRUE.equals(entity.getEnabled()));
        vo.setParams(entity.getDefaultParamsJson());
        vo.setDefaultParamsJson(entity.getDefaultParamsJson());
        return vo;
    }

    private HomepageComponentVO toPreferenceVO(SysHomepageComponentPreference entity) {
        HomepageComponentVO vo = new HomepageComponentVO();
        BeanUtils.copyProperties(entity, vo);
        fillCategory(vo, entity.getCategoryId());
        vo.setScopeLevel(SCOPE_USER);
        vo.setSourceScope(SCOPE_USER);
        vo.setFavorite(Boolean.TRUE.equals(entity.getFavorite()));
        vo.setRemoved(Boolean.TRUE.equals(entity.getRemoved()));
        vo.setSelected(!Boolean.TRUE.equals(entity.getRemoved()));
        vo.setEnabled(Boolean.TRUE);
        vo.setParams(entity.getParamsJson());
        vo.setDefaultParamsJson(entity.getParamsJson());
        return vo;
    }

    private void fillCategory(HomepageComponentVO vo, Long categoryId) {
        if (categoryId == null) {
            return;
        }
        SysHomepageComponentCategory category = categoryMapper.selectById(categoryId);
        if (category != null) {
            vo.setCategoryId(category.getId());
            vo.setCategoryCode(category.getCategoryCode());
            vo.setCategoryName(category.getCategoryName());
            vo.setModuleCode(category.getModuleCode());
        }
    }

    private Comparator<HomepageComponentVO> componentComparator() {
        return Comparator
            .comparing((HomepageComponentVO item) -> item.getOrderNum() == null ? 0 : item.getOrderNum())
            .thenComparing(item -> item.getComponentCode() == null ? "" : item.getComponentCode());
    }

    private boolean matchesKeyword(String code, String name, String useDesc, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String value = keyword.trim().toLowerCase();
        return containsIgnoreCase(code, value) || containsIgnoreCase(name, value) || containsIgnoreCase(useDesc, value);
    }

    private boolean matchesQuery(HomepageComponentVO item, HomepageComponentQueryParam param) {
        if (item == null || param == null) {
            return true;
        }
        return matchesKeyword(
            join(item.getComponentCode(), item.getCategoryCode()),
            join(item.getComponentName(), item.getCategoryName(), item.getModuleCode()),
            item.getUseDesc(),
            param.getKeyword()
        )
            && matchesText(item.getComponentCode(), param.getComponentCode())
            && matchesText(item.getComponentName(), param.getComponentName())
            && matchesText(item.getCategoryName(), param.getCategoryName());
    }

    private boolean matchesScope(String scopeLevel, String queryScopeLevel) {
        if (!StringUtils.hasText(queryScopeLevel)) {
            return true;
        }
        return StringUtils.hasText(scopeLevel) && scopeLevel.equalsIgnoreCase(queryScopeLevel.trim());
    }

    private boolean matchesText(String text, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return containsIgnoreCase(text, keyword.trim().toLowerCase());
    }

    private String join(String... values) {
        return java.util.Arrays.stream(values)
            .filter(StringUtils::hasText)
            .collect(Collectors.joining(" "));
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : value;
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        return StringUtils.hasText(text) && text.toLowerCase().contains(keyword);
    }

    private HomepageComponentQueryParam queryByModule(String moduleCode) {
        HomepageComponentQueryParam query = new HomepageComponentQueryParam();
        query.setModuleCode(moduleCode);
        return query;
    }

    private String normalizeConfigScope(String scopeLevel) {
        if (!StringUtils.hasText(scopeLevel)) {
            return SCOPE_TENANT;
        }
        return SCOPE_PUBLIC.equalsIgnoreCase(scopeLevel.trim()) ? SCOPE_PUBLIC : SCOPE_TENANT;
    }

    private String normalizeModuleCode(String moduleCode) {
        if (!StringUtils.hasText(moduleCode)) {
            return DEFAULT_MODULE_CODE;
        }
        String normalized = moduleCode.trim().toLowerCase();
        if ("system".equals(normalized)) {
            return "sys";
        }
        if ("workflow".equals(normalized)) {
            return "approval";
        }
        return normalized;
    }

    private Long safeTenantId(Long tenantId) {
        return tenantId == null ? 0L : tenantId;
    }
}
