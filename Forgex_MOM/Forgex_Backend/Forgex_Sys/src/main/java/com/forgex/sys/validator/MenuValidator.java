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
        
        // 5. 按钮类型必须有权限标识
        if ("button".equalsIgnoreCase(menuDTO.getType())) {
            Assert.hasText(menuDTO.getPermKey(), "按钮权限标识不能为空");
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
        
        // 2. 存在性校验
        if (!menuService.existsById(menuDTO.getId())) {
            throw new BusinessException("菜单不存在");
        }
        
        // 3. 其他校验
        validateForAdd(menuDTO);
    }
    
    /**
     * 删除菜单校验
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
        
        // 3. 检查是否有子菜单
        if (menuService.hasChildren(id)) {
            throw new BusinessException("该菜单下有子菜单，不能删除");
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
}
