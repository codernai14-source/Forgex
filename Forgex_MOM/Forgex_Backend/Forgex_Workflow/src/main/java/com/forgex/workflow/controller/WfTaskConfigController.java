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
package com.forgex.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.workflow.domain.dto.WfTaskConfigDTO;
import com.forgex.workflow.domain.param.WfTaskConfigQueryParam;
import com.forgex.workflow.domain.param.WfTaskConfigSaveParam;
import com.forgex.workflow.service.IWfTaskConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 审批任务配置 Controller。
 * <p>
 * 提供审批任务配置的 HTTP 接口，包含审批任务的增删改查功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see com.forgex.workflow.service.IWfTaskConfigService
 */
@Slf4j
@RestController
@RequestMapping("/wf/task/config")
@RequiredArgsConstructor
public class WfTaskConfigController {
    
    private final IWfTaskConfigService taskConfigService;
    
    /**
     * 分页查询审批任务配置列表
     * 
     * @param param 查询参数（包含分页参数）
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<Page<WfTaskConfigDTO>> page(@RequestBody WfTaskConfigQueryParam param) {
        Page<WfTaskConfigDTO> page = taskConfigService.page(param);
        return R.ok(page);
    }
    
    /**
     * 查询审批任务配置列表
     * 
     * @param param 查询参数
     * @return 配置列表
     */
    @PostMapping("/list")
    public R<List<WfTaskConfigDTO>> list(@RequestBody WfTaskConfigQueryParam param) {
        List<WfTaskConfigDTO> list = taskConfigService.list(param);
        return R.ok(list);
    }
    
    /**
     * 获取审批任务配置详情
     *
     * @param params 参数（id）
     * @return 配置详情
     */
    @PostMapping("/get")
    public R<WfTaskConfigDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        WfTaskConfigDTO config = taskConfigService.getById(id);
        
        if (config == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }
        
        return R.ok(config);
    }
    
    /**
     * 根据任务编码获取审批任务配置
     *
     * @param params 参数（taskCode）
     * @return 配置详情
     */
    @PostMapping("/getByCode")
    public R<WfTaskConfigDTO> getByCode(@RequestBody Map<String, Object> params) {
        String taskCode = params.get("taskCode").toString();
        WfTaskConfigDTO config = taskConfigService.getByCode(taskCode);
        
        if (config == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }
        
        return R.ok(config);
    }
    
    /**
     * 新增审批任务配置
     *
     * @param param 配置参数
     * @return 配置 ID
     */
    @PostMapping("/create")
    public R<Long> create(@Validated @RequestBody WfTaskConfigSaveParam param) {
        Long id = taskConfigService.create(param);
        return R.ok(CommonPrompt.CREATE_SUCCESS, id);
    }
    
    /**
     * 更新审批任务配置
     *
     * @param param 配置参数
     * @return 是否成功
     */
    @PostMapping("/update")
    public R<Boolean> update(@Validated @RequestBody WfTaskConfigSaveParam param) {
        Boolean success = taskConfigService.update(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }
    
    /**
     * 删除审批任务配置
     *
     * @param params 参数（id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Boolean success = taskConfigService.delete(id);
        return R.ok(CommonPrompt.DELETE_SUCCESS, success);
    }
    
    /**
     * 启用/禁用审批任务配置
     *
     * @param params 参数（id, status）
     * @return 是否成功
     */
    @PostMapping("/updateStatus")
    public R<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        Boolean success = taskConfigService.updateStatus(id, status);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }
}