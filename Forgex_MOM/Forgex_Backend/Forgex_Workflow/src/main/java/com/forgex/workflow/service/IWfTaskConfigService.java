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
package com.forgex.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.workflow.domain.dto.WfTaskConfigDTO;
import com.forgex.workflow.domain.param.WfTaskConfigQueryParam;
import com.forgex.workflow.domain.param.WfTaskConfigSaveParam;

import java.util.List;

/**
 * 审批任务配置Service接口。
 * <p>
 * 提供审批任务配置的业务逻辑接口，包含审批任务的增删改查功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see com.forgex.workflow.domain.entity.WfTaskConfig
 */
public interface IWfTaskConfigService {
    
    /**
     * 分页查询审批任务配置列表
     * 
     * @param param 查询参数（包含分页参数）
     * @return 分页结果
     */
    Page<WfTaskConfigDTO> page(WfTaskConfigQueryParam param);
    
    /**
     * 查询审批任务配置列表
     * 
     * @param param 查询参数
     * @return 配置列表
     */
    List<WfTaskConfigDTO> list(WfTaskConfigQueryParam param);
    
    /**
     * 根据ID获取审批任务配置详情
     * 
     * @param id 配置ID
     * @return 配置详情
     */
    WfTaskConfigDTO getById(Long id);
    
    /**
     * 根据任务编码获取审批任务配置
     * 
     * @param taskCode 任务编码
     * @return 配置详情
     */
    WfTaskConfigDTO getByCode(String taskCode);
    
    /**
     * 新增审批任务配置
     * <p>
     * 新增时会进行以下校验：
     * <ul>
     *   <li>任务编码唯一性校验</li>
     * </ul>
     * </p>
     * 
     * @param param 配置参数
     * @return 配置ID
     * @throws RuntimeException 当任务编码已存在时抛出异常
     */
    Long create(WfTaskConfigSaveParam param);
    
    /**
     * 更新审批任务配置
     * <p>
     * 更新时会进行以下校验：
     * <ul>
     *   <li>任务编码唯一性校验（排除自己）</li>
     * </ul>
     * </p>
     * 
     * @param param 配置参数
     * @return 是否成功
     * @throws RuntimeException 当任务编码已存在时抛出异常
     */
    Boolean update(WfTaskConfigSaveParam param);
    
    /**
     * 删除审批任务配置
     * 
     * @param id 配置ID
     * @return 是否成功
     */
    Boolean delete(Long id);
    
    /**
     * 启用/禁用审批任务配置
     * 
     * @param id 配置ID
     * @param status 状态：0=禁用，1=启用
     * @return 是否成功
     */
    Boolean updateStatus(Long id, Integer status);
}