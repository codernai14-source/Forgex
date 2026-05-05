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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.CodegenDatasourceDTO;
import com.forgex.sys.domain.dto.CodegenDatasourceQueryDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.param.CodegenDatasourceSaveParam;
import com.forgex.sys.domain.param.CodegenDatasourceTestParam;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysCodegenDatasourceMapper;
import com.forgex.sys.service.ICodegenDatasourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码生成数据源服务实现
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CodegenDatasourceServiceImpl
    extends ServiceImpl<SysCodegenDatasourceMapper, SysCodegenDatasource>
    implements ICodegenDatasourceService {

    private final SysCodegenDatasourceMapper datasourceMapper;

    /**
     * 分页查询代码生成数据源。
     *
     * @param page 分页对象
     * @param query 查询参数
     * @return 处理结果
     */
    @Override
    public IPage<CodegenDatasourceDTO> pageDatasources(Page<SysCodegenDatasource> page, CodegenDatasourceQueryDTO query) {
        LambdaQueryWrapper<SysCodegenDatasource> wrapper = buildWrapper(query);
        return datasourceMapper.selectPage(page, wrapper).convert(this::toDTO);
    }

    /**
     * 查询启用的数据源列表。
     *
     * @return 列表数据
     */
    @Override
    public List<CodegenDatasourceDTO> listEnabledDatasources() {
        LambdaQueryWrapper<SysCodegenDatasource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysCodegenDatasource::getEnabled, true);
        wrapper.orderByAsc(SysCodegenDatasource::getDatasourceCode);
        return datasourceMapper.selectList(wrapper).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 获取数据源byID。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public CodegenDatasourceDTO getDatasourceById(Long id) {
        SysCodegenDatasource entity = datasourceMapper.selectById(id);
        return entity == null ? null : toDTO(entity);
    }

    /**
     * 保存数据源。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDatasource(CodegenDatasourceSaveParam param) {
        validateSaveParam(param);
        SysCodegenDatasource entity = param.getId() == null ? new SysCodegenDatasource() : requireEntity(param.getId());
        BeanUtils.copyProperties(param, entity);
        if (entity.getEnabled() == null) {
            entity.setEnabled(Boolean.TRUE);
        }
        saveOrUpdate(entity);
        return entity.getId();
    }

    /**
     * 删除数据源。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatasource(Long id) {
        requireEntity(id);
        datasourceMapper.deleteById(id);
    }

    /**
     * 测试数据源连接。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    public boolean testConnection(CodegenDatasourceTestParam param) {
        if (param == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_PARAM_EMPTY);
        }
        String jdbcUrl = param.getJdbcUrl();
        String username = param.getUsername();
        String password = param.getPassword();
        if (!StringUtils.hasText(jdbcUrl) && param.getId() != null) {
            SysCodegenDatasource entity = requireEntity(param.getId());
            jdbcUrl = entity.getJdbcUrl();
            username = entity.getUsername();
            password = entity.getPassword();
        }
        if (!StringUtils.hasText(jdbcUrl) || !StringUtils.hasText(username)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_PARAM_EMPTY);
        }
        try (Connection ignored = DriverManager.getConnection(jdbcUrl, username, password)) {
            return true;
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_TEST_FAILED, ex.getMessage());
        }
    }

    /**
     * 获取实体数据，不存在时抛出业务异常。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public SysCodegenDatasource requireEntity(Long id) {
        SysCodegenDatasource entity = datasourceMapper.selectById(id);
        if (entity == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_NOT_FOUND);
        }
        return entity;
    }

    private LambdaQueryWrapper<SysCodegenDatasource> buildWrapper(CodegenDatasourceQueryDTO query) {
        LambdaQueryWrapper<SysCodegenDatasource> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getDatasourceCode()),
                SysCodegenDatasource::getDatasourceCode, query.getDatasourceCode());
            wrapper.like(StringUtils.hasText(query.getDatasourceName()),
                SysCodegenDatasource::getDatasourceName, query.getDatasourceName());
            wrapper.eq(StringUtils.hasText(query.getDbType()),
                SysCodegenDatasource::getDbType, query.getDbType());
            wrapper.eq(query.getEnabled() != null,
                SysCodegenDatasource::getEnabled, query.getEnabled());
        }
        wrapper.orderByDesc(SysCodegenDatasource::getId);
        return wrapper;
    }

    private void validateSaveParam(CodegenDatasourceSaveParam param) {
        if (param == null || !StringUtils.hasText(param.getDatasourceCode())
            || !StringUtils.hasText(param.getDatasourceName())
            || !StringUtils.hasText(param.getJdbcUrl())
            || !StringUtils.hasText(param.getUsername())
            || !StringUtils.hasText(param.getDbType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_PARAM_EMPTY);
        }

        LambdaQueryWrapper<SysCodegenDatasource> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysCodegenDatasource::getDatasourceCode, param.getDatasourceCode());
        codeWrapper.ne(param.getId() != null, SysCodegenDatasource::getId, param.getId());
        if (datasourceMapper.selectCount(codeWrapper) > 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_CODE_EXISTS);
        }
    }

    private CodegenDatasourceDTO toDTO(SysCodegenDatasource entity) {
        CodegenDatasourceDTO dto = new CodegenDatasourceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
