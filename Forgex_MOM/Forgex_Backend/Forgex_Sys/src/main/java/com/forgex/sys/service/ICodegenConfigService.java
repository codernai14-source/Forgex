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
package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.dto.CodegenConfigDTO;
import com.forgex.sys.domain.dto.CodegenConfigQueryDTO;
import com.forgex.sys.domain.entity.SysCodegenConfig;
import com.forgex.sys.domain.param.CodegenConfigSaveParam;

/**
 * 代码生成配置服务接口
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
public interface ICodegenConfigService {

    /**
     * 分页查询配置
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<CodegenConfigDTO> pageConfigs(Page<SysCodegenConfig> page, CodegenConfigQueryDTO query);

    /**
     * 查询配置详情
     *
     * @param id 主键 ID
     * @return 配置详情
     */
    CodegenConfigDTO getConfigDetail(Long id);

    /**
     * 保存配置
     *
     * @param param 保存参数
     * @return 主键 ID
     */
    Long saveConfig(CodegenConfigSaveParam param);

    /**
     * 删除配置
     *
     * @param id 主键 ID
     */
    void deleteConfig(Long id);

    /**
     * 根据配置记录构建生成请求
     *
     * @param id 主键 ID
     * @return 代码生成请求
     */
    CodeGenRequestDTO buildRequest(Long id);

    /**
     * 校验并返回配置实体
     *
     * @param id 主键 ID
     * @return 配置实体
     */
    SysCodegenConfig requireEntity(Long id);

    /**
     * 标记最近生成时间
     *
     * @param id 主键 ID
     */
    void markGenerated(Long id);
}
