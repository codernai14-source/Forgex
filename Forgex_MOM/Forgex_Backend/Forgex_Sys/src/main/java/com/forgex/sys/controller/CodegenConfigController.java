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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.CodegenConfigDTO;
import com.forgex.sys.domain.dto.CodegenConfigQueryDTO;
import com.forgex.sys.domain.entity.SysCodegenConfig;
import com.forgex.sys.domain.param.CodegenConfigSaveParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.service.ICodegenConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码生成配置控制器
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/codegen/config")
@RequiredArgsConstructor
public class CodegenConfigController {

    private final ICodegenConfigService codegenConfigService;

    /**
     * 分页查询代码生成配置
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<IPage<CodegenConfigDTO>> page(@RequestBody(required = false) CodegenConfigQueryDTO query) {
        CodegenConfigQueryDTO condition = query == null ? new CodegenConfigQueryDTO() : query;
        Page<SysCodegenConfig> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(codegenConfigService.pageConfigs(page, condition));
    }

    /**
     * 查询配置详情
     *
     * @param param 主键参数
     * @return 配置详情
     */
    @PostMapping("/detail")
    public R<CodegenConfigDTO> detail(@RequestBody IdParam param) {
        return R.ok(codegenConfigService.getConfigDetail(param.getId()));
    }

    /**
     * 保存数据。
     *
     * @param param 保存参数
     * @return 主键 ID
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody CodegenConfigSaveParam param) {
        return R.ok(codegenConfigService.saveConfig(param));
    }

    /**
     * 删除配置
     *
     * @param param 主键参数
     * @return 是否成功
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        codegenConfigService.deleteConfig(param.getId());
        return R.ok();
    }
}
