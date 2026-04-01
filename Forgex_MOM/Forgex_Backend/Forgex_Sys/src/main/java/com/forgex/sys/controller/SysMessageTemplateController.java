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
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.param.SysMessageTemplateBatchDeleteParam;
import com.forgex.sys.domain.param.SysMessageTemplateIdParam;
import com.forgex.sys.domain.param.SysMessageTemplateParam;
import com.forgex.sys.domain.vo.SysMessageTemplateVO;
import com.forgex.sys.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 消息模板控制器
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/message-template")
@RequiredArgsConstructor
public class SysMessageTemplateController {
    
    private final SysMessageTemplateService messageTemplateService;
    
    /**
     * 分页查询消息模板
     */
    @PostMapping("/page")
    public R<Page<SysMessageTemplateVO>> page(@RequestBody SysMessageTemplateParam param) {
        return R.ok(messageTemplateService.page(param));
    }
    
    /**
     * 根据ID查询消息模板详情
     *
     * @param param 请求体，必须包含 {@code id}
     * @return 消息模板详情；参数非法时返回失败
     */
    @PostMapping("/get")
    public R<SysMessageTemplateVO> getById(@RequestBody SysMessageTemplateIdParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "id不能为空");
        }
        return R.ok(messageTemplateService.getById(param.getId()));
    }
    
    /**
     * 保存消息模板(新增或修改)
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody SysMessageTemplateSaveDTO dto) {
        return R.ok(messageTemplateService.save(dto));
    }
    
    /**
     * 删除消息模板
     *
     * @param param 请求体，必须包含 {@code id}
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody SysMessageTemplateIdParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "id不能为空");
        }
        return R.ok(messageTemplateService.delete(param.getId()));
    }
    
    /**
     * 批量删除消息模板
     *
     * @param param 请求体，必须包含非空 {@code ids}
     * @return 是否删除成功
     */
    @PostMapping("/delete-batch")
    public R<Boolean> deleteBatch(@RequestBody SysMessageTemplateBatchDeleteParam param) {
        if (param == null || CollectionUtils.isEmpty(param.getIds())) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ids不能为空");
        }
        return R.ok(messageTemplateService.deleteBatch(param.getIds()));
    }
}



