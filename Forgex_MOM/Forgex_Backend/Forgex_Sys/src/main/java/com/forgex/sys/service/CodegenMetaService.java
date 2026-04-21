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

import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.domain.vo.CodegenMetaOptionVO;

import java.util.List;

/**
 * 代码生成元数据服务
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
public interface CodegenMetaService {

    /**
     * 查询数据库列表
     *
     * @param datasourceId 数据源 ID
     * @return 数据库列表
     */
    List<CodegenMetaOptionVO> listSchemas(Long datasourceId);

    /**
     * 查询表列表
     *
     * @param datasourceId 数据源 ID
     * @param schemaName schema 名称
     * @return 表列表
     */
    List<CodegenMetaOptionVO> listTables(Long datasourceId, String schemaName);

    /**
     * 查询表字段
     *
     * @param datasourceId 数据源 ID
     * @param schemaName schema 名称
     * @param tableName 表名
     * @return 字段列表
     */
    List<ColumnMetaDTO> listColumns(Long datasourceId, String schemaName, String tableName);

    /**
     * 查询表信息
     *
     * @param datasourceId 数据源 ID
     * @param schemaName schema 名称
     * @param tableName 表名
     * @return 表信息
     */
    TableMetaDTO getTableMeta(Long datasourceId, String schemaName, String tableName);
}
