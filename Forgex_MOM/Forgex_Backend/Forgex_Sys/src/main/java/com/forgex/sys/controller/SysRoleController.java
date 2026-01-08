package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.domain.dto.SysRoleQueryDTO;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.service.ISysRoleService;
import com.forgex.sys.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理Controller
 * 
 * 职责：
 * - 接收HTTP请求
 * - 参数校验（调用Validator）
 * - 调用Service层方法
 * - 返回响应结果
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {
    
    private final ISysRoleService roleService;
    private final RoleValidator roleValidator;
    
    /**
     * 查询角色列表（兼容旧接口）
     */
    @PostMapping("/list")
    public R<List<SysRoleDTO>> list(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long tenantId = parseLong(body.get("tenantId"));
        if (tenantId == null) {
            return R.fail(500, "tenantId不能为空");
        }
        
        SysRoleQueryDTO query = new SysRoleQueryDTO();
        query.setTenantId(tenantId);
        
        // 2. 调用Service
        List<SysRoleDTO> list = roleService.listRoles(query);
        
        // 3. 返回结果
        return R.ok(list);
    }
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public R<IPage<SysRoleDTO>> page(Page<SysRole> page, SysRoleQueryDTO query) {
        return R.ok(roleService.pageRoles(page, query));
    }
    
    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    public R<List<SysRoleDTO>> listRoles(SysRoleQueryDTO query) {
        return R.ok(roleService.listRoles(query));
    }
    
    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{id}")
    public R<SysRoleDTO> getById(@PathVariable Long id) {
        roleValidator.validateId(id);
        return R.ok(roleService.getRoleById(id));
    }
    
    /**
     * 新增角色
     */
    @PostMapping
    public R<Void> add(@RequestBody @Validated SysRoleDTO roleDTO) {
        // 1. 数据校验
        roleValidator.validateForAdd(roleDTO);
        
        // 2. 调用Service
        roleService.addRole(roleDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 新增角色（兼容旧接口）
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody SysRoleDTO roleDTO) {
        roleValidator.validateForAdd(roleDTO);
        roleService.addRole(roleDTO);
        return R.ok(true);
    }
    
    /**
     * 更新角色
     */
    @PutMapping
    public R<Void> update(@RequestBody @Validated SysRoleDTO roleDTO) {
        // 1. 数据校验
        roleValidator.validateForUpdate(roleDTO);
        
        // 2. 调用Service
        roleService.updateRole(roleDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 更新角色（兼容旧接口）
     */
    @PostMapping("/update")
    public R<Boolean> updateOld(@RequestBody SysRoleDTO roleDTO) {
        roleValidator.validateForUpdate(roleDTO);
        roleService.updateRole(roleDTO);
        return R.ok(true);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 1. 数据校验
        roleValidator.validateForDelete(id);
        
        // 2. 调用Service
        roleService.deleteRole(id);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 删除角色（兼容旧接口）
     */
    @PostMapping("/delete")
    public R<Boolean> deleteOld(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        roleValidator.validateForDelete(id);
        roleService.deleteRole(id);
        return R.ok(true);
    }
    
    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        // 1. 校验每个ID
        for (Long id : ids) {
            roleValidator.validateForDelete(id);
        }
        
        // 2. 调用Service
        roleService.batchDeleteRoles(ids);
        
        // 3. 返回结果
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
