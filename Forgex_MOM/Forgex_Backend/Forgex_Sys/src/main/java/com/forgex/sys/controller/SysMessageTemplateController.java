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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.domain.param.SysMessageTemplateBatchDeleteParam;
import com.forgex.sys.domain.param.SysMessageTemplateIdParam;
import com.forgex.sys.domain.param.SysMessageTemplateParam;
import com.forgex.sys.domain.vo.SysMessageTemplateVO;
import com.forgex.sys.service.SysMessageTemplateService;
import com.forgex.sys.validator.MessageTemplateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消息模板控制器
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysMessageTemplateService
 * @see MessageTemplateValidator
 */
@RestController
@RequestMapping("/sys/message-template")
@RequiredArgsConstructor
public class SysMessageTemplateController {

    private final SysMessageTemplateService messageTemplateService;
    private final MessageTemplateValidator messageTemplateValidator;

    /**
     * 分页查询消息模板
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<Page<SysMessageTemplateVO>> page(@RequestBody SysMessageTemplateParam param) {
        return R.ok(messageTemplateService.page(param));
    }

    /**
     * 根据ID查询消息模板详情
     *
     * @param param 主键参数
     * @return 模板详情
     */
    @PostMapping("/get")
    public R<SysMessageTemplateVO> getById(@RequestBody @Validated SysMessageTemplateIdParam param) {
        messageTemplateValidator.validateId(param.getId());
        return R.ok(messageTemplateService.getById(param.getId(), param.getPublicConfig()));
    }

    /**
     * 保存消息模板
     *
     * @param dto 保存参数
     * @return 模板ID
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody @Validated SysMessageTemplateSaveDTO dto) {
        messageTemplateValidator.validateForSave(dto);
        Long id = messageTemplateService.save(dto);
        return R.ok(CommonPrompt.SAVE_SUCCESS, id);
    }

    /**
     * 删除消息模板
     *
     * @param param 主键参数
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody @Validated SysMessageTemplateIdParam param) {
        messageTemplateValidator.validateId(param.getId());
        Boolean success = messageTemplateService.delete(param.getId(), param.getPublicConfig());
        return R.ok(CommonPrompt.DELETE_SUCCESS, success);
    }

    /**
     * 批量删除消息模板
     *
     * @param param 批量删除参数
     * @return 是否删除成功
     */
    @PostMapping("/delete-batch")
    public R<Boolean> deleteBatch(@RequestBody @Validated SysMessageTemplateBatchDeleteParam param) {
        messageTemplateValidator.validateBatchIds(param.getIds());
        Boolean success = messageTemplateService.deleteBatch(param.getIds(), param.getPublicConfig());
        return R.ok(CommonPrompt.DELETE_SUCCESS, success);
    }

    /**
     * 从公共配置中拉取模板到当前租户
     *
     * @return 变更数量
     */
    @PostMapping("/pull-public")
    public R<Integer> pullPublicConfig() {
        Integer count = messageTemplateService.pullPublicConfig();
        return R.ok(CommonPrompt.OPERATION_SUCCESS, count);
    }
}
