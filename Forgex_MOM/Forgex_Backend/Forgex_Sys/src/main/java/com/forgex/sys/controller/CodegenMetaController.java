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

import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.domain.vo.CodegenMetaOptionVO;
import com.forgex.sys.service.CodegenMetaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代码生成元数据控制器
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@RestController
@RequestMapping("/sys/codegen/meta")
@RequiredArgsConstructor
public class CodegenMetaController {

    private final CodegenMetaService codegenMetaService;

    /**
     * 查询数据库列表
     *
     * @param param 查询参数
     * @return 数据库列表
     */
    @PostMapping("/databases")
    public R<List<CodegenMetaOptionVO>> databases(@RequestBody MetaQueryParam param) {
        return R.ok(codegenMetaService.listSchemas(param.getDatasourceId()));
    }

    /**
     * 查询表列表
     *
     * @param param 查询参数
     * @return 表列表
     */
    @PostMapping("/tables")
    public R<List<CodegenMetaOptionVO>> tables(@RequestBody MetaQueryParam param) {
        return R.ok(codegenMetaService.listTables(param.getDatasourceId(), param.getSchemaName()));
    }

    /**
     * 查询字段列表
     *
     * @param param 查询参数
     * @return 字段列表
     */
    @PostMapping("/columns")
    public R<List<ColumnMetaDTO>> columns(@RequestBody MetaQueryParam param) {
        return R.ok(codegenMetaService.listColumns(param.getDatasourceId(), param.getSchemaName(), param.getTableName()));
    }

    /**
     * 查询表详情
     *
     * @param param 查询参数
     * @return 表详情
     */
    @PostMapping("/tableDetail")
    public R<TableMetaDTO> tableDetail(@RequestBody MetaQueryParam param) {
        return R.ok(codegenMetaService.getTableMeta(param.getDatasourceId(), param.getSchemaName(), param.getTableName()));
    }

    /**
     * 元数据查询参数
     */
    @Data
    public static class MetaQueryParam {

        /**
         * 数据源 ID
         */
        private Long datasourceId;

        /**
         * schema 名称
         */
        private String schemaName;

        /**
         * 表名称
         */
        private String tableName;
    }
}
