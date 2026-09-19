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
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 消息模板校验器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MessageTemplateValidator {

    private static final String MSG_TEMPLATE_ID_REQUIRED = "\u6a21\u677f ID \u4e0d\u80fd\u4e3a\u7a7a";
    private static final String MSG_TEMPLATE_IDS_REQUIRED = "\u6a21\u677f ID \u5217\u8868\u4e0d\u80fd\u4e3a\u7a7a";
    private static final String MSG_TEMPLATE_NAME_REQUIRED = "\u6a21\u677f\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a";
    private static final String MSG_TEMPLATE_CODE_REQUIRED = "\u6a21\u677f\u7f16\u7801\u4e0d\u80fd\u4e3a\u7a7a";

    private final SysMessageTemplateService messageTemplateService;

    /**
     * 校验ID。
     *
     * @param id 主键 ID
     */
    public void validateId(Long id) {
        Assert.notNull(id, MSG_TEMPLATE_ID_REQUIRED);
    }

    /**
     * 校验batchids。
     *
     * @param ids 主键 ID 集合
     */
    public void validateBatchIds(List<Long> ids) {
        Assert.isTrue(!CollectionUtils.isEmpty(ids), MSG_TEMPLATE_IDS_REQUIRED);
    }

    /**
     * 校验forsave。
     *
     * @param dto 数据传输对象
     */
    public void validateForSave(SysMessageTemplateSaveDTO dto) {
        Assert.isTrue(
                StringUtils.hasText(dto.getTemplateName()) || StringUtils.hasText(dto.getTemplateNameI18nJson()),
                MSG_TEMPLATE_NAME_REQUIRED
        );
        Assert.hasText(dto.getTemplateCode(), MSG_TEMPLATE_CODE_REQUIRED);

        if (dto.getId() == null) {
            if (messageTemplateService.existsByCode(dto.getTemplateCode(), dto.getPublicConfig())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_TEMPLATE_CODE_EXISTS);
            }
        } else {
            if (messageTemplateService.existsByCodeExcludeId(dto.getTemplateCode(), dto.getId(), dto.getPublicConfig())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_TEMPLATE_CODE_EXISTS_OTHER);
            }
        }
    }
}
