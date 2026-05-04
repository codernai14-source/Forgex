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
import com.forgex.sys.domain.dto.CodegenDatasourceDTO;
import com.forgex.sys.domain.dto.CodegenDatasourceQueryDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.param.CodegenDatasourceSaveParam;
import com.forgex.sys.domain.param.CodegenDatasourceTestParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.service.ICodegenDatasourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代码生成数据源管理控制器
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/codegen/datasource")
@RequiredArgsConstructor
public class CodegenDatasourceController {

    private final ICodegenDatasourceService datasourceService;

    /**
     * 分页查询数据源
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<IPage<CodegenDatasourceDTO>> page(@RequestBody(required = false) CodegenDatasourceQueryDTO query) {
        CodegenDatasourceQueryDTO condition = query == null ? new CodegenDatasourceQueryDTO() : query;
        Page<SysCodegenDatasource> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(datasourceService.pageDatasources(page, condition));
    }

    /**
     * 查询启用数据源列表
     *
     * @return 已启用数据源
     */
    @PostMapping("/list")
    public R<List<CodegenDatasourceDTO>> list() {
        return R.ok(datasourceService.listEnabledDatasources());
    }

    /**
     * 查询数据源详情
     *
     * @param param 主键参数
     * @return 数据源详情
     */
    @PostMapping("/detail")
    public R<CodegenDatasourceDTO> detail(@RequestBody IdParam param) {
        return R.ok(datasourceService.getDatasourceById(param.getId()));
    }

    /**
     * 保存数据。
     *
     * @param param 保存参数
     * @return 数据源主键
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody CodegenDatasourceSaveParam param) {
        return R.ok(datasourceService.saveDatasource(param));
    }

    /**
     * 删除数据源
     *
     * @param param 主键参数
     * @return 是否成功
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        datasourceService.deleteDatasource(param.getId());
        return R.ok();
    }

    /**
     * 测试连接
     *
     * @param param 测试参数
     * @return 是否成功
     */
    @PostMapping("/test")
    public R<Boolean> test(@RequestBody CodegenDatasourceTestParam param) {
        return R.ok(datasourceService.testConnection(param));
    }
}
