package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.entity.ApiParamMapping;
import com.forgex.integration.domain.param.ApiParamMappingParam;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ApiParamMappingMapper;
import com.forgex.integration.service.IApiParamMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口参数映射服务实现。
 * <p>
 * 负责维护源字段路径到目标字段路径的映射关系，并保证同一接口、出站目标和方向下源字段映射唯一。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiParamMappingService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiParamMappingServiceImpl extends ServiceImpl<ApiParamMappingMapper, ApiParamMapping>
    implements IApiParamMappingService {

    /**
     * 查询字段映射列表。
     *
     * @param param 查询参数
     * @return 字段映射 DTO 列表
     */
    @Override
    public List<ApiParamMappingDTO> listMappings(ApiParamMappingParam param) {
        if (param.getApiConfigId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        LambdaQueryWrapper<ApiParamMapping> wrapper = baseWrapper(param.getApiConfigId(), param.getOutboundTargetId(), param.getDirection());
        wrapper.like(hasText(param.getSourceFieldPath()), ApiParamMapping::getSourceFieldPath, param.getSourceFieldPath());
        wrapper.like(hasText(param.getTargetFieldPath()), ApiParamMapping::getTargetFieldPath, param.getTargetFieldPath());
        wrapper.orderByDesc(ApiParamMapping::getCreateTime);
        return this.list(wrapper).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询字段映射详情。
     *
     * @param id 映射 ID
     * @return 字段映射 DTO
     */
    @Override
    public ApiParamMappingDTO getById(Long id) {
        ApiParamMapping mapping = this.baseMapper.selectById(id);
        if (mapping == null || Boolean.TRUE.equals(mapping.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_NOT_FOUND);
        }
        return convertToDTO(mapping);
    }

    /**
     * 新增字段映射。
     *
     * @param dto 字段映射 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApiParamMappingDTO dto) {
        validate(dto);
        ApiParamMapping existing = getMappingByFields(dto.getApiConfigId(), dto.getOutboundTargetId(), dto.getSourceFieldPath(), dto.getDirection());
        if (existing != null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_EXISTS,
                dto.getSourceFieldPath(), dto.getDirection());
        }
        ApiParamMapping mapping = convertToEntity(dto);
        mapping.setCreateBy(resolveOperator());
        mapping.setUpdateBy(resolveOperator());
        if (!this.save(mapping)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_CREATE_FAILED);
        }
    }

    /**
     * 更新字段映射。
     *
     * @param dto 字段映射 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApiParamMappingDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        ApiParamMapping current = this.baseMapper.selectById(dto.getId());
        if (current == null || Boolean.TRUE.equals(current.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_NOT_FOUND);
        }
        validate(dto);
        ApiParamMapping same = getMappingByFields(dto.getApiConfigId(), dto.getOutboundTargetId(), dto.getSourceFieldPath(), dto.getDirection());
        if (same != null && !same.getId().equals(dto.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_EXISTS,
                dto.getSourceFieldPath(), dto.getDirection());
        }
        ApiParamMapping mapping = convertToEntity(dto);
        mapping.setUpdateTime(LocalDateTime.now());
        mapping.setUpdateBy(resolveOperator());
        if (!this.updateById(mapping)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_UPDATE_FAILED);
        }
    }

    /**
     * 逻辑删除字段映射。
     *
     * @param id 映射 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ApiParamMapping mapping = this.baseMapper.selectById(id);
        if (mapping == null || Boolean.TRUE.equals(mapping.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_NOT_FOUND);
        }
        mapping.setDeleted(Boolean.TRUE);
        mapping.setUpdateTime(LocalDateTime.now());
        mapping.setUpdateBy(resolveOperator());
        if (!this.updateById(mapping)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_MAPPING_DELETE_FAILED);
        }
    }

    /**
     * 批量删除数据。
     *
     * @param ids 映射 ID 列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 批量保存字段映射。
     * <p>
     * 当前接口、出站目标和方向下先清空旧映射，再逐条保存新映射。
     * </p>
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param dtos             字段映射列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long apiConfigId, Long outboundTargetId, String direction, List<ApiParamMappingDTO> dtos) {
        if (apiConfigId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        if (!StringUtils.hasText(direction)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_DIRECTION_REQUIRED);
        }
        LambdaQueryWrapper<ApiParamMapping> wrapper = baseWrapper(apiConfigId, outboundTargetId, direction);
        this.remove(wrapper);
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        for (ApiParamMappingDTO dto : dtos) {
            dto.setApiConfigId(apiConfigId);
            dto.setOutboundTargetId(outboundTargetId);
            dto.setDirection(direction);
            create(dto);
        }
    }

    /**
     * 构建字段映射基础查询条件。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @return 查询包装器
     */
    private LambdaQueryWrapper<ApiParamMapping> baseWrapper(Long apiConfigId, Long outboundTargetId, String direction) {
        LambdaQueryWrapper<ApiParamMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamMapping::getApiConfigId, apiConfigId);
        wrapper.eq(ApiParamMapping::getDeleted, false);
        wrapper.eq(hasText(direction), ApiParamMapping::getDirection, direction);
        if (outboundTargetId == null) {
            wrapper.isNull(ApiParamMapping::getOutboundTargetId);
        } else {
            wrapper.eq(ApiParamMapping::getOutboundTargetId, outboundTargetId);
        }
        return wrapper;
    }

    /**
     * 根据源字段路径查询映射。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param sourceFieldPath  源字段路径
     * @param direction        参数方向
     * @return 字段映射实体
     */
    private ApiParamMapping getMappingByFields(Long apiConfigId, Long outboundTargetId, String sourceFieldPath, String direction) {
        LambdaQueryWrapper<ApiParamMapping> wrapper = baseWrapper(apiConfigId, outboundTargetId, direction);
        wrapper.eq(ApiParamMapping::getSourceFieldPath, sourceFieldPath);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 校验字段映射参数。
     *
     * @param dto 字段映射 DTO
     */
    private void validate(ApiParamMappingDTO dto) {
        if (dto.getApiConfigId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        if (!hasText(dto.getSourceFieldPath())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_SOURCE_FIELD_REQUIRED);
        }
        if (!hasText(dto.getTargetFieldPath())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_TARGET_FIELD_REQUIRED);
        }
        if (!hasText(dto.getDirection())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_DIRECTION_REQUIRED);
        }
    }

    /**
     * DTO 转实体。
     *
     * @param dto 字段映射 DTO
     * @return 字段映射实体
     */
    private ApiParamMapping convertToEntity(ApiParamMappingDTO dto) {
        ApiParamMapping entity = new ApiParamMapping();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * 实体转 DTO。
     *
     * @param entity 字段映射实体
     * @return 字段映射 DTO
     */
    private ApiParamMappingDTO convertToDTO(ApiParamMapping entity) {
        ApiParamMappingDTO dto = new ApiParamMappingDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 解析当前操作人。
     *
     * @return 当前登录 ID，未登录时返回 system
     */
    private String resolveOperator() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException ex) {
            return "system";
        }
    }

    /**
     * 判断文本是否非空。
     *
     * @param value 文本值
     * @return true 表示非空
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
