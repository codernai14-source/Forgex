package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.domain.dto.SysModuleQueryDTO;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.service.ISysModuleService;
import com.forgex.sys.validator.ModuleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模块管理Controller
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/module")
@RequiredArgsConstructor
public class SysModuleController {
    
    private final ISysModuleService moduleService;
    private final ModuleValidator moduleValidator;
    
    /**
     * 分页查询模块列表
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<IPage<SysModuleDTO>> page(Page<SysModule> page, SysModuleQueryDTO query) {
        return R.ok(moduleService.pageModules(page, query));
    }
    
    /**
     * 查询所有模块列表
     * 
     * @param query 查询条件
     * @return 模块列表
     */
    @GetMapping("/list")
    public R<List<SysModuleDTO>> list(SysModuleQueryDTO query) {
        return R.ok(moduleService.listModules(query));
    }
    
    /**
     * 根据ID获取模块详情
     * 
     * @param id 模块ID
     * @return 模块详情
     */
    @GetMapping("/{id}")
    public R<SysModuleDTO> getById(@PathVariable Long id) {
        moduleValidator.validateId(id);
        return R.ok(moduleService.getModuleById(id));
    }
    
    /**
     * 新增模块
     * 
     * @param moduleDTO 模块信息
     * @return 操作结果
     */
    @PostMapping
    public R<Void> add(@RequestBody @Validated SysModuleDTO moduleDTO) {
        moduleValidator.validateForAdd(moduleDTO);
        moduleService.addModule(moduleDTO);
        return R.ok();
    }
    
    /**
     * 更新模块
     * 
     * @param moduleDTO 模块信息
     * @return 操作结果
     */
    @PutMapping
    public R<Void> update(@RequestBody @Validated SysModuleDTO moduleDTO) {
        moduleValidator.validateForUpdate(moduleDTO);
        moduleService.updateModule(moduleDTO);
        return R.ok();
    }
    
    /**
     * 删除模块
     * 
     * @param id 模块ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        moduleValidator.validateForDelete(id);
        moduleService.deleteModule(id);
        return R.ok();
    }
    
    /**
     * 批量删除模块
     * 
     * @param ids 模块ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        // 逐个校验
        ids.forEach(moduleValidator::validateForDelete);
        moduleService.batchDeleteModules(ids);
        return R.ok();
    }
}
