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
package com.forgex.sys.validator;

import com.forgex.common.exception.BusinessException;
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 角色数据校验器
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Component
@RequiredArgsConstructor
public class RoleValidator {
    
    private final ISysRoleService roleService;
    
    /**
     * 新增角色校验（任务 14）
     * 
     * @param roleDTO 角色信息
     */
    public void validateForAdd(SysRoleDTO roleDTO) {
        // 1. 必填项校验
        Assert.hasText(roleDTO.getRoleName(), "角色名称不能为空");
        Assert.hasText(roleDTO.getRoleCode(), "角色编码不能为空");
        Assert.notNull(roleDTO.getTenantId(), "租户ID不能为空");
        
        // 2. 角色编码唯一性校验（任务 14）
        if (roleService.existsByRoleCode(roleDTO.getRoleCode(), roleDTO.getTenantId())) {
            throw new BusinessException("角色编码已存在");
        }
        
        // 3. 角色名称唯一性校验（任务 14）
        if (roleService.existsByRoleName(roleDTO.getRoleName(), roleDTO.getTenantId())) {
            throw new BusinessException("角色名称已存在");
        }
        
        // 4. 角色键唯一性校验（保留原有逻辑）
        if (roleService.existsByRoleKey(roleDTO.getRoleKey())) {
            throw new BusinessException("角色键已存在");
        }
    }
    
    /**
     * 更新角色校验（任务 14、15）
     * 
     * @param roleDTO 角色信息
     */
    public void validateForUpdate(SysRoleDTO roleDTO) {
        // 1. ID校验
        Assert.notNull(roleDTO.getId(), "角色ID不能为空");
        Assert.notNull(roleDTO.getTenantId(), "租户ID不能为空");
        
        // 2. 存在性校验
        if (!roleService.existsById(roleDTO.getId())) {
            throw new BusinessException("角色不存在");
        }
        
        // 3. 角色编码不可修改校验（任务 15）
        String existingRoleCode = roleService.getRoleCodeById(roleDTO.getId());
        if (existingRoleCode != null && !existingRoleCode.equals(roleDTO.getRoleCode())) {
            throw new BusinessException("角色编码不可修改");
        }
        
        // 4. 角色编码唯一性校验（排除自己）（任务 14）
        if (roleService.existsByRoleCodeExcludeId(roleDTO.getRoleCode(), roleDTO.getTenantId(), roleDTO.getId())) {
            throw new BusinessException("角色编码已被其他角色使用");
        }
        
        // 5. 角色名称唯一性校验（排除自己）（任务 14）
        if (roleService.existsByRoleNameExcludeId(roleDTO.getRoleName(), roleDTO.getTenantId(), roleDTO.getId())) {
            throw new BusinessException("角色名称已被其他角色使用");
        }
        
        // 6. 角色键唯一性校验（排除自己）（保留原有逻辑）
        if (roleService.existsByRoleKeyExcludeId(roleDTO.getRoleKey(), roleDTO.getId())) {
            throw new BusinessException("角色键已被其他角色使用");
        }
    }
    
    /**
     * 删除角色校验（任务 16）
     * 
     * @param id 角色ID
     */
    public void validateForDelete(Long id) {
        // 1. ID校验
        validateId(id);
        
        // 2. 存在性校验
        if (!roleService.existsById(id)) {
            throw new BusinessException("角色不存在");
        }
        
        // 3. 关联用户检查（任务 16）
        if (roleService.hasUserAssociation(id)) {
            throw new BusinessException("该角色下还有用户，无法删除");
        }
    }
    
    /**
     * ID校验
     * 
     * @param id 角色ID
     */
    public void validateId(Long id) {
        Assert.notNull(id, "角色ID不能为空");
        if (id <= 0) {
            throw new BusinessException("角色ID格式不正确");
        }
    }
}
