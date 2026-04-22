package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.entity.ApiConfig;
import com.forgex.integration.domain.param.ApiConfigParam;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ApiConfigMapper;
import com.forgex.integration.service.IApiConfigService;
import com.forgex.integration.service.IApiOutboundTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口配置服务实现。
 * <p>
 * 负责第三方接口配置的分页查询、列表查询、详情、新增、编辑、删除以及启停用。
 * 保存接口配置时同步维护出站目标配置。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiConfigService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConfigServiceImpl extends ServiceImpl<ApiConfigMapper, ApiConfig> implements IApiConfigService {

    /**
     * 接口配置 Mapper。
     */
    private final ApiConfigMapper apiConfigMapper;

    /**
     * 出站目标服务。
     */
    private final IApiOutboundTargetService apiOutboundTargetService;

    /**
     * 分页查询接口配置。
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @Override
    public Page<ApiConfigDTO> pageApiConfigs(ApiConfigParam param) {
        Page<ApiConfig> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, false);
        wrapper.eq(param.getStatus() != null, ApiConfig::getStatus, param.getStatus());
        wrapper.like(hasText(param.getApiCode()), ApiConfig::getApiCode, param.getApiCode());
        wrapper.like(hasText(param.getApiName()), ApiConfig::getApiName, param.getApiName());
        wrapper.eq(hasText(param.getModuleCode()), ApiConfig::getModuleCode, param.getModuleCode());
        wrapper.eq(hasText(param.getDirection()), ApiConfig::getDirection, param.getDirection());
        wrapper.orderByDesc(ApiConfig::getCreateTime);

        Page<ApiConfig> resultPage = this.page(page, wrapper);
        Page<ApiConfigDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage, "records");
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 查询接口配置列表。
     *
     * @param param 查询参数
     * @return 接口配置列表
     */
    @Override
    public List<ApiConfigDTO> listApiConfigs(ApiConfigParam param) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, false);
        wrapper.eq(param.getStatus() != null, ApiConfig::getStatus, param.getStatus());
        wrapper.like(hasText(param.getApiCode()), ApiConfig::getApiCode, param.getApiCode());
        wrapper.like(hasText(param.getApiName()), ApiConfig::getApiName, param.getApiName());
        wrapper.eq(hasText(param.getModuleCode()), ApiConfig::getModuleCode, param.getModuleCode());
        wrapper.eq(hasText(param.getDirection()), ApiConfig::getDirection, param.getDirection());
        wrapper.orderByDesc(ApiConfig::getCreateTime);
        return this.list(wrapper).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询接口配置。
     *
     * @param id 接口配置 ID
     * @return 接口配置 DTO
     */
    @Override
    public ApiConfigDTO getApiConfigById(Long id) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        return convertToDTO(config);
    }

    /**
     * 根据接口编码查询配置。
     *
     * @param apiCode 接口编码
     * @return 接口配置 DTO，不存在时返回 null
     */
    @Override
    public ApiConfigDTO getByApiCode(String apiCode) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, false);
        wrapper.eq(ApiConfig::getApiCode, apiCode);
        wrapper.eq(ApiConfig::getTenantId, getCurrentTenantId());
        wrapper.last("LIMIT 1");
        ApiConfig config = apiConfigMapper.selectOne(wrapper);
        return config == null ? null : convertToDTO(config);
    }

    /**
     * 根据处理器 Bean 查询启用的接口配置。
     *
     * @param processorBean 处理器 Bean 名称
     * @return 接口配置 DTO，不存在时返回 null
     */
    @Override
    public ApiConfigDTO getByProcessorBean(String processorBean) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, false);
        wrapper.eq(ApiConfig::getProcessorBean, processorBean);
        wrapper.eq(ApiConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        ApiConfig config = apiConfigMapper.selectOne(wrapper);
        return config == null ? null : convertToDTO(config);
    }

    /**
     * 新增接口配置。
     *
     * @param dto 接口配置 DTO
     * @return 新增后的接口配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiConfigDTO createApiConfig(ApiConfigDTO dto) {
        ApiConfigDTO existing = getByApiCode(dto.getApiCode());
        if (existing != null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CODE_EXISTS, dto.getApiCode());
        }

        ApiConfig config = new ApiConfig();
        BeanUtils.copyProperties(dto, config);
        fillAuditFieldsOnCreate(config);
        boolean success = this.save(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_CREATE_FAILED);
        }
        apiOutboundTargetService.replaceTargets(config.getId(), dto.getOutboundTargets());
        return getApiConfigById(config.getId());
    }

    /**
     * 更新接口配置。
     *
     * @param dto 接口配置 DTO
     * @return 更新后的接口配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiConfigDTO updateApiConfig(ApiConfigDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        ApiConfig existing = this.getById(dto.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        ApiConfigDTO sameCode = getByApiCode(dto.getApiCode());
        if (sameCode != null && !sameCode.getId().equals(dto.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CODE_EXISTS, dto.getApiCode());
        }

        ApiConfig config = new ApiConfig();
        BeanUtils.copyProperties(dto, config);
        fillAuditFieldsOnUpdate(config, existing);
        boolean success = this.updateById(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_UPDATE_FAILED);
        }
        apiOutboundTargetService.replaceTargets(dto.getId(), dto.getOutboundTargets());
        return getApiConfigById(dto.getId());
    }

    /**
     * 逻辑删除接口配置。
     *
     * @param id 接口配置 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApiConfig(Long id) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        config.setDeleted(Boolean.TRUE);
        if (!this.updateById(config)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_DELETE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteApiConfigs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }
        for (Long id : ids) {
            deleteApiConfig(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableApiConfig(Long id) {
        updateStatus(id, 1, IntegrationPromptEnum.API_CONFIG_ENABLE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableApiConfig(Long id) {
        updateStatus(id, 0, IntegrationPromptEnum.API_CONFIG_DISABLE_FAILED);
    }

    private void updateStatus(Long id, int status, IntegrationPromptEnum prompt) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        config.setStatus(status);
        if (!this.updateById(config)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt);
        }
    }

    private ApiConfigDTO convertToDTO(ApiConfig config) {
        ApiConfigDTO dto = new ApiConfigDTO();
        BeanUtils.copyProperties(config, dto);
        List<ApiOutboundTargetDTO> targets = apiOutboundTargetService.listByApiConfigId(config.getId());
        dto.setOutboundTargets(targets);
        return dto;
    }

    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId != null ? tenantId : 0L;
    }

    private void fillAuditFieldsOnCreate(ApiConfig config) {
        LocalDateTime now = LocalDateTime.now();
        if (config.getTenantId() == null) {
            config.setTenantId(getCurrentTenantId());
        }
        if (config.getCreateTime() == null) {
            config.setCreateTime(now);
        }
        if (config.getUpdateTime() == null) {
            config.setUpdateTime(now);
        }
        if (config.getDeleted() == null) {
            config.setDeleted(Boolean.FALSE);
        }
        String operator = resolveCurrentOperator();
        if (config.getCreateBy() == null) {
            config.setCreateBy(operator);
        }
        if (config.getUpdateBy() == null) {
            config.setUpdateBy(operator);
        }
    }

    private void fillAuditFieldsOnUpdate(ApiConfig config, ApiConfig existing) {
        config.setTenantId(existing.getTenantId());
        config.setCreateTime(existing.getCreateTime());
        config.setCreateBy(existing.getCreateBy());
        config.setDeleted(existing.getDeleted());
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(resolveCurrentOperator());
    }

    private String resolveCurrentOperator() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId != null ? String.valueOf(loginId) : "system";
        } catch (NotLoginException ex) {
            return "system";
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
