package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ThirdSystemDTO;
import com.forgex.integration.domain.entity.ThirdSystem;
import com.forgex.integration.domain.param.ThirdSystemParam;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ThirdSystemMapper;
import com.forgex.integration.service.IThirdSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 第三方系统信息服务实现类
 *
 * <p>提供第三方系统的增删改查等基础服务实现。</p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdSystemServiceImpl extends ServiceImpl<ThirdSystemMapper, ThirdSystem>
    implements IThirdSystemService {

    private final ThirdSystemMapper thirdSystemMapper;

    @Override
    public Page<ThirdSystemDTO> pageThirdSystems(ThirdSystemParam param) {
        Page<ThirdSystem> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<ThirdSystem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdSystem::getDeleted, 0);
        wrapper.eq(param.getStatus() != null, ThirdSystem::getStatus, param.getStatus());
        wrapper.like(param.getSystemCode() != null && !param.getSystemCode().isEmpty(),
            ThirdSystem::getSystemCode, param.getSystemCode());
        wrapper.like(param.getSystemName() != null && !param.getSystemName().isEmpty(),
            ThirdSystem::getSystemName, param.getSystemName());
        wrapper.orderByDesc(ThirdSystem::getCreateTime);

        Page<ThirdSystem> resultPage = this.page(page, wrapper);

        Page<ThirdSystemDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage, "records");
        dtoPage.setRecords(resultPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    public List<ThirdSystemDTO> listThirdSystems(ThirdSystemParam param) {
        LambdaQueryWrapper<ThirdSystem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdSystem::getDeleted, 0);
        wrapper.eq(param.getStatus() != null, ThirdSystem::getStatus, param.getStatus());
        wrapper.like(param.getSystemCode() != null && !param.getSystemCode().isEmpty(),
            ThirdSystem::getSystemCode, param.getSystemCode());
        wrapper.like(param.getSystemName() != null && !param.getSystemName().isEmpty(),
            ThirdSystem::getSystemName, param.getSystemName());
        wrapper.orderByDesc(ThirdSystem::getCreateTime);

        List<ThirdSystem> list = this.list(wrapper);
        return list.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ThirdSystemDTO getThirdSystemById(Long id) {
        ThirdSystem system = this.getById(id);
        if (system == null || Boolean.TRUE.equals(system.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_SYSTEM_NOT_FOUND);
        }
        return convertToDTO(system);
    }

    @Override
    public ThirdSystemDTO getBySystemCode(String systemCode) {
        Long tenantId = getCurrentTenantId();

        LambdaQueryWrapper<ThirdSystem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdSystem::getDeleted, 0);
        wrapper.eq(ThirdSystem::getSystemCode, systemCode);
        wrapper.eq(ThirdSystem::getTenantId, tenantId);
        wrapper.last("LIMIT 1");

        ThirdSystem system = thirdSystemMapper.selectOne(wrapper);
        return system != null ? convertToDTO(system) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createThirdSystem(ThirdSystemDTO dto) {
        ThirdSystemDTO existing = getBySystemCode(dto.getSystemCode());
        if (existing != null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.THIRD_SYSTEM_CODE_EXISTS, dto.getSystemCode());
        }

        ThirdSystem system = new ThirdSystem();
        BeanUtils.copyProperties(dto, system);

        LocalDateTime now = LocalDateTime.now();
        String currentUsername = getCurrentUsername();
        system.setTenantId(getCurrentTenantId());
        system.setCreateTime(now);
        system.setUpdateTime(now);
        system.setCreateBy(currentUsername);
        system.setUpdateBy(currentUsername);

        boolean success = this.save(system);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_SYSTEM_CREATE_FAILED);
        }

        log.info("创建第三方系统成功：{}", dto.getSystemCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThirdSystem(ThirdSystemDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }

        ThirdSystem existing = this.getById(dto.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_SYSTEM_NOT_FOUND);
        }

        ThirdSystemDTO sameCode = getBySystemCode(dto.getSystemCode());
        if (sameCode != null && !sameCode.getId().equals(dto.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.THIRD_SYSTEM_CODE_EXISTS, dto.getSystemCode());
        }

        ThirdSystem system = new ThirdSystem();
        BeanUtils.copyProperties(dto, system);
        system.setTenantId(existing.getTenantId());
        system.setUpdateTime(LocalDateTime.now());
        system.setUpdateBy(getCurrentUsername());

        boolean success = this.updateById(system);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_SYSTEM_UPDATE_FAILED);
        }

        log.info("更新第三方系统成功：{}", dto.getSystemCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThirdSystem(Long id) {
        ThirdSystem system = this.getById(id);
        if (system == null || Boolean.TRUE.equals(system.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_SYSTEM_NOT_FOUND);
        }

        system.setDeleted(true);
        system.setUpdateTime(LocalDateTime.now());
        system.setUpdateBy(getCurrentUsername());

        boolean success = this.updateById(system);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_SYSTEM_DELETE_FAILED);
        }

        log.info("删除第三方系统成功：ID={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteThirdSystems(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }

        for (Long id : ids) {
            try {
                deleteThirdSystem(id);
            } catch (I18nBusinessException e) {
                log.warn("删除第三方系统失败：ID={}, 原因：{}", id, e.getMessage());
            }
        }
    }

    private ThirdSystemDTO convertToDTO(ThirdSystem system) {
        ThirdSystemDTO dto = new ThirdSystemDTO();
        BeanUtils.copyProperties(system, dto);
        return dto;
    }

    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId != null ? tenantId : 0L;
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException e) {
            log.warn("当前用户未登录，使用系统默认用户");
            return "system";
        }
    }
}
