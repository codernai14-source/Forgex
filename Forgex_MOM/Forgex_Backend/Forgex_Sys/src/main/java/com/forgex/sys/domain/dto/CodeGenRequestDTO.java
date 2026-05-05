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
package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成请求 DTO
 * <p>
 * 封装代码生成所需的完整配置，用于单表、主子表、单树左树右表、双表左树右表等页面生成。
 * </p>
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Data
public class CodeGenRequestDTO {

    /**
     * 代码生成配置记录 ID
     */
    private Long configId;

    /**
     * 数据源 ID
     */
    private Long datasourceId;

    /**
     * 数据源编码
     */
    private String datasourceCode;

    /**
     * schema/catalog 名称
     */
    private String schemaName;

    /**
     * 页面类型：SINGLE / MASTER_DETAIL / TREE_SINGLE / TREE_DOUBLE
     */
    private String pageType;

    /**
     * 主表名称
     */
    private String mainTableName;

    /**
     * 树表名称
     */
    private String treeTableName;

    /**
     * 子表名称
     */
    private String subTableName;

    /**
     * 主表主键列
     */
    private String mainPkColumn;

    /**
     * 树表主键列
     */
    private String treePkColumn;

    /**
     * 树表父级列
     */
    private String treeParentColumn;

    /**
     * 树表显示列
     */
    private String treeLabelColumn;

    /**
     * 树表排序列
     */
    private String treeSortColumn;

    /**
     * 树过滤外键列
     */
    private String treeFilterColumn;

    /**
     * 子表外键列
     */
    private String subFkColumn;

    /**
     * 子表主键列
     */
    private String subPkColumn;

    /**
     * 模块编码
     */
    private String moduleName;

    /**
     * 业务标识
     */
    private String bizName;

    /**
     * 主实体名称
     */
    private String entityName;

    /**
     * 树实体名称
     */
    private String treeEntityName;

    /**
     * 子实体名称
     */
    private String subEntityName;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 作者
     */
    private String author;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 父级菜单路径
     */
    private String parentMenuPath;

    /**
     * 表格编码前缀
     */
    private String tableCodePrefix;

    /**
     * Android 特性标识
     */
    private String androidFeatureKey;

    /**
     * 生成项：backend / frontend / sql / android
     */
    private List<String> generateItems = new ArrayList<>();

    /**
     * 主表字段配置
     */
    private List<ColumnMetaDTO> mainColumns = new ArrayList<>();

    /**
     * 树表字段配置
     */
    private List<ColumnMetaDTO> treeColumns = new ArrayList<>();

    /**
     * 子表字段配置
     */
    private List<ColumnMetaDTO> subColumns = new ArrayList<>();
}
