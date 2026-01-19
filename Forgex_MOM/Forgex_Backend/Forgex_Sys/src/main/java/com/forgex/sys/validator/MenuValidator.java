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
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 菜单数据校验器
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Component
@RequiredArgsConstructor
public class MenuValidator {
    
    private final ISysMenuService menuService;
    
    /**
     * 新增菜单校验
     * 
     * @param menuDTO 菜单信息
     */
    public void validateForAdd(SysMenuDTO menuDTO) {
        // 1. 必填项校验
        Assert.notNull(menuDTO.getModuleId(), "模块ID不能为空");
        Assert.hasText(menuDTO.getName(), "菜单名称不能为空");
        Assert.hasText(menuDTO.getType(), "菜单类型不能为空");
        Assert.notNull(menuDTO.getTenantId(), "租户ID不能为空");
        
        // 2. 菜单类型校验
        if (!isValidMenuType(menuDTO.getType())) {
            throw new BusinessException("菜单类型不正确，只能是：catalog、menu、button");
        }
        
        // 3. 非按钮类型必须有路径
        if (!"button".equalsIgnoreCase(menuDTO.getType())) {
            Assert.hasText(menuDTO.getPath(), "菜单路径不能为空");
        }
        
        // 4. 外联模式必须有URL
        if ("external".equals(menuDTO.getMenuMode())) {
            Assert.hasText(menuDTO.getExternalUrl(), "外联URL不能为空");
            if (!isValidUrl(menuDTO.getExternalUrl())) {
                throw new BusinessException("外联URL格式不正确");
            }
        }
        
        // 5. 按钮类型必须有权限标识（任务 11）
        if ("button".equalsIgnoreCase(menuDTO.getType())) {
            Assert.hasText(menuDTO.getPermKey(), "按钮权限标识不能为空");
            
            // 5.1 验证权限标识格式：必须符合 {module}:{entity}:{action} 格式
            if (!isValidPermKeyFormat(menuDTO.getPermKey())) {
                throw new BusinessException("权限标识格式不正确，必须符合 {module}:{entity}:{action} 格式，例如：sys:user:create");
            }
            
            // 5.2 验证权限标识唯一性
            if (menuService.existsByPermKey(menuDTO.getPermKey(), menuDTO.getTenantId())) {
                throw new BusinessException("权限标识已存在");
            }
        }
    }
    
    /**
     * 更新菜单校验
     * 
     * @param menuDTO 菜单信息
     */
    public void validateForUpdate(SysMenuDTO menuDTO) {
        // 1. ID校验
        Assert.notNull(menuDTO.getId(), "菜单ID不能为空");
        Assert.notNull(menuDTO.getTenantId(), "租户ID不能为空");
        
        // 2. 存在性校验
        if (!menuService.existsById(menuDTO.getId())) {
            throw new BusinessException("菜单不存在");
        }
        
        // 3. 按钮类型权限标识唯一性校验（排除自己）
        if ("button".equalsIgnoreCase(menuDTO.getType()) && StringUtils.hasText(menuDTO.getPermKey())) {
            // 验证格式
            if (!isValidPermKeyFormat(menuDTO.getPermKey())) {
                throw new BusinessException("权限标识格式不正确，必须符合 {module}:{entity}:{action} 格式，例如：sys:user:create");
            }
            
            // 验证唯一性（排除自己）
            if (menuService.existsByPermKeyExcludeId(menuDTO.getPermKey(), menuDTO.getTenantId(), menuDTO.getId())) {
                throw new BusinessException("权限标识已被其他菜单使用");
            }
        }
        
        // 4. 其他校验
        validateForAdd(menuDTO);
    }
    
    /**
     * 删除菜单校验（任务 10）
     * 
     * @param id 菜单ID
     */
    public void validateForDelete(Long id) {
        // 1. ID校验
        validateId(id);
        
        // 2. 存在性校验
        if (!menuService.existsById(id)) {
            throw new BusinessException("菜单不存在");
        }
        
        // 3. 检查是否有子菜单或按钮
        if (menuService.hasChildren(id)) {
            throw new BusinessException("该菜单下还有子菜单或按钮，无法删除");
        }
        
        // 4. 检查是否已被角色授权
        if (menuService.hasRoleAssociation(id)) {
            throw new BusinessException("该菜单已被角色授权，无法删除");
        }
    }
    
    /**
     * ID校验
     * 
     * @param id 菜单ID
     */
    public void validateId(Long id) {
        Assert.notNull(id, "菜单ID不能为空");
        if (id <= 0) {
            throw new BusinessException("菜单ID格式不正确");
        }
    }
    
    /**
     * 用户路由参数校验
     * 
     * @param account 用户账号
     * @param tenantId 租户ID
     */
    public void validateRoutesParams(String account, Long tenantId) {
        if (!StringUtils.hasText(account)) {
            throw new BusinessException("用户账号不能为空");
        }
        if (tenantId == null) {
            throw new BusinessException("租户ID不能为空");
        }
    }
    
    /**
     * 校验菜单类型
     */
    private boolean isValidMenuType(String type) {
        return "catalog".equalsIgnoreCase(type) 
            || "menu".equalsIgnoreCase(type) 
            || "button".equalsIgnoreCase(type);
    }
    
    /**
     * 校验URL格式
     */
    private boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    /**
     * 校验权限标识格式：必须符合 {module}:{entity}:{action} 格式
     * 例如：sys:user:create
     */
    private boolean isValidPermKeyFormat(String permKey) {
        if (!StringUtils.hasText(permKey)) {
            return false;
        }
        // 权限标识格式：{module}:{entity}:{action}
        // 每部分只能包含字母、数字、下划线，长度2-50
        String regex = "^[a-zA-Z0-9_]{2,50}:[a-zA-Z0-9_]{2,50}:[a-zA-Z0-9_]{2,50}$";
        return permKey.matches(regex);
    }
}
