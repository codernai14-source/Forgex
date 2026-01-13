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
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.domain.dto.SysModuleQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.service.ISysModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模块Service实现类
 * <p>负责系统模块的增删改查、分页查询、批量删除等操作。</p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>模块分页查询</li>
 *   <li>模块列表查询</li>
 *   <li>根据ID查询模块</li>
 *   <li>新增模块</li>
 *   <li>更新模块</li>
 *   <li>删除模块</li>
 *   <li>批量删除模块</li>
 *   <li>验证模块是否存在</li>
 *   <li>验证模块编码是否存在</li>
 *   <li>检查模块是否关联菜单</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 * @see ISysModuleService
 */
@Service
@RequiredArgsConstructor
public class SysModuleServiceImpl extends ServiceImpl<SysModuleMapper, SysModule> 
    implements ISysModuleService {
    
    private final SysModuleMapper moduleMapper;
    private final SysMenuMapper menuMapper;
    private final com.forgex.sys.mapper.SysRoleMenuMapper roleMenuMapper;
    
    /**
     * 模块分页查询
     * <p>根据查询条件分页查询模块列表，并将结果转换为DTO返回。</p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>模块编码模糊查询</li>
     *   <li>模块名称模糊查询</li>
     *   <li>状态精确查询</li>
     *   <li>租户ID精确查询</li>
     * </ul>
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页模块DTO列表
     * @see #buildQueryWrapper(SysModuleQueryDTO)
     * @see #convertToDTO(SysModule)
     */
    @Override
    public IPage<SysModuleDTO> pageModules(Page<SysModule> page, SysModuleQueryDTO query) {
        LambdaQueryWrapper<SysModule> wrapper = buildQueryWrapper(query);
        IPage<SysModule> modulePage = moduleMapper.selectPage(page, wrapper);
        return modulePage.convert(this::convertToDTO);
    }
    
    /**
     * 模块列表查询
     * <p>根据查询条件查询模块列表，并将结果转换为DTO返回。</p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>模块编码模糊查询</li>
     *   <li>模块名称模糊查询</li>
     *   <li>状态精确查询</li>
     *   <li>租户ID精确查询</li>
     * </ul>
     * @param query 查询条件
     * @return 模块DTO列表
     * @see #buildQueryWrapper(SysModuleQueryDTO)
     * @see #convertToDTO(SysModule)
     */
    @Override
    public List<SysModuleDTO> listModules(SysModuleQueryDTO query) {
        LambdaQueryWrapper<SysModule> wrapper = buildQueryWrapper(query);
        List<SysModule> modules = moduleMapper.selectList(wrapper);
        return modules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 根据ID查询模块
     * <p>根据模块ID查询模块信息，并将结果转换为DTO返回。</p>
     * @param id 模块ID
     * @return 模块DTO，若不存在则返回null
     * @see #convertToDTO(SysModule)
     */
    @Override
    public SysModuleDTO getModuleById(Long id) {
        SysModule module = moduleMapper.selectById(id);
        return module != null ? convertToDTO(module) : null;
    }
    
    /**
     * 新增模块
     * <p>将模块DTO转换为实体，并插入数据库。</p>
     * @param moduleDTO 模块DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addModule(SysModuleDTO moduleDTO) {
        SysModule module = new SysModule();
        BeanUtils.copyProperties(moduleDTO, module);
        moduleMapper.insert(module);
    }
    
    /**
     * 更新模块
     * <p>将模块DTO转换为实体，并更新数据库。</p>
     * @param moduleDTO 模块DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModule(SysModuleDTO moduleDTO) {
        SysModule module = new SysModule();
        BeanUtils.copyProperties(moduleDTO, module);
        moduleMapper.updateById(module);
    }
    
    /**
     * 删除模块
     * <p>根据模块ID删除模块。</p>
     * @param id 模块ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModule(Long id) {
        moduleMapper.deleteById(id);
    }
    
    /**
     * 批量删除模块
     * <p>根据模块ID列表批量删除模块。</p>
     * @param ids 模块ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteModules(List<Long> ids) {
        moduleMapper.deleteBatchIds(ids);
    }
    
    /**
     * 根据编码验证模块是否存在
     * <p>根据模块编码验证模块是否存在。</p>
     * @param code 模块编码
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getCode, code);
        return moduleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 根据编码验证模块是否存在（排除指定ID）
     * <p>根据模块编码验证模块是否存在，排除指定ID的模块。</p>
     * @param code 模块编码
     * @param excludeId 排除的模块ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsByCodeExcludeId(String code, Long excludeId) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getCode, code);
        wrapper.ne(SysModule::getId, excludeId);
        return moduleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 验证模块是否存在
     * <p>根据模块ID验证模块是否存在。</p>
     * @param id 模块ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsById(Long id) {
        return moduleMapper.selectById(id) != null;
    }
    
    /**
     * 检查模块是否关联菜单
     * <p>检查指定模块是否关联了菜单。</p>
     * @param id 模块ID
     * @return 关联菜单返回true，否则返回false
     */
    @Override
    public boolean hasMenus(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getModuleId, id);
        return menuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查模块名称是否存在
     * <p>根据模块名称和租户ID检查模块是否存在。</p>
     * @param name 模块名称
     * @param tenantId 租户ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsByName(String name, Long tenantId) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getName, name);
        wrapper.eq(SysModule::getTenantId, tenantId);
        return moduleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查模块名称是否存在（排除指定ID）
     * <p>根据模块名称和租户ID检查模块是否存在，排除指定ID的模块。</p>
     * @param name 模块名称
     * @param tenantId 租户ID
     * @param excludeId 排除的模块ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsByNameExcludeId(String name, Long tenantId, Long excludeId) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getName, name);
        wrapper.eq(SysModule::getTenantId, tenantId);
        wrapper.ne(SysModule::getId, excludeId);
        return moduleMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查模块的菜单是否已被角色授权
     * <p>检查指定模块下的菜单是否已被角色授权。</p>
     * @param id 模块ID
     * @return 已被角色授权返回true，否则返回false
     */
    @Override
    public boolean hasRoleAssociationThroughMenus(Long id) {
        // 1. 查询该模块下的所有菜单ID
        LambdaQueryWrapper<SysMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.eq(SysMenu::getModuleId, id);
        menuWrapper.select(SysMenu::getId);
        List<SysMenu> menus = menuMapper.selectList(menuWrapper);
        
        if (menus.isEmpty()) {
            return false;
        }
        
        // 2. 提取菜单ID列表
        List<Long> menuIds = menus.stream()
            .map(SysMenu::getId)
            .collect(java.util.stream.Collectors.toList());
        
        // 3. 检查这些菜单是否被角色授权
        LambdaQueryWrapper<com.forgex.sys.domain.entity.SysRoleMenu> roleMenuWrapper = 
            new LambdaQueryWrapper<>();
        roleMenuWrapper.in(com.forgex.sys.domain.entity.SysRoleMenu::getMenuId, menuIds);
        
        return roleMenuMapper.selectCount(roleMenuWrapper) > 0;
    }
    
    /**
     * 构建查询条件
     * <p>根据查询DTO构建Lambda查询条件。</p>
     * @param query 查询条件DTO
     * @return Lambda查询条件
     */
    private LambdaQueryWrapper<SysModule> buildQueryWrapper(SysModuleQueryDTO query) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getCode()), 
                SysModule::getCode, query.getCode());
            wrapper.like(StringUtils.hasText(query.getName()), 
                SysModule::getName, query.getName());
            wrapper.eq(query.getStatus() != null, 
                SysModule::getStatus, query.getStatus());
            wrapper.eq(query.getTenantId() != null, 
                SysModule::getTenantId, query.getTenantId());
        }
        
        wrapper.orderByAsc(SysModule::getOrderNum);
        return wrapper;
    }
    
    /**
     * 实体转DTO
     * <p>将模块实体转换为DTO。</p>
     * @param module 模块实体
     * @return 模块DTO
     */
    private SysModuleDTO convertToDTO(SysModule module) {
        SysModuleDTO dto = new SysModuleDTO();
        BeanUtils.copyProperties(module, dto);
        return dto;
    }
}
