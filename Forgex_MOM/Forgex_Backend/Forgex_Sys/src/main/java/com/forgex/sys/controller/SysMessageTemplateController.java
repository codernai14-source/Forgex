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
 * 职责：
 * - 接收 HTTP 请求
 * - 参数校验（调用 MessageTemplateValidator）
 * - 调用 Service 层方法
 * - 返回响应结果
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
     * <p>
     * 接口路径：POST /sys/message-template/page
     * 需要权限：sys:messageTemplate:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含 pageNum、pageSize 和筛选条件）</li>
     *   <li>调用 Service 层分页查询消息模板 VO 列表</li>
     *   <li>返回分页结果</li>
     * </ol>
     *
     * @param param 查询参数
     *              - pageNum: 页码（必填，从 1 开始）
     *              - pageSize: 每页大小（必填）
     *              - templateName: 模板名称（可选，模糊查询）
     *              - templateCode: 模板编码（可选，精确查询）
     *              - templateType: 模板类型（可选）
     * @return {@link R} 包含消息模板分页列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 分页结果（Page&lt;SysMessageTemplateVO&gt;）
     *         - message: 提示信息
     * @see SysMessageTemplateParam
     * @see SysMessageTemplateVO
     */
    @PostMapping("/page")
    public R<Page<SysMessageTemplateVO>> page(@RequestBody SysMessageTemplateParam param) {
        // 委派给 Service 层分页查询消息模板
        return R.ok(messageTemplateService.page(param));
    }
    
    /**
     * 根据 ID 查询消息模板详情
     * <p>
     * 接口路径：POST /sys/message-template/get
     * 需要权限：sys:messageTemplate:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收参数（包含模板 ID）</li>
     *   <li>调用 Validator 校验 ID 合法性</li>
     *   <li>调用 Service 层获取模板详情</li>
     *   <li>返回模板详情</li>
     * </ol>
     *
     * @param param 请求参数
     *              - id: 模板 ID（必填）
     * @return {@link R} 包含消息模板详情的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 模板详情（SysMessageTemplateVO）
     *         - message: 提示信息
     * @throws BusinessException 参数校验失败时抛出
     * @see SysMessageTemplateIdParam
     * @see MessageTemplateValidator#validateId(Long)
     */
    @PostMapping("/get")
    public R<SysMessageTemplateVO> getById(@RequestBody @Validated SysMessageTemplateIdParam param) {
        // 1. 调用 Validator 校验 ID 合法性
        messageTemplateValidator.validateId(param.getId());
        // 2. 委派给 Service 层获取模板详情
        return R.ok(messageTemplateService.getById(param.getId()));
    }
    
    /**
     * 保存消息模板（新增或修改）
     * <p>
     * 接口路径：POST /sys/message-template/save
     * 需要权限：sys:messageTemplate:add（新增）或 sys:messageTemplate:edit（修改）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收模板保存参数</li>
     *   <li>调用 Validator 校验数据（模板编码唯一性、必填字段等）</li>
     *   <li>调用 Service 层保存模板</li>
     *   <li>返回保存成功的模板 ID</li>
     * </ol>
     *
     * @param dto 模板保存参数
     *            - id: 模板 ID（修改时必填，新增时不填）
     *            - templateName: 模板名称（必填）
     *            - templateCode: 模板编码（必填）
     *            - templateType: 模板类型（必填）
     *            - templateContent: 模板内容（必填）
     *            - paramExample: 参数示例（可选）
     *            - remark: 备注（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: 保存成功的模板 ID
     *         - message: 提示信息
     * @throws BusinessException 参数校验失败或模板编码已存在时抛出
     * @see SysMessageTemplateSaveDTO
     * @see MessageTemplateValidator#validateForSave(SysMessageTemplateSaveDTO)
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody @Validated SysMessageTemplateSaveDTO dto) {
        // 1. 调用 Validator 校验数据
        messageTemplateValidator.validateForSave(dto);
        // 2. 委派给 Service 层保存模板
        return R.ok(messageTemplateService.save(dto));
    }
    
    /**
     * 删除消息模板
     * <p>
     * 接口路径：POST /sys/message-template/delete
     * 需要权限：sys:messageTemplate:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收参数（包含模板 ID）</li>
     *   <li>调用 Validator 校验 ID 合法性</li>
     *   <li>调用 Service 层删除模板</li>
     *   <li>返回删除结果</li>
     * </ol>
     *
     * @param param 请求参数
     *              - id: 模板 ID（必填）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: 删除结果（true=成功，false=失败）
     *         - message: 提示信息
     * @throws BusinessException 参数校验失败时抛出
     * @see SysMessageTemplateIdParam
     * @see MessageTemplateValidator#validateId(Long)
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody @Validated SysMessageTemplateIdParam param) {
        // 1. 调用 Validator 校验 ID 合法性
        messageTemplateValidator.validateId(param.getId());
        // 2. 委派给 Service 层删除模板
        return R.ok(messageTemplateService.delete(param.getId()));
    }
    
    /**
     * 批量删除消息模板
     * <p>
     * 接口路径：POST /sys/message-template/delete-batch
     * 需要权限：sys:messageTemplate:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收参数（包含模板 ID 列表）</li>
     *   <li>调用 Validator 校验 ID 列表合法性</li>
     *   <li>调用 Service 层批量删除模板</li>
     *   <li>返回删除结果</li>
     * </ol>
     *
     * @param param 请求参数
     *              - ids: 模板 ID 列表（必填，不能为空）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: 删除结果（true=成功，false=失败）
     *         - message: 提示信息
     * @throws BusinessException 参数校验失败时抛出
     * @see SysMessageTemplateBatchDeleteParam
     * @see MessageTemplateValidator#validateBatchIds(java.util.List)
     */
    @PostMapping("/delete-batch")
    public R<Boolean> deleteBatch(@RequestBody @Validated SysMessageTemplateBatchDeleteParam param) {
        // 1. 调用 Validator 校验 ID 列表合法性
        messageTemplateValidator.validateBatchIds(param.getIds());
        // 2. 委派给 Service 层批量删除模板
        return R.ok(messageTemplateService.deleteBatch(param.getIds()));
    }
}
