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
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
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
import java.util.Map;

/**
 * 模块管理Controller
 * 
 * 接口规范：
 * - 所有接口统一使用 POST 方法
 * - 参数统一封装为对象
 * - 分页查询使用 BaseGetParam（pageNum/pageSize）
 * 
 * @author coder_nai@163.com
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
     */
    @PostMapping("/page")
    public R<IPage<SysModuleDTO>> page(@RequestBody SysModuleQueryDTO query) {
        // 使用 BaseGetParam 中的 pageNum 和 pageSize
        Page<SysModule> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(moduleService.pageModules(page, query));
    }
    
    /**
     * 查询所有模块列表（不分页）
     */
    @PostMapping("/list")
    public R<List<SysModuleDTO>> list(@RequestBody SysModuleQueryDTO query) {
        return R.ok(moduleService.listModules(query));
    }
    
    /**
     * 根据ID获取模块详情
     */
    @PostMapping("/detail")
    public R<SysModuleDTO> detail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        moduleValidator.validateId(id);
        return R.ok(moduleService.getModuleById(id));
    }
    
    /**
     * 新增模块
     */
    @RequirePerm("sys:module:create")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysModuleDTO moduleDTO) {
        moduleValidator.validateForAdd(moduleDTO);
        moduleService.addModule(moduleDTO);
        return R.ok();
    }
    
    /**
     * 更新模块
     */
    @RequirePerm("sys:module:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysModuleDTO moduleDTO) {
        moduleValidator.validateForUpdate(moduleDTO);
        moduleService.updateModule(moduleDTO);
        return R.ok();
    }
    
    /**
     * 删除模块
     */
    @RequirePerm("sys:module:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        moduleValidator.validateForDelete(id);
        moduleService.deleteModule(id);
        return R.ok();
    }
    
    /**
     * 批量删除模块
     */
    @RequirePerm("sys:module:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        // 逐个校验
        ids.forEach(moduleValidator::validateForDelete);
        moduleService.batchDeleteModules(ids);
        return R.ok();
    }
    
    /**
     * 解析Long类型参数
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
