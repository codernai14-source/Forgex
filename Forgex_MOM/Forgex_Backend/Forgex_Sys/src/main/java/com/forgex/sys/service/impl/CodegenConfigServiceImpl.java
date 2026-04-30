/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.dto.CodegenConfigDTO;
import com.forgex.sys.domain.dto.CodegenConfigQueryDTO;
import com.forgex.sys.domain.dto.CodegenDatasourceDTO;
import com.forgex.sys.domain.entity.SysCodegenConfig;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.param.CodegenConfigSaveParam;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysCodegenConfigMapper;
import com.forgex.sys.service.ICodegenConfigService;
import com.forgex.sys.service.ICodegenDatasourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 代码生成配置服务实现
 * <p>
 * 通过摘要字段加 JSON 快照的方式保存配置，满足列表展示、再次编辑和生成时按记录复用。
 * </p>
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Service
@RequiredArgsConstructor
public class CodegenConfigServiceImpl extends ServiceImpl<SysCodegenConfigMapper, SysCodegenConfig>
    implements ICodegenConfigService {

    private static final String PAGE_TYPE_MASTER_DETAIL = "MASTER_DETAIL";
    private static final String PAGE_TYPE_TREE_SINGLE = "TREE_SINGLE";
    private static final String PAGE_TYPE_TREE_DOUBLE = "TREE_DOUBLE";

    private final SysCodegenConfigMapper configMapper;
    private final ICodegenDatasourceService datasourceService;

    @Override
    public IPage<CodegenConfigDTO> pageConfigs(Page<SysCodegenConfig> page, CodegenConfigQueryDTO query) {
        LambdaQueryWrapper<SysCodegenConfig> wrapper = buildWrapper(query);
        return configMapper.selectPage(page, wrapper).convert(this::toDTO);
    }

    @Override
    public CodegenConfigDTO getConfigDetail(Long id) {
        return toDTO(requireEntity(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveConfig(CodegenConfigSaveParam param) {
        validateSaveParam(param);
        SysCodegenDatasource datasource = datasourceService.requireEntity(param.getDatasourceId());
        SysCodegenConfig entity = param.getId() == null ? new SysCodegenConfig() : requireEntity(param.getId());

        entity.setConfigName(resolveConfigName(param));
        entity.setDatasourceId(param.getDatasourceId());
        entity.setDatasourceCode(datasource.getDatasourceCode());
        entity.setSchemaName(param.getSchemaName());
        entity.setPageType(normalizePageType(param.getPageType()));
        entity.setMainTableName(param.getMainTableName());
        entity.setSubTableName(param.getSubTableName());
        entity.setMainPkColumn(param.getMainPkColumn());
        entity.setSubFkColumn(param.getSubFkColumn());
        entity.setSubPkColumn(param.getSubPkColumn());
        entity.setModuleName(param.getModuleName());
        entity.setBizName(param.getBizName());
        entity.setEntityName(param.getEntityName());
        entity.setSubEntityName(param.getSubEntityName());
        entity.setPackageName(param.getPackageName());
        entity.setAuthor(param.getAuthor());
        entity.setMenuName(param.getMenuName());
        entity.setMenuIcon(param.getMenuIcon());
        entity.setParentMenuPath(param.getParentMenuPath());
        entity.setTableCodePrefix(param.getTableCodePrefix());
        entity.setGenerateItemsJson(JSONUtil.toJsonStr(param.getGenerateItems()));
        entity.setConfigJson(JSONUtil.toJsonStr(copyRequest(param)));
        entity.setRemark(param.getRemark());
        if (entity.getId() == null) {
            configMapper.insert(entity);
        } else {
            configMapper.updateById(entity);
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        requireEntity(id);
        configMapper.deleteById(id);
    }

    @Override
    public CodeGenRequestDTO buildRequest(Long id) {
        SysCodegenConfig entity = requireEntity(id);
        CodeGenRequestDTO request = parseRequest(entity.getConfigJson());
        request.setConfigId(entity.getId());
        request.setDatasourceId(entity.getDatasourceId());
        request.setDatasourceCode(entity.getDatasourceCode());
        request.setSchemaName(defaultText(request.getSchemaName(), entity.getSchemaName()));
        request.setPageType(defaultText(request.getPageType(), entity.getPageType()));
        request.setMainTableName(defaultText(request.getMainTableName(), entity.getMainTableName()));
        request.setSubTableName(defaultText(request.getSubTableName(), entity.getSubTableName()));
        request.setMainPkColumn(defaultText(request.getMainPkColumn(), entity.getMainPkColumn()));
        request.setSubFkColumn(defaultText(request.getSubFkColumn(), entity.getSubFkColumn()));
        request.setSubPkColumn(defaultText(request.getSubPkColumn(), entity.getSubPkColumn()));
        request.setModuleName(defaultText(request.getModuleName(), entity.getModuleName()));
        request.setBizName(defaultText(request.getBizName(), entity.getBizName()));
        request.setEntityName(defaultText(request.getEntityName(), entity.getEntityName()));
        request.setSubEntityName(defaultText(request.getSubEntityName(), entity.getSubEntityName()));
        request.setPackageName(defaultText(request.getPackageName(), entity.getPackageName()));
        request.setAuthor(defaultText(request.getAuthor(), entity.getAuthor()));
        request.setMenuName(defaultText(request.getMenuName(), entity.getMenuName()));
        request.setMenuIcon(defaultText(request.getMenuIcon(), entity.getMenuIcon()));
        request.setParentMenuPath(defaultText(request.getParentMenuPath(), entity.getParentMenuPath()));
        request.setTableCodePrefix(defaultText(request.getTableCodePrefix(), entity.getTableCodePrefix()));
        if (CollectionUtils.isEmpty(request.getGenerateItems()) && StringUtils.hasText(entity.getGenerateItemsJson())) {
            request.setGenerateItems(JSONUtil.toList(entity.getGenerateItemsJson(), String.class));
        }
        return request;
    }

    @Override
    public SysCodegenConfig requireEntity(Long id) {
        SysCodegenConfig entity = configMapper.selectById(id);
        if (entity == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_CONFIG_NOT_FOUND);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markGenerated(Long id) {
        if (id == null) {
            return;
        }
        SysCodegenConfig entity = requireEntity(id);
        entity.setLastGenerateTime(LocalDateTime.now());
        configMapper.updateById(entity);
    }

    private LambdaQueryWrapper<SysCodegenConfig> buildWrapper(CodegenConfigQueryDTO query) {
        LambdaQueryWrapper<SysCodegenConfig> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getConfigName()), SysCodegenConfig::getConfigName, query.getConfigName());
            wrapper.eq(StringUtils.hasText(query.getPageType()), SysCodegenConfig::getPageType, normalizePageType(query.getPageType()));
            wrapper.like(StringUtils.hasText(query.getModuleName()), SysCodegenConfig::getModuleName, query.getModuleName());
            wrapper.like(StringUtils.hasText(query.getBizName()), SysCodegenConfig::getBizName, query.getBizName());
            wrapper.like(StringUtils.hasText(query.getMenuName()), SysCodegenConfig::getMenuName, query.getMenuName());
            wrapper.like(StringUtils.hasText(query.getMainTableName()), SysCodegenConfig::getMainTableName, query.getMainTableName());
        }
        wrapper.orderByDesc(SysCodegenConfig::getUpdateTime);
        wrapper.orderByDesc(SysCodegenConfig::getId);
        return wrapper;
    }

    private void validateSaveParam(CodegenConfigSaveParam param) {
        if (param == null
            || param.getDatasourceId() == null
            || !StringUtils.hasText(param.getSchemaName())
            || !StringUtils.hasText(param.getPageType())
            || !StringUtils.hasText(param.getMainTableName())
            || !StringUtils.hasText(param.getModuleName())
            || !StringUtils.hasText(param.getBizName())
            || !StringUtils.hasText(param.getEntityName())
            || !StringUtils.hasText(param.getPackageName())
            || !StringUtils.hasText(param.getAuthor())
            || !StringUtils.hasText(param.getMenuName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_PARAM_EMPTY);
        }

        String pageType = normalizePageType(param.getPageType());
        if (PAGE_TYPE_MASTER_DETAIL.equals(pageType)
            && (!StringUtils.hasText(param.getSubTableName())
            || !StringUtils.hasText(param.getMainPkColumn())
            || !StringUtils.hasText(param.getSubFkColumn())
            || !StringUtils.hasText(param.getSubPkColumn()))) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_PARAM_EMPTY);
        }

        if ((PAGE_TYPE_TREE_SINGLE.equals(pageType) || PAGE_TYPE_TREE_DOUBLE.equals(pageType))
            && !StringUtils.hasText(param.getTreeTableName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_TREE_FIELD_MISSING, "treeTableName");
        }
    }

    private String resolveConfigName(CodegenConfigSaveParam param) {
        if (StringUtils.hasText(param.getConfigName())) {
            return param.getConfigName();
        }
        if (StringUtils.hasText(param.getMenuName())) {
            return param.getMenuName();
        }
        if (StringUtils.hasText(param.getEntityName())) {
            return param.getEntityName();
        }
        return param.getBizName();
    }

    private CodeGenRequestDTO copyRequest(CodegenConfigSaveParam param) {
        CodeGenRequestDTO request = new CodeGenRequestDTO();
        request.setConfigId(param.getId());
        request.setDatasourceId(param.getDatasourceId());
        request.setDatasourceCode(param.getDatasourceCode());
        request.setSchemaName(param.getSchemaName());
        request.setPageType(normalizePageType(param.getPageType()));
        request.setMainTableName(param.getMainTableName());
        request.setTreeTableName(param.getTreeTableName());
        request.setSubTableName(param.getSubTableName());
        request.setMainPkColumn(param.getMainPkColumn());
        request.setTreePkColumn(param.getTreePkColumn());
        request.setTreeParentColumn(param.getTreeParentColumn());
        request.setTreeLabelColumn(param.getTreeLabelColumn());
        request.setTreeSortColumn(param.getTreeSortColumn());
        request.setTreeFilterColumn(param.getTreeFilterColumn());
        request.setSubFkColumn(param.getSubFkColumn());
        request.setSubPkColumn(param.getSubPkColumn());
        request.setModuleName(param.getModuleName());
        request.setBizName(param.getBizName());
        request.setEntityName(param.getEntityName());
        request.setTreeEntityName(param.getTreeEntityName());
        request.setSubEntityName(param.getSubEntityName());
        request.setPackageName(param.getPackageName());
        request.setAuthor(param.getAuthor());
        request.setMenuName(param.getMenuName());
        request.setMenuIcon(param.getMenuIcon());
        request.setParentMenuPath(param.getParentMenuPath());
        request.setTableCodePrefix(param.getTableCodePrefix());
        request.setAndroidFeatureKey(param.getAndroidFeatureKey());
        request.setGenerateItems(param.getGenerateItems());
        request.setMainColumns(param.getMainColumns());
        request.setTreeColumns(param.getTreeColumns());
        request.setSubColumns(param.getSubColumns());
        return request;
    }

    private CodeGenRequestDTO parseRequest(String json) {
        if (!StringUtils.hasText(json)) {
            return new CodeGenRequestDTO();
        }
        return JSONUtil.toBean(json, CodeGenRequestDTO.class);
    }

    private CodegenConfigDTO toDTO(SysCodegenConfig entity) {
        CodeGenRequestDTO request = parseRequest(entity.getConfigJson());
        CodegenConfigDTO dto = new CodegenConfigDTO();
        dto.setId(entity.getId());
        dto.setConfigId(entity.getId());
        dto.setConfigName(entity.getConfigName());
        dto.setDatasourceId(entity.getDatasourceId());
        dto.setDatasourceCode(entity.getDatasourceCode());
        dto.setSchemaName(defaultText(request.getSchemaName(), entity.getSchemaName()));
        dto.setPageType(defaultText(request.getPageType(), entity.getPageType()));
        dto.setMainTableName(defaultText(request.getMainTableName(), entity.getMainTableName()));
        dto.setTreeTableName(request.getTreeTableName());
        dto.setSubTableName(defaultText(request.getSubTableName(), entity.getSubTableName()));
        dto.setMainPkColumn(defaultText(request.getMainPkColumn(), entity.getMainPkColumn()));
        dto.setTreePkColumn(request.getTreePkColumn());
        dto.setTreeParentColumn(request.getTreeParentColumn());
        dto.setTreeLabelColumn(request.getTreeLabelColumn());
        dto.setTreeSortColumn(request.getTreeSortColumn());
        dto.setTreeFilterColumn(request.getTreeFilterColumn());
        dto.setSubFkColumn(defaultText(request.getSubFkColumn(), entity.getSubFkColumn()));
        dto.setSubPkColumn(defaultText(request.getSubPkColumn(), entity.getSubPkColumn()));
        dto.setModuleName(defaultText(request.getModuleName(), entity.getModuleName()));
        dto.setBizName(defaultText(request.getBizName(), entity.getBizName()));
        dto.setEntityName(defaultText(request.getEntityName(), entity.getEntityName()));
        dto.setTreeEntityName(request.getTreeEntityName());
        dto.setSubEntityName(defaultText(request.getSubEntityName(), entity.getSubEntityName()));
        dto.setPackageName(defaultText(request.getPackageName(), entity.getPackageName()));
        dto.setAuthor(defaultText(request.getAuthor(), entity.getAuthor()));
        dto.setMenuName(defaultText(request.getMenuName(), entity.getMenuName()));
        dto.setMenuIcon(defaultText(request.getMenuIcon(), entity.getMenuIcon()));
        dto.setParentMenuPath(defaultText(request.getParentMenuPath(), entity.getParentMenuPath()));
        dto.setTableCodePrefix(defaultText(request.getTableCodePrefix(), entity.getTableCodePrefix()));
        dto.setAndroidFeatureKey(request.getAndroidFeatureKey());
        dto.setGenerateItems(resolveGenerateItems(request.getGenerateItems(), entity.getGenerateItemsJson()));
        dto.setMainColumns(request.getMainColumns());
        dto.setTreeColumns(request.getTreeColumns());
        dto.setSubColumns(request.getSubColumns());
        dto.setDatasourceName(resolveDatasourceName(entity.getDatasourceId()));
        dto.setLastGenerateTime(entity.getLastGenerateTime());
        dto.setRemark(entity.getRemark());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    private List<String> resolveGenerateItems(List<String> generateItems, String generateItemsJson) {
        if (!CollectionUtils.isEmpty(generateItems)) {
            return generateItems;
        }
        if (StringUtils.hasText(generateItemsJson)) {
            return JSONUtil.toList(generateItemsJson, String.class);
        }
        return List.of("backend", "frontend", "sql");
    }

    private String resolveDatasourceName(Long datasourceId) {
        if (datasourceId == null) {
            return null;
        }
        CodegenDatasourceDTO datasource = datasourceService.getDatasourceById(datasourceId);
        return datasource == null ? null : datasource.getDatasourceName();
    }

    private String normalizePageType(String pageType) {
        return StringUtils.hasText(pageType) ? pageType.toUpperCase(Locale.ROOT) : pageType;
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
