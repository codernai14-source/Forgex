package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.dto.ThirdSystemDTO;
import com.forgex.integration.domain.entity.ApiOutboundTarget;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ApiOutboundTargetMapper;
import com.forgex.integration.service.IApiOutboundTargetService;
import com.forgex.integration.service.IThirdSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiOutboundTargetServiceImpl extends ServiceImpl<ApiOutboundTargetMapper, ApiOutboundTarget>
    implements IApiOutboundTargetService {

    private final IThirdSystemService thirdSystemService;

    @Override
    public List<ApiOutboundTargetDTO> listByApiConfigId(Long apiConfigId) {
        if (apiConfigId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ApiOutboundTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiOutboundTarget::getApiConfigId, apiConfigId);
        wrapper.eq(ApiOutboundTarget::getDeleted, false);
        wrapper.orderByAsc(ApiOutboundTarget::getOrderNum, ApiOutboundTarget::getId);
        return this.list(wrapper).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ApiOutboundTargetDTO> listEnabledByApiConfigId(Long apiConfigId) {
        LambdaQueryWrapper<ApiOutboundTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiOutboundTarget::getApiConfigId, apiConfigId);
        wrapper.eq(ApiOutboundTarget::getDeleted, false);
        wrapper.eq(ApiOutboundTarget::getStatus, 1);
        wrapper.orderByAsc(ApiOutboundTarget::getOrderNum, ApiOutboundTarget::getId);
        return this.list(wrapper).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceTargets(Long apiConfigId, List<ApiOutboundTargetDTO> targets) {
        if (apiConfigId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        LambdaQueryWrapper<ApiOutboundTarget> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(ApiOutboundTarget::getApiConfigId, apiConfigId);
        this.remove(removeWrapper);

        if (targets == null || targets.isEmpty()) {
            return;
        }

        int order = 1;
        for (ApiOutboundTargetDTO dto : targets) {
            if (dto == null || dto.getThirdSystemId() == null || !StringUtils.hasText(dto.getTargetUrl())) {
                continue;
            }
            ApiOutboundTarget entity = new ApiOutboundTarget();
            BeanUtils.copyProperties(dto, entity);
            entity.setId(null);
            entity.setApiConfigId(apiConfigId);
            entity.setOrderNum(dto.getOrderNum() == null ? order : dto.getOrderNum());
            entity.setTenantId(resolveTenantId());
            entity.setDeleted(Boolean.FALSE);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setCreateBy(resolveOperator());
            entity.setUpdateBy(resolveOperator());
            fillTargetInfo(entity);
            this.save(entity);
            order++;
        }
    }

    private void fillTargetInfo(ApiOutboundTarget entity) {
        ThirdSystemDTO system = thirdSystemService.getThirdSystemById(entity.getThirdSystemId());
        entity.setTargetCode(system.getSystemCode());
        entity.setTargetName(system.getSystemName());
    }

    private ApiOutboundTargetDTO toDto(ApiOutboundTarget entity) {
        ApiOutboundTargetDTO dto = new ApiOutboundTargetDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    private String resolveOperator() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException ex) {
            return "system";
        }
    }
}
