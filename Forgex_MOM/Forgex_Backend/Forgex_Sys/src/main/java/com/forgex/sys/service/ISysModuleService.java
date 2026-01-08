package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.domain.dto.SysModuleQueryDTO;
import com.forgex.sys.domain.entity.SysModule;

import java.util.List;

/**
 * 模块Service接口
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
public interface ISysModuleService extends IService<SysModule> {
    
    /**
     * 分页查询模块列表
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysModuleDTO> pageModules(Page<SysModule> page, SysModuleQueryDTO query);
    
    /**
     * 查询所有模块列表
     * 
     * @param query 查询条件
     * @return 模块列表
     */
    List<SysModuleDTO> listModules(SysModuleQueryDTO query);
    
    /**
     * 根据ID获取模块详情
     * 
     * @param id 模块ID
     * @return 模块详情
     */
    SysModuleDTO getModuleById(Long id);
    
    /**
     * 新增模块
     * 
     * @param moduleDTO 模块信息
     */
    void addModule(SysModuleDTO moduleDTO);
    
    /**
     * 更新模块
     * 
     * @param moduleDTO 模块信息
     */
    void updateModule(SysModuleDTO moduleDTO);
    
    /**
     * 删除模块
     * 
     * @param id 模块ID
     */
    void deleteModule(Long id);
    
    /**
     * 批量删除模块
     * 
     * @param ids 模块ID列表
     */
    void batchDeleteModules(List<Long> ids);
    
    /**
     * 检查模块编码是否存在
     * 
     * @param code 模块编码
     * @return 是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查模块编码是否存在（排除指定ID）
     * 
     * @param code 模块编码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean existsByCodeExcludeId(String code, Long excludeId);
    
    /**
     * 检查模块是否存在
     * 
     * @param id 模块ID
     * @return 是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查模块下是否有菜单
     * 
     * @param id 模块ID
     * @return 是否有菜单
     */
    boolean hasMenus(Long id);
}
