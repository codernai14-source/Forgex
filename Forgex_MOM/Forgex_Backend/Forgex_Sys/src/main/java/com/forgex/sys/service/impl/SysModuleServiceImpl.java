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
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Service
@RequiredArgsConstructor
public class SysModuleServiceImpl extends ServiceImpl<SysModuleMapper, SysModule> 
    implements ISysModuleService {
    
    private final SysModuleMapper moduleMapper;
    private final SysMenuMapper menuMapper;
    
    @Override
    public IPage<SysModuleDTO> pageModules(Page<SysModule> page, SysModuleQueryDTO query) {
        LambdaQueryWrapper<SysModule> wrapper = buildQueryWrapper(query);
        IPage<SysModule> modulePage = moduleMapper.selectPage(page, wrapper);
        return modulePage.convert(this::convertToDTO);
    }
    
    @Override
    public List<SysModuleDTO> listModules(SysModuleQueryDTO query) {
        LambdaQueryWrapper<SysModule> wrapper = buildQueryWrapper(query);
        List<SysModule> modules = moduleMapper.selectList(wrapper);
        return modules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public SysModuleDTO getModuleById(Long id) {
        SysModule module = moduleMapper.selectById(id);
        return module != null ? convertToDTO(module) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addModule(SysModuleDTO moduleDTO) {
        SysModule module = new SysModule();
        BeanUtils.copyProperties(moduleDTO, module);
        moduleMapper.insert(module);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModule(SysModuleDTO moduleDTO) {
        SysModule module = new SysModule();
        BeanUtils.copyProperties(moduleDTO, module);
        moduleMapper.updateById(module);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModule(Long id) {
        moduleMapper.deleteById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteModules(List<Long> ids) {
        moduleMapper.deleteBatchIds(ids);
    }
    
    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getCode, code);
        return moduleMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsByCodeExcludeId(String code, Long excludeId) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getCode, code);
        wrapper.ne(SysModule::getId, excludeId);
        return moduleMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsById(Long id) {
        return moduleMapper.selectById(id) != null;
    }
    
    @Override
    public boolean hasMenus(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getModuleId, id);
        return menuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 构建查询条件
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
     */
    private SysModuleDTO convertToDTO(SysModule module) {
        SysModuleDTO dto = new SysModuleDTO();
        BeanUtils.copyProperties(module, dto);
        return dto;
    }
}
