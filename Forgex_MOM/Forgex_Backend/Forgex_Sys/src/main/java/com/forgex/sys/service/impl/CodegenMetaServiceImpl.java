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
package com.forgex.sys.service.impl;

import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.vo.CodegenMetaOptionVO;
import com.forgex.sys.service.CodegenMetaService;
import com.forgex.sys.service.DbMetaService;
import com.forgex.sys.service.ICodegenDatasourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码生成元数据服务实现
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CodegenMetaServiceImpl implements CodegenMetaService {

    private final DbMetaService dbMetaService;
    private final ICodegenDatasourceService datasourceService;

    /**
     * 查询数据库 Schema 列表。
     *
     * @param datasourceId 数据源 ID
     * @return 列表数据
     */
    @Override
    public List<CodegenMetaOptionVO> listSchemas(Long datasourceId) {
        SysCodegenDatasource datasource = datasourceService.requireEntity(datasourceId);
        return dbMetaService.listSchemas(datasource).stream()
            .map(item -> new CodegenMetaOptionVO(item, item))
            .collect(Collectors.toList());
    }

    /**
     * 查询数据库表列表。
     *
     * @param datasourceId 数据源 ID
     * @param schemaName 数据库模式名称
     * @return 列表数据
     */
    @Override
    public List<CodegenMetaOptionVO> listTables(Long datasourceId, String schemaName) {
        SysCodegenDatasource datasource = datasourceService.requireEntity(datasourceId);
        return dbMetaService.listTables(datasource, schemaName).stream()
            .map(item -> new CodegenMetaOptionVO(item, item))
            .collect(Collectors.toList());
    }

    /**
     * 查询数据库列列表。
     *
     * @param datasourceId 数据源 ID
     * @param schemaName 数据库模式名称
     * @param tableName 表格名称
     * @return 列表数据
     */
    @Override
    public List<ColumnMetaDTO> listColumns(Long datasourceId, String schemaName, String tableName) {
        SysCodegenDatasource datasource = datasourceService.requireEntity(datasourceId);
        return dbMetaService.listColumns(datasource, schemaName, tableName);
    }

    /**
     * 获取表格meta。
     *
     * @param datasourceId 数据源 ID
     * @param schemaName 数据库模式名称
     * @param tableName 表格名称
     * @return 处理结果
     */
    @Override
    public TableMetaDTO getTableMeta(Long datasourceId, String schemaName, String tableName) {
        SysCodegenDatasource datasource = datasourceService.requireEntity(datasourceId);
        return dbMetaService.getTableMeta(datasource, schemaName, tableName);
    }
}
