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
import com.forgex.common.enums.TenantTypeEnum;
import com.forgex.sys.domain.dto.tenant.SysTenantDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantQueryDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantSaveParam;
import com.forgex.sys.domain.entity.SysTenant;
import com.forgex.sys.mapper.SysTenantMapper;
import com.forgex.sys.service.SysTenantService;
import com.forgex.sys.service.ITenantInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户Service实现
 * <p>
 * 提供租户管理的业务逻辑实现，包含租户的增删改查功能
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.service.SysTenantService
 * @see com.forgex.sys.domain.entity.SysTenant
 * @see com.forgex.common.enums.TenantTypeEnum
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl implements SysTenantService {
    
    private final SysTenantMapper tenantMapper;
    private final ITenantInitService tenantInitService;
    
    @Override
    public List<SysTenantDTO> list(SysTenantQueryDTO queryDTO) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getDeleted, false);
        
        // 租户名称模糊查询
        if (StringUtils.hasText(queryDTO.getTenantName())) {
            wrapper.like(SysTenant::getTenantName, queryDTO.getTenantName());
        }
        
        // 租户编码模糊查询
        if (StringUtils.hasText(queryDTO.getTenantCode())) {
            wrapper.like(SysTenant::getTenantCode, queryDTO.getTenantCode());
        }
        
        // 租户类别
        if (queryDTO.getTenantType() != null) {
            wrapper.eq(SysTenant::getTenantType, queryDTO.getTenantType());
        }
        
        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysTenant::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(SysTenant::getCreateTime);
        
        List<SysTenant> tenants = tenantMapper.selectList(wrapper);
        
        return tenants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SysTenantDTO getById(Long id) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getId, id)
               .eq(SysTenant::getDeleted, false);
        
        SysTenant tenant = tenantMapper.selectOne(wrapper);
        
        if (tenant == null) {
            return null;
        }
        
        return convertToDTO(tenant);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysTenantSaveParam param) {
        // 检查租户编码是否重复
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantCode, param.getTenantCode())
               .eq(SysTenant::getDeleted, false);
        
        Long count = tenantMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("租户编码已存在");
        }
        
        // 如果是主租户，检查系统中是否已存在主租户
        if (TenantTypeEnum.MAIN_TENANT.equals(param.getTenantType())) {
            LambdaQueryWrapper<SysTenant> mainTenantWrapper = new LambdaQueryWrapper<>();
            mainTenantWrapper.eq(SysTenant::getTenantType, TenantTypeEnum.MAIN_TENANT)
                            .eq(SysTenant::getDeleted, false);
            
            Long mainTenantCount = tenantMapper.selectCount(mainTenantWrapper);
            if (mainTenantCount > 0) {
                throw new RuntimeException("系统中已存在主租户，不允许创建多个主租户");
            }
        }
        
        // 创建租户
        SysTenant tenant = new SysTenant();
        BeanUtils.copyProperties(param, tenant);
        
        // 设置默认值
        if (tenant.getStatus() == null) {
            tenant.setStatus(true);
        }
        
        tenantMapper.insert(tenant);
        
        log.info("创建租户成功，租户ID：{}，租户名称：{}，租户类别：{}", 
                tenant.getId(), tenant.getTenantName(), tenant.getTenantType());
        
        // 初始化租户数据（模块、菜单、角色、用户）
        tenantInitService.initTenant(tenant.getId(), tenant.getTenantName(), param.getTenantType());
        
        return tenant.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysTenantSaveParam param) {
        if (param.getId() == null) {
            throw new RuntimeException("租户ID不能为空");
        }
        
        // 检查租户是否存在
        SysTenant existTenant = tenantMapper.selectById(param.getId());
        if (existTenant == null || existTenant.getDeleted()) {
            throw new RuntimeException("租户不存在");
        }
        
        // 检查租户编码是否重复（排除自己）
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantCode, param.getTenantCode())
               .eq(SysTenant::getDeleted, false)
               .ne(SysTenant::getId, param.getId());
        
        Long count = tenantMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("租户编码已存在");
        }
        
        // 如果修改为其他类型的主租户，检查系统中是否已存在主租户（排除自己）
        if (TenantTypeEnum.MAIN_TENANT.equals(param.getTenantType()) 
                && !TenantTypeEnum.MAIN_TENANT.equals(existTenant.getTenantType())) {
            LambdaQueryWrapper<SysTenant> mainTenantWrapper = new LambdaQueryWrapper<>();
            mainTenantWrapper.eq(SysTenant::getTenantType, TenantTypeEnum.MAIN_TENANT)
                            .eq(SysTenant::getDeleted, false)
                            .ne(SysTenant::getId, param.getId());
            
            Long mainTenantCount = tenantMapper.selectCount(mainTenantWrapper);
            if (mainTenantCount > 0) {
                throw new RuntimeException("系统中已存在主租户，不允许创建多个主租户");
            }
        }
        
        // 更新租户
        SysTenant tenant = new SysTenant();
        BeanUtils.copyProperties(param, tenant);
        
        int rows = tenantMapper.updateById(tenant);
        
        log.info("更新租户成功，租户ID：{}，租户名称：{}，租户类别：{}", 
                tenant.getId(), tenant.getTenantName(), tenant.getTenantType());
        
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        // 检查租户是否存在
        SysTenant existTenant = tenantMapper.selectById(id);
        if (existTenant == null || existTenant.getDeleted()) {
            throw new RuntimeException("租户不存在");
        }
        
        // 检查是否为主租户，主租户不允许删除
        if (TenantTypeEnum.MAIN_TENANT.equals(existTenant.getTenantType())) {
            throw new RuntimeException("主租户不允许删除");
        }
        
        // TODO: 检查租户下是否存在用户
        
        // 逻辑删除
        SysTenant tenant = new SysTenant();
        tenant.setId(id);
        tenant.setDeleted(true);
        
        int rows = tenantMapper.updateById(tenant);
        
        log.info("删除租户成功，租户ID：{}", id);
        
        return rows > 0;
    }
    
    @Override
    public SysTenantDTO getMainTenant() {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantType, TenantTypeEnum.MAIN_TENANT)
               .eq(SysTenant::getDeleted, false)
               .last("LIMIT 1");
        
        SysTenant tenant = tenantMapper.selectOne(wrapper);
        
        if (tenant == null) {
            return null;
        }
        
        return convertToDTO(tenant);
    }
    
    /**
     * 实体转DTO
     * 
     * @param tenant 租户实体
     * @return 租户DTO
     */
    private SysTenantDTO convertToDTO(SysTenant tenant) {
        SysTenantDTO dto = new SysTenantDTO();
        BeanUtils.copyProperties(tenant, dto);
        
        // 设置租户类别描述
        if (tenant.getTenantType() != null) {
            dto.setTenantTypeDesc(tenant.getTenantType().getDesc());
        }
        
        return dto;
    }
}
