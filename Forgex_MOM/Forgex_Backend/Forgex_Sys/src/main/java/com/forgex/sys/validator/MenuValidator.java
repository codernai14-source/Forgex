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

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.enums.SysPromptEnum;
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
        Assert.notNull(menuDTO.getModuleId(), "模块 ID 不能为空");
        Assert.hasText(menuDTO.getName(), "菜单名称不能为空");
        Assert.hasText(menuDTO.getType(), "菜单类型不能为空");
        Assert.notNull(menuDTO.getTenantId(), "租户 ID 不能为空");
        
        // 2. 菜单类型校验
        if (!isValidMenuType(menuDTO.getType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_TYPE_INVALID);
        }
        
        // 3. 非按钮类型必须有路径
        if (!"button".equalsIgnoreCase(menuDTO.getType())) {
            Assert.hasText(menuDTO.getPath(), "菜单路径不能为空");
        }
        
        // 4. 外联模式必须有 URL
        if ("external".equals(menuDTO.getMenuMode())) {
            Assert.hasText(menuDTO.getExternalUrl(), "外联 URL 不能为空");
            if (!isValidUrl(menuDTO.getExternalUrl())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_URL_INVALID);
            }
        }
        
        // 5. 按钮类型必须有权限标识（任务 11）
        if ("button".equalsIgnoreCase(menuDTO.getType())) {
            Assert.hasText(menuDTO.getPermKey(), "按钮权限标识不能为空");
            
            // 5.1 验证权限标识格式：必须符合 {module}:{entity}:{action} 格式
            if (!isValidPermKeyFormat(menuDTO.getPermKey())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_PERM_KEY_INVALID);
            }
            
            // 5.2 验证权限标识唯一性
            if (menuService.existsByPermKey(menuDTO.getPermKey(), menuDTO.getTenantId())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_PERM_KEY_EXISTS_VALIDATOR);
            }
        }
    }
    
    /**
     * 更新菜单校验
     * 
     * @param menuDTO 菜单信息
     */
    public void validateForUpdate(SysMenuDTO menuDTO) {
        // 1. ID 校验
        Assert.notNull(menuDTO.getId(), "菜单 ID 不能为空");
        Assert.notNull(menuDTO.getTenantId(), "租户 ID 不能为空");
        
        // 2. 存在性校验
        if (!menuService.existsById(menuDTO.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_NOT_FOUND);
        }
        
        // 3. 按钮类型权限标识唯一性校验（排除自己）
        if ("button".equalsIgnoreCase(menuDTO.getType()) && StringUtils.hasText(menuDTO.getPermKey())) {
            // 验证格式
            if (!isValidPermKeyFormat(menuDTO.getPermKey())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_PERM_KEY_INVALID);
            }
            
            // 验证唯一性（排除自己）
            if (menuService.existsByPermKeyExcludeId(menuDTO.getPermKey(), menuDTO.getTenantId(), menuDTO.getId())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_PERM_KEY_EXISTS_OTHER);
            }
        }
        
        // 4. 其他校验
        validateForAdd(menuDTO);
    }
    
    /**
     * 删除菜单校验（任务 10）
     * 
     * @param id 菜单 ID
     */
    public void validateForDelete(Long id) {
        // 1. ID 校验
        validateId(id);
        
        // 2. 存在性校验
        if (!menuService.existsById(id)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_NOT_FOUND);
        }
        
        // 3. 检查是否有子菜单或按钮
        if (menuService.hasChildren(id)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_HAS_CHILDREN_DELETE);
        }
        
        // 4. 检查是否已被角色授权
        if (menuService.hasRoleAssociation(id)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_HAS_ROLE_ASSOCIATION_DELETE);
        }
    }
    
    /**
     * ID 校验
     * 
     * @param id 菜单 ID
     */
    public void validateId(Long id) {
        Assert.notNull(id, "菜单 ID 不能为空");
        if (id <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_ID_INVALID);
        }
    }
    
    /**
     * 用户路由参数校验
     * 
     * @param account 用户账号
     * @param tenantId 租户 ID
     */
    public void validateRoutesParams(String account, Long tenantId) {
        if (!StringUtils.hasText(account)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_ACCOUNT_EMPTY);
        }
        if (tenantId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MENU_TENANT_ID_EMPTY);
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
     * 校验 URL 格式
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
        // 每部分只能包含字母、数字、下划线，长度 2-50
        String regex = "^[a-zA-Z0-9_]{2,50}:[a-zA-Z0-9_]{2,50}:[a-zA-Z0-9_]{2,50}$";
        return permKey.matches(regex);
    }
}
