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
 */
@Data
public class CodeGenContextDTO {

    private String datasourceCode;

    private String schemaName;

    private String pageType;

    private String mainTableName;

    private String mainTableComment;

    private String treeTableName;

    private String treeTableComment;

    private String subTableName;

    private String subTableComment;

    private String entityName;

    private String entityNameLower;

    private String treeEntityName;

    private String treeEntityNameLower;

    private String subEntityName;

    private String subEntityNameLower;

    private String moduleName;

    private String bizName;

    private String packageName;

    private String author;

    private String date;

    private String mainPkColumn;

    private String mainPkJavaField;

    private String mainPkJavaFieldCapital;

    private String mainPkJavaType;

    private String treePkColumn;

    private String treePkJavaField;

    private String treePkJavaFieldCapital;

    private String treePkJavaType;

    private String treeParentColumn;

    private String treeParentJavaField;

    private String treeParentJavaFieldCapital;

    private String treeParentJavaType;

    private String treeLabelColumn;

    private String treeLabelJavaField;

    private String treeLabelJavaFieldCapital;

    private String treeSortColumn;

    private String treeSortJavaField;

    private String treeSortJavaFieldCapital;

    private String treeFilterColumn;

    private String treeFilterJavaField;

    private String treeFilterJavaFieldCapital;

    private String treeFilterJavaType;

    private String subPkColumn;

    private String subPkJavaField;

    private String subPkJavaFieldCapital;

    private String subPkJavaType;

    private String subFkColumn;

    private String subFkJavaField;

    private String subFkJavaFieldCapital;

    private String subFkJavaType;

    private String subDefaultSortColumn;

    private String subDefaultSortJavaField;

    private String subDefaultSortJavaFieldCapital;

    private String menuName;

    private String menuIcon;

    private String menuPath;

    private String parentMenuPath;

    private String permKeyPrefix;

    private String mainTableCode;

    private String treeTableCode;

    private String subTableCode;

    private String i18nPrefix;

    private String androidFeatureKey;

    private String androidRouteConst;

    private String treeRootValueLiteral;

    private boolean treeSingle;

    private boolean treeDouble;

    private boolean androidEnabled;

    private boolean frontendEnabled;

    private boolean backendEnabled;

    private boolean sqlEnabled;

    private List<String> generateItems = new ArrayList<>();

    private List<ColumnMetaDTO> mainColumns = new ArrayList<>();

    private List<ColumnMetaDTO> treeColumns = new ArrayList<>();

    private List<ColumnMetaDTO> subColumns = new ArrayList<>();

    private List<ColumnMetaDTO> mainQueryColumns = new ArrayList<>();

    private List<ColumnMetaDTO> mainFormColumns = new ArrayList<>();

    private List<ColumnMetaDTO> mainTableColumns = new ArrayList<>();

    private List<ColumnMetaDTO> treeQueryColumns = new ArrayList<>();

    private List<ColumnMetaDTO> treeFormColumns = new ArrayList<>();

    private List<ColumnMetaDTO> treeTableColumns = new ArrayList<>();

    private List<ColumnMetaDTO> subQueryColumns = new ArrayList<>();

    private List<ColumnMetaDTO> subFormColumns = new ArrayList<>();

    private List<ColumnMetaDTO> subTableColumns = new ArrayList<>();

    private Set<String> imports = new LinkedHashSet<>();

    private Map<String, Object> extraConfig;
}
