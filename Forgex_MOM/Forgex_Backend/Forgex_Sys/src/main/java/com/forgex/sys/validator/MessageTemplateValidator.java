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
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 消息模板数据校验器
 * 
 * 职责：
 * - 校验消息模板参数的合法性
 * - 校验模板编码的唯一性
 * - 校验必填字段
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @see SysMessageTemplateService
 */
@Component
@RequiredArgsConstructor
public class MessageTemplateValidator {
    
    private final SysMessageTemplateService messageTemplateService;
    
    /**
     * 校验模板 ID 合法性
     * 
     * @param id 模板 ID
     * @throws BusinessException ID 为空时抛出
     */
    public void validateId(Long id) {
        Assert.notNull(id, "模板 ID 不能为空");
    }
    
    /**
     * 校验批量删除的 ID 列表
     * 
     * @param ids 模板 ID 列表
     * @throws BusinessException ID 列表为空时抛出
     */
    public void validateBatchIds(List<Long> ids) {
        Assert.isTrue(!CollectionUtils.isEmpty(ids), "模板 ID 列表不能为空");
    }
    
    /**
     * 保存模板校验（新增或修改）
     * 
     * @param dto 模板保存参数
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateForSave(SysMessageTemplateSaveDTO dto) {
        // 1. 必填项校验
        Assert.hasText(dto.getTemplateName(), "模板名称不能为空");
        Assert.hasText(dto.getTemplateCode(), "模板编码不能为空");

        
        // 2. 模板编码唯一性校验
        if (dto.getId() == null) {
            // 新增时检查编码是否存在
            if (messageTemplateService.existsByCode(dto.getTemplateCode())) {
                throw new BusinessException("模板编码已存在");
            }
        } else {
            // 修改时检查编码是否被其他模板使用
            if (messageTemplateService.existsByCodeExcludeId(dto.getTemplateCode(), dto.getId())) {
                throw new BusinessException("模板编码已被其他模板使用");
            }
        }
    }
}
