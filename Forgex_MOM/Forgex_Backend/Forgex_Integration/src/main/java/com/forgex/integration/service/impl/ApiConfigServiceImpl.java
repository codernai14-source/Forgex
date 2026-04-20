package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.common.tenant.TenantContext;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.entity.ApiConfig;
import com.forgex.integration.domain.param.ApiConfigParam;
import com.forgex.integration.mapper.ApiConfigMapper;
import com.forgex.integration.service.IApiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口配置信息服务实现类
 * <p>
 * 提供接口配置的增删改查、启用/停用等基础服务实现
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConfigServiceImpl extends ServiceImpl<ApiConfigMapper, ApiConfig> 
    implements IApiConfigService {

    private final ApiConfigMapper apiConfigMapper;

    @Override
    public Page<ApiConfigDTO> pageApiConfigs(ApiConfigParam param) {
        Page<ApiConfig> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, 0);
        wrapper.eq(param.getStatus() != null, ApiConfig::getStatus, param.getStatus());
        wrapper.like(param.getApiCode() != null && !param.getApiCode().isEmpty(), 
                     ApiConfig::getApiCode, param.getApiCode());
        wrapper.like(param.getApiName() != null && !param.getApiName().isEmpty(), 
                     ApiConfig::getApiName, param.getApiName());
        wrapper.eq(param.getModuleCode() != null && !param.getModuleCode().isEmpty(), 
                   ApiConfig::getModuleCode, param.getModuleCode());
        wrapper.eq(param.getDirection() != null && !param.getDirection().isEmpty(), 
                   ApiConfig::getDirection, param.getDirection());
        wrapper.orderByDesc(ApiConfig::getCreateTime);
        
        Page<ApiConfig> resultPage = this.page(page, wrapper);
        
        Page<ApiConfigDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage, "records");
        dtoPage.setRecords(resultPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
        
        return dtoPage;
    }

    @Override
    public List<ApiConfigDTO> listApiConfigs(ApiConfigParam param) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, 0);
        wrapper.eq(param.getStatus() != null, ApiConfig::getStatus, param.getStatus());
        wrapper.like(param.getApiCode() != null && !param.getApiCode().isEmpty(), 
                     ApiConfig::getApiCode, param.getApiCode());
        wrapper.like(param.getApiName() != null && !param.getApiName().isEmpty(), 
                     ApiConfig::getApiName, param.getApiName());
        wrapper.eq(param.getModuleCode() != null && !param.getModuleCode().isEmpty(), 
                   ApiConfig::getModuleCode, param.getModuleCode());
        wrapper.eq(param.getDirection() != null && !param.getDirection().isEmpty(), 
                   ApiConfig::getDirection, param.getDirection());
        wrapper.orderByDesc(ApiConfig::getCreateTime);
        
        List<ApiConfig> list = this.list(wrapper);
        return list.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ApiConfigDTO getApiConfigById(Long id) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        return convertToDTO(config);
    }

    @Override
    public ApiConfigDTO getByApiCode(String apiCode) {
        Long tenantId = getCurrentTenantId();
        
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, 0);
        wrapper.eq(ApiConfig::getApiCode, apiCode);
        wrapper.eq(ApiConfig::getTenantId, tenantId);
        wrapper.last("LIMIT 1");
        
        ApiConfig config = apiConfigMapper.selectOne(wrapper);
        return config != null ? convertToDTO(config) : null;
    }

    @Override
    public ApiConfigDTO getByProcessorBean(String processorBean) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getDeleted, 0);
        wrapper.eq(ApiConfig::getProcessorBean, processorBean);
        wrapper.eq(ApiConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        
        ApiConfig config = apiConfigMapper.selectOne(wrapper);
        return config != null ? convertToDTO(config) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiConfigDTO createApiConfig(ApiConfigDTO dto) {
        // 校验接口编码唯一性
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
        
        log.info("创建接口配置成功：{}", dto.getApiCode());
        return convertToDTO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiConfigDTO updateApiConfig(ApiConfigDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        // 校验配置是否存在
        ApiConfig existing = this.getById(dto.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        
        // 校验接口编码唯一性（排除自身）
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
        
        log.info("更新接口配置成功：{}", dto.getApiCode());
        return getApiConfigById(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApiConfig(Long id) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        
        config.setDeleted(true);
        // 不需要手动设置 updateTime 和 updateBy，MyBatis-Plus 会自动填充
        
        boolean success = this.updateById(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_DELETE_FAILED);
        }
        
        log.info("删除接口配置成功：ID={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteApiConfigs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }
        
        for (Long id : ids) {
            try {
                deleteApiConfig(id);
            } catch (I18nBusinessException e) {
                log.warn("删除接口配置失败：ID={}, 原因：{}", id, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableApiConfig(Long id) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        
        config.setStatus(1);
        // 不需要手动设置 updateTime 和 updateBy，MyBatis-Plus 会自动填充
        
        boolean success = this.updateById(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_ENABLE_FAILED);
        }
        
        log.info("启用接口配置成功：ID={}, apiCode={}", id, config.getApiCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableApiConfig(Long id) {
        ApiConfig config = this.getById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND);
        }
        
        config.setStatus(0);
        // 不需要手动设置 updateTime 和 updateBy，MyBatis-Plus 会自动填充
        
        boolean success = this.updateById(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_DISABLE_FAILED);
        }
        
        log.info("停用接口配置成功：ID={}, apiCode={}", id, config.getApiCode());
    }

    /**
     * 实体转 DTO
     *
     * @param config 接口配置实体
     * @return 接口配置 DTO
     */
    private ApiConfigDTO convertToDTO(ApiConfig config) {
        ApiConfigDTO dto = new ApiConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }

    /**
     * 获取当前租户 ID
     *
     * @return 当前租户 ID，默认返回 0L
     */
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
}
