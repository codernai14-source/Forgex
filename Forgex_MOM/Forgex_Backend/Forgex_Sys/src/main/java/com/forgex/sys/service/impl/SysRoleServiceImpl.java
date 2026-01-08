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
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.domain.dto.SysRoleQueryDTO;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色Service实现类
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> 
    implements ISysRoleService {
    
    private final SysRoleMapper roleMapper;
    
    @Override
    public IPage<SysRoleDTO> pageRoles(Page<SysRole> page, SysRoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = buildQueryWrapper(query);
        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);
        return rolePage.convert(this::convertToDTO);
    }
    
    @Override
    public List<SysRoleDTO> listRoles(SysRoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = buildQueryWrapper(query);
        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roles.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public SysRoleDTO getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        return role != null ? convertToDTO(role) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRoleDTO roleDTO) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        roleMapper.insert(role);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRoleDTO roleDTO) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        roleMapper.updateById(role);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        roleMapper.deleteById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoles(List<Long> ids) {
        roleMapper.deleteBatchIds(ids);
    }
    
    @Override
    public boolean existsById(Long id) {
        return roleMapper.selectById(id) != null;
    }
    
    @Override
    public boolean existsByRoleKey(String roleKey) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsByRoleKeyExcludeId(String roleKey, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        wrapper.ne(SysRole::getId, excludeId);
        return roleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysRole> buildQueryWrapper(SysRoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getRoleName()), 
                SysRole::getRoleName, query.getRoleName());
            wrapper.like(StringUtils.hasText(query.getRoleKey()), 
                SysRole::getRoleKey, query.getRoleKey());
            wrapper.eq(query.getStatus() != null, 
                SysRole::getStatus, query.getStatus());
            wrapper.eq(query.getTenantId() != null, 
                SysRole::getTenantId, query.getTenantId());
        }
        
        wrapper.orderByDesc(SysRole::getCreateTime);
        return wrapper;
    }
    
    /**
     * 实体转DTO
     */
    private SysRoleDTO convertToDTO(SysRole role) {
        SysRoleDTO dto = new SysRoleDTO();
        BeanUtils.copyProperties(role, dto);
        return dto;
    }
}
