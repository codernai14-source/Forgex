package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.BusinessException;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.entity.ApiParamMapping;
import com.forgex.integration.domain.param.ApiParamMappingParam;
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
 * 接口参数映射服务实现类
 * <p>
 * 提供接口参数映射关系的增删改查、批量保存等功能实现
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiParamMappingServiceImpl extends ServiceImpl<ApiParamMappingMapper, ApiParamMapping>
    implements IApiParamMappingService {

    private final ApiParamMappingMapper apiParamMappingMapper;

    @Override
    public List<ApiParamMappingDTO> listMappings(ApiParamMappingParam param) {
        if (param.getApiConfigId() == null) {
            throw new BusinessException("接口配置 ID 不能为空");
        }
        
        // 1. 构建查询条件
        LambdaQueryWrapper<ApiParamMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamMapping::getApiConfigId, param.getApiConfigId());
        wrapper.eq(StringUtils.hasText(param.getDirection()), ApiParamMapping::getDirection, param.getDirection());
        wrapper.like(StringUtils.hasText(param.getSourceFieldPath()), 
                   ApiParamMapping::getSourceFieldPath, param.getSourceFieldPath());
        wrapper.like(StringUtils.hasText(param.getTargetFieldPath()), 
                   ApiParamMapping::getTargetFieldPath, param.getTargetFieldPath());
        wrapper.orderByDesc(ApiParamMapping::getCreateTime);
        
        // 2. 查询列表
        List<ApiParamMapping> mappings = this.list(wrapper);
        
        // 3. 转换为 DTO
        return mappings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ApiParamMappingDTO getById(Long id) {
        if (id == null) {
            throw new BusinessException("参数映射 ID 不能为空");
        }
        
        ApiParamMapping mapping = this.baseMapper.selectById(id);
        if (mapping == null || mapping.getDeleted()) {
            throw new BusinessException("参数映射不存在");
        }
        
        return convertToDTO(mapping);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApiParamMappingDTO dto) {
        // 1. 参数校验
        if (dto.getApiConfigId() == null) {
            throw new BusinessException("接口配置 ID 不能为空");
        }
        if (!StringUtils.hasText(dto.getSourceFieldPath())) {
            throw new BusinessException("源字段路径不能为空");
        }
        if (!StringUtils.hasText(dto.getTargetFieldPath())) {
            throw new BusinessException("目标字段路径不能为空");
        }
        if (!StringUtils.hasText(dto.getDirection())) {
            throw new BusinessException("映射方向不能为空");
        }
        
        // 2. 校验映射关系唯一性
        ApiParamMapping existing = getMappingByFields(dto.getApiConfigId(), 
                                                       dto.getSourceFieldPath(), 
                                                       dto.getDirection());
        if (existing != null) {
            throw new BusinessException("映射关系已存在：源字段=" + dto.getSourceFieldPath() + 
                                      ", 方向=" + dto.getDirection());
        }
        
        // 3. 构建实体
        ApiParamMapping mapping = convertToEntity(dto);
        mapping.setCreateBy(getCurrentUsername());
        mapping.setUpdateBy(getCurrentUsername());
        
        // 4. 插入数据库
        boolean success = this.save(mapping);
        if (!success) {
            throw new BusinessException("创建参数映射失败");
        }
        
        log.info("创建参数映射成功：mappingId={}, source={}, target={}", 
                 mapping.getId(), mapping.getSourceFieldPath(), mapping.getTargetFieldPath());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApiParamMappingDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("参数映射 ID 不能为空");
        }
        
        // 1. 检查参数映射是否存在
        ApiParamMapping existing = this.baseMapper.selectById(dto.getId());
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException("参数映射不存在");
        }
        
        // 2. 参数校验
        if (!StringUtils.hasText(dto.getSourceFieldPath())) {
            throw new BusinessException("源字段路径不能为空");
        }
        if (!StringUtils.hasText(dto.getTargetFieldPath())) {
            throw new BusinessException("目标字段路径不能为空");
        }
        if (!StringUtils.hasText(dto.getDirection())) {
            throw new BusinessException("映射方向不能为空");
        }
        
        // 3. 校验映射关系唯一性（排除自身）
        ApiParamMapping sameMapping = getMappingByFields(dto.getApiConfigId(), 
                                                          dto.getSourceFieldPath(), 
                                                          dto.getDirection());
        if (sameMapping != null && !sameMapping.getId().equals(dto.getId())) {
            throw new BusinessException("映射关系已存在：源字段=" + dto.getSourceFieldPath() + 
                                      ", 方向=" + dto.getDirection());
        }
        
        // 4. 构建更新实体
        ApiParamMapping mapping = convertToEntity(dto);
        mapping.setUpdateTime(LocalDateTime.now());
        mapping.setUpdateBy(getCurrentUsername());
        
        // 5. 更新数据库
        boolean success = this.updateById(mapping);
        if (!success) {
            throw new BusinessException("更新参数映射失败");
        }
        
        log.info("更新参数映射成功：mappingId={}, source={}, target={}", 
                 mapping.getId(), mapping.getSourceFieldPath(), mapping.getTargetFieldPath());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("参数映射 ID 不能为空");
        }
        
        // 1. 检查参数映射是否存在
        ApiParamMapping mapping = this.baseMapper.selectById(id);
        if (mapping == null || mapping.getDeleted()) {
            throw new BusinessException("参数映射不存在");
        }
        
        // 2. 逻辑删除
        mapping.setDeleted(true);
        mapping.setUpdateTime(LocalDateTime.now());
        mapping.setUpdateBy(getCurrentUsername());
        
        boolean success = this.updateById(mapping);
        if (!success) {
            throw new BusinessException("删除参数映射失败");
        }
        
        log.info("删除参数映射成功：mappingId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除 ID 列表不能为空");
        }
        
        for (Long id : ids) {
            try {
                delete(id);
            } catch (BusinessException e) {
                log.warn("删除参数映射失败：ID={}, 原因：{}", id, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Long apiConfigId, String direction, List<ApiParamMappingDTO> dtos) {
        if (apiConfigId == null) {
            throw new BusinessException("接口配置 ID 不能为空");
        }
        if (!StringUtils.hasText(direction)) {
            throw new BusinessException("映射方向不能为空");
        }
        
        // 1. 删除该接口配置和方向下的所有映射关系
        LambdaQueryWrapper<ApiParamMapping> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ApiParamMapping::getApiConfigId, apiConfigId);
        deleteWrapper.eq(ApiParamMapping::getDirection, direction);
        this.remove(deleteWrapper);
        
        log.info("已删除接口配置{}方向{}的所有映射关系", apiConfigId, direction);
        
        // 2. 批量插入新映射
        if (dtos != null && !dtos.isEmpty()) {
            for (ApiParamMappingDTO dto : dtos) {
                try {
                    // 为每个映射设置公共字段
                    dto.setApiConfigId(apiConfigId);
                    dto.setDirection(direction);
                    
                    // 校验必填字段
                    if (!StringUtils.hasText(dto.getSourceFieldPath()) || 
                        !StringUtils.hasText(dto.getTargetFieldPath())) {
                        log.warn("跳过无效映射：source={}, target={}", 
                                dto.getSourceFieldPath(), dto.getTargetFieldPath());
                        continue;
                    }
                    
                    // 创建映射
                    create(dto);
                } catch (BusinessException e) {
                    log.warn("创建映射失败：source={}, target={}, 原因：{}", 
                            dto.getSourceFieldPath(), dto.getTargetFieldPath(), e.getMessage());
                }
            }
            
            log.info("批量保存参数映射成功：apiConfigId={}, direction={}, 数量={}", 
                     apiConfigId, direction, dtos.size());
        }
    }

    /**
     * 根据字段查询映射关系
     * <p>
     * 用于校验映射关系的唯一性
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param sourceFieldPath 源字段路径
     * @param direction 映射方向
     * @return 映射关系，不存在返回 null
     */
    private ApiParamMapping getMappingByFields(Long apiConfigId, String sourceFieldPath, String direction) {
        LambdaQueryWrapper<ApiParamMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamMapping::getApiConfigId, apiConfigId);
        wrapper.eq(ApiParamMapping::getSourceFieldPath, sourceFieldPath);
        wrapper.eq(ApiParamMapping::getDirection, direction);
        wrapper.select(ApiParamMapping::getId, ApiParamMapping::getSourceFieldPath);
        
        return this.getOne(wrapper);
    }

    /**
     * DTO 转 Entity
     */
    private ApiParamMapping convertToEntity(ApiParamMappingDTO dto) {
        ApiParamMapping mapping = new ApiParamMapping();
        BeanUtils.copyProperties(dto, mapping);
        return mapping;
    }

    /**
     * Entity 转 DTO
     */
    private ApiParamMappingDTO convertToDTO(ApiParamMapping mapping) {
        ApiParamMappingDTO dto = new ApiParamMappingDTO();
        BeanUtils.copyProperties(mapping, dto);
        return dto;
    }

    /**
     * 获取当前登录用户
     */
    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException e) {
            return "system";
        }
    }
}
