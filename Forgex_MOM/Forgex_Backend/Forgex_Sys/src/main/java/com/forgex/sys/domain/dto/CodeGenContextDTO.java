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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 代码生成上下文 DTO
 * <p>
 * 封装 Beetl 模板渲染所需的全部运行时信息。
 * </p>
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Data
public class CodeGenContextDTO {

    /**
     * 数据源编码。
     */
    private String datasourceCode;

    /**
     * 数据库模式名称。
     */
    private String schemaName;

    /**
     * 分页类型。
     */
    private String pageType;

    /**
     * 主表名称。
     */
    private String mainTableName;

    /**
     * 主表注释。
     */
    private String mainTableComment;

    /**
     * 树表名称。
     */
    private String treeTableName;

    /**
     * 树表注释。
     */
    private String treeTableComment;

    /**
     * 子表名称。
     */
    private String subTableName;

    /**
     * 子表注释。
     */
    private String subTableComment;

    /**
     * 实体名称。
     */
    private String entityName;

    /**
     * 实体名称小写形式。
     */
    private String entityNameLower;

    /**
     * 树实体名称。
     */
    private String treeEntityName;

    /**
     * 树实体名称小写形式。
     */
    private String treeEntityNameLower;

    /**
     * 子表实体名称。
     */
    private String subEntityName;

    /**
     * 子表实体名称小写形式。
     */
    private String subEntityNameLower;

    /**
     * 模块名称。
     */
    private String moduleName;

    /**
     * 业务名称。
     */
    private String bizName;

    /**
     * 包名称。
     */
    private String packageName;

    /**
     * 作者。
     */
    private String author;

    /**
     * 日期。
     */
    private String date;

    /**
     * 主表主键列。
     */
    private String mainPkColumn;

    /**
     * 主表主键 Java 字段名。
     */
    private String mainPkJavaField;

    /**
     * 主表主键 Java 字段首字母大写形式。
     */
    private String mainPkJavaFieldCapital;

    /**
     * 主表主键 Java 类型。
     */
    private String mainPkJavaType;

    /**
     * 树主键列。
     */
    private String treePkColumn;

    /**
     * 树表主键 Java 字段名。
     */
    private String treePkJavaField;

    /**
     * 树表主键 Java 字段首字母大写形式。
     */
    private String treePkJavaFieldCapital;

    /**
     * 树表主键 Java 类型。
     */
    private String treePkJavaType;

    /**
     * 树父级列。
     */
    private String treeParentColumn;

    /**
     * 树表父级 Java 字段名。
     */
    private String treeParentJavaField;

    /**
     * 树表父级 Java 字段首字母大写形式。
     */
    private String treeParentJavaFieldCapital;

    /**
     * 树表父级 Java 类型。
     */
    private String treeParentJavaType;

    /**
     * 树标签列。
     */
    private String treeLabelColumn;

    /**
     * 树表标签 Java 字段名。
     */
    private String treeLabelJavaField;

    /**
     * 树表标签 Java 字段首字母大写形式。
     */
    private String treeLabelJavaFieldCapital;

    /**
     * 树排序列。
     */
    private String treeSortColumn;

    /**
     * 树表排序 Java 字段名。
     */
    private String treeSortJavaField;

    /**
     * 树表排序 Java 字段首字母大写形式。
     */
    private String treeSortJavaFieldCapital;

    /**
     * 树筛选列。
     */
    private String treeFilterColumn;

    /**
     * 树表筛选 Java 字段名。
     */
    private String treeFilterJavaField;

    /**
     * 树表筛选 Java 字段首字母大写形式。
     */
    private String treeFilterJavaFieldCapital;

    /**
     * 树表筛选 Java 类型。
     */
    private String treeFilterJavaType;

    /**
     * 子表主键列。
     */
    private String subPkColumn;

    /**
     * 子表主键 Java 字段名。
     */
    private String subPkJavaField;

    /**
     * 子表主键 Java 字段首字母大写形式。
     */
    private String subPkJavaFieldCapital;

    /**
     * 子表主键 Java 类型。
     */
    private String subPkJavaType;

    /**
     * 子表外键列。
     */
    private String subFkColumn;

    /**
     * 子表外键 Java 字段名。
     */
    private String subFkJavaField;

    /**
     * 子表外键 Java 字段首字母大写形式。
     */
    private String subFkJavaFieldCapital;

    /**
     * 子表外键 Java 类型。
     */
    private String subFkJavaType;

    /**
     * 子表默认排序列。
     */
    private String subDefaultSortColumn;

    /**
     * 子表默认排序 Java 字段名。
     */
    private String subDefaultSortJavaField;

    /**
     * 子表默认排序 Java 字段首字母大写形式。
     */
    private String subDefaultSortJavaFieldCapital;

    /**
     * 菜单名称。
     */
    private String menuName;

    /**
     * 菜单图标。
     */
    private String menuIcon;

    /**
     * 菜单路径。
     */
    private String menuPath;

    /**
     * 父级菜单路径。
     */
    private String parentMenuPath;

    /**
     * 权限 Key 前缀。
     */
    private String permKeyPrefix;

    /**
     * 主表编码。
     */
    private String mainTableCode;

    /**
     * 树表编码。
     */
    private String treeTableCode;

    /**
     * 子表编码。
     */
    private String subTableCode;

    /**
     * 国际化前缀。
     */
    private String i18nPrefix;

    /**
     * 安卓功能 Key。
     */
    private String androidFeatureKey;

    /**
     * 安卓路由常量。
     */
    private String androidRouteConst;

    /**
     * 树根节点值字面量。
     */
    private String treeRootValueLiteral;

    /**
     * 是否为单树结构。
     */
    private boolean treeSingle;

    /**
     * 是否为双树结构。
     */
    private boolean treeDouble;

    /**
     * 安卓启用。
     */
    private boolean androidEnabled;

    /**
     * 前端启用。
     */
    private boolean frontendEnabled;

    /**
     * 后端启用。
     */
    private boolean backendEnabled;

    /**
     * SQL 启用。
     */
    private boolean sqlEnabled;

    /**
     * 生成项列表。
     */
    private List<String> generateItems = new ArrayList<>();

    /**
     * 主表列集合。
     */
    private List<ColumnMetaDTO> mainColumns = new ArrayList<>();

    /**
     * 树表列集合。
     */
    private List<ColumnMetaDTO> treeColumns = new ArrayList<>();

    /**
     * 子表列集合。
     */
    private List<ColumnMetaDTO> subColumns = new ArrayList<>();

    /**
     * 主表查询列集合。
     */
    private List<ColumnMetaDTO> mainQueryColumns = new ArrayList<>();

    /**
     * 主表表单列集合。
     */
    private List<ColumnMetaDTO> mainFormColumns = new ArrayList<>();

    /**
     * 主表列表列集合。
     */
    private List<ColumnMetaDTO> mainTableColumns = new ArrayList<>();

    /**
     * 树表查询列集合。
     */
    private List<ColumnMetaDTO> treeQueryColumns = new ArrayList<>();

    /**
     * 树表表单列集合。
     */
    private List<ColumnMetaDTO> treeFormColumns = new ArrayList<>();

    /**
     * 树表列表列集合。
     */
    private List<ColumnMetaDTO> treeTableColumns = new ArrayList<>();

    /**
     * 子表查询列集合。
     */
    private List<ColumnMetaDTO> subQueryColumns = new ArrayList<>();

    /**
     * 子表表单列集合。
     */
    private List<ColumnMetaDTO> subFormColumns = new ArrayList<>();

    /**
     * 子表列表列集合。
     */
    private List<ColumnMetaDTO> subTableColumns = new ArrayList<>();

    /**
     * 模板渲染需要引入的 Java 类名集合。
     */
    private Set<String> imports = new LinkedHashSet<>();

    /**
     * 额外配置项。
     */
    private Map<String, Object> extraConfig;
}
