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
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.ISysModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 模块数据校验器
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Component
@RequiredArgsConstructor
public class ModuleValidator {
    
    private final ISysModuleService moduleService;
    
    /**
     * 新增模块校验（任务 12）
     * 
     * @param moduleDTO 模块信息
     */
    public void validateForAdd(SysModuleDTO moduleDTO) {
        // 1. 必填项校验（@Validated 注解已处理，这里做额外校验）
        Assert.hasText(moduleDTO.getCode(), "模块编码不能为空");
        Assert.hasText(moduleDTO.getName(), "模块名称不能为空");
        Assert.notNull(moduleDTO.getTenantId(), "租户 ID 不能为空");
        
        // 2. 业务规则校验：模块编码唯一性
        if (moduleService.existsByCode(moduleDTO.getCode())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_CODE_EXISTS_VALIDATOR);
        }
        
        // 3. 业务规则校验：模块名称唯一性（任务 12）
        if (moduleService.existsByName(moduleDTO.getName(), moduleDTO.getTenantId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_NAME_EXISTS);
        }
        
        // 4. 数据格式校验
        validateModuleCode(moduleDTO.getCode());
    }
    
    /**
     * 更新模块校验（任务 12）
     * 
     * @param moduleDTO 模块信息
     */
    public void validateForUpdate(SysModuleDTO moduleDTO) {
        // 1. ID 校验
        Assert.notNull(moduleDTO.getId(), "模块 ID 不能为空");
        Assert.notNull(moduleDTO.getTenantId(), "租户 ID 不能为空");
        
        // 2. 存在性校验
        if (!moduleService.existsById(moduleDTO.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_NOT_FOUND);
        }
        
        // 3. 唯一性校验（排除自己）
        if (moduleService.existsByCodeExcludeId(moduleDTO.getCode(), moduleDTO.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_CODE_EXISTS_OTHER);
        }
        
        // 4. 模块名称唯一性校验（排除自己）（任务 12）
        if (moduleService.existsByNameExcludeId(moduleDTO.getName(), moduleDTO.getTenantId(), moduleDTO.getId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_NAME_EXISTS_OTHER);
        }
        
        // 5. 数据格式校验
        validateModuleCode(moduleDTO.getCode());
    }
    
    /**
     * 删除模块校验（任务 13）
     * 
     * @param id 模块 ID
     */
    public void validateForDelete(Long id) {
        // 1. ID 校验
        validateId(id);
        
        // 2. 存在性校验
        if (!moduleService.existsById(id)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_NOT_FOUND);
        }
        
        // 3. 关联数据校验：检查是否有关联菜单
        if (moduleService.hasMenus(id)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_HAS_MENUS_DELETE);
        }
        
        // 4. 角色关联检查：通过菜单查询是否有角色关联（任务 13）
        if (moduleService.hasRoleAssociationThroughMenus(id)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_HAS_ROLE_ASSOCIATION_DELETE);
        }
    }
    
    /**
     * ID 校验
     * 
     * @param id 模块 ID
     */
    public void validateId(Long id) {
        Assert.notNull(id, "模块 ID 不能为空");
        if (id <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_ID_INVALID);
        }
    }
    
    /**
     * 模块编码格式校验
     * 
     * @param code 模块编码
     */
    private void validateModuleCode(String code) {
        // 模块编码规则：只能包含字母、数字、下划线，长度 2-50
        if (!code.matches("^[a-zA-Z0-9_]{2,50}$")) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MODULE_CODE_INVALID);
        }
    }
}
