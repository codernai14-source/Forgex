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
package com.forgex.sys.service.codegen.template;

import com.forgex.sys.domain.dto.CodeGenContextDTO;
import lombok.RequiredArgsConstructor;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.springframework.stereotype.Component;

/**
 * Beetl 模板渲染器
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CodegenTemplateRenderer {

    private final GroupTemplate groupTemplate;

    /**
     * 渲染模板
     *
     * @param templateName 模板名称
     * @param context 上下文
     * @return 渲染结果
     */
    public String render(String templateName, CodeGenContextDTO context) {
        try {
            Template template = groupTemplate.getTemplate(templateName);
            template.binding("ctx", context);
            template.binding("pageType", context.getPageType());
            template.binding("moduleName", context.getModuleName());
            template.binding("bizName", context.getBizName());
            template.binding("entityName", context.getEntityName());
            template.binding("entityNameLower", context.getEntityNameLower());
            template.binding("subEntityName", context.getSubEntityName());
            template.binding("subEntityNameLower", context.getSubEntityNameLower());
            template.binding("packageName", context.getPackageName());
            template.binding("author", context.getAuthor());
            template.binding("date", context.getDate());
            template.binding("mainTableName", context.getMainTableName());
            template.binding("mainTableComment", context.getMainTableComment());
            template.binding("treeTableName", context.getTreeTableName());
            template.binding("treeTableComment", context.getTreeTableComment());
            template.binding("subTableName", context.getSubTableName());
            template.binding("subTableComment", context.getSubTableComment());
            template.binding("mainPkColumn", context.getMainPkColumn());
            template.binding("mainPkJavaField", context.getMainPkJavaField());
            template.binding("mainPkJavaFieldCapital", context.getMainPkJavaFieldCapital());
            template.binding("mainPkJavaType", context.getMainPkJavaType());
            template.binding("treePkColumn", context.getTreePkColumn());
            template.binding("treePkJavaField", context.getTreePkJavaField());
            template.binding("treePkJavaFieldCapital", context.getTreePkJavaFieldCapital());
            template.binding("treePkJavaType", context.getTreePkJavaType());
            template.binding("treeParentColumn", context.getTreeParentColumn());
            template.binding("treeParentJavaField", context.getTreeParentJavaField());
            template.binding("treeParentJavaFieldCapital", context.getTreeParentJavaFieldCapital());
            template.binding("treeParentJavaType", context.getTreeParentJavaType());
            template.binding("treeLabelColumn", context.getTreeLabelColumn());
            template.binding("treeLabelJavaField", context.getTreeLabelJavaField());
            template.binding("treeLabelJavaFieldCapital", context.getTreeLabelJavaFieldCapital());
            template.binding("treeSortColumn", context.getTreeSortColumn());
            template.binding("treeSortJavaField", context.getTreeSortJavaField());
            template.binding("treeSortJavaFieldCapital", context.getTreeSortJavaFieldCapital());
            template.binding("treeFilterColumn", context.getTreeFilterColumn());
            template.binding("treeFilterJavaField", context.getTreeFilterJavaField());
            template.binding("treeFilterJavaFieldCapital", context.getTreeFilterJavaFieldCapital());
            template.binding("treeFilterJavaType", context.getTreeFilterJavaType());
            template.binding("subPkColumn", context.getSubPkColumn());
            template.binding("subPkJavaField", context.getSubPkJavaField());
            template.binding("subPkJavaFieldCapital", context.getSubPkJavaFieldCapital());
            template.binding("subPkJavaType", context.getSubPkJavaType());
            template.binding("subFkColumn", context.getSubFkColumn());
            template.binding("subFkJavaField", context.getSubFkJavaField());
            template.binding("subFkJavaFieldCapital", context.getSubFkJavaFieldCapital());
            template.binding("subFkJavaType", context.getSubFkJavaType());
            template.binding("subDefaultSortColumn", context.getSubDefaultSortColumn());
            template.binding("subDefaultSortJavaField", context.getSubDefaultSortJavaField());
            template.binding("subDefaultSortJavaFieldCapital", context.getSubDefaultSortJavaFieldCapital());
            template.binding("treeEntityName", context.getTreeEntityName());
            template.binding("treeEntityNameLower", context.getTreeEntityNameLower());
            template.binding("menuName", context.getMenuName());
            template.binding("menuIcon", context.getMenuIcon());
            template.binding("menuPath", context.getMenuPath());
            template.binding("parentMenuPath", context.getParentMenuPath());
            template.binding("permKeyPrefix", context.getPermKeyPrefix());
            template.binding("mainTableCode", context.getMainTableCode());
            template.binding("treeTableCode", context.getTreeTableCode());
            template.binding("subTableCode", context.getSubTableCode());
            template.binding("i18nPrefix", context.getI18nPrefix());
            template.binding("androidFeatureKey", context.getAndroidFeatureKey());
            template.binding("androidRouteConst", context.getAndroidRouteConst());
            template.binding("treeRootValueLiteral", context.getTreeRootValueLiteral());
            template.binding("treeSingle", context.isTreeSingle());
            template.binding("treeDouble", context.isTreeDouble());
            template.binding("androidEnabled", context.isAndroidEnabled());
            template.binding("frontendEnabled", context.isFrontendEnabled());
            template.binding("backendEnabled", context.isBackendEnabled());
            template.binding("sqlEnabled", context.isSqlEnabled());
            template.binding("mainColumns", context.getMainColumns());
            template.binding("treeColumns", context.getTreeColumns());
            template.binding("subColumns", context.getSubColumns());
            template.binding("mainQueryColumns", context.getMainQueryColumns());
            template.binding("mainFormColumns", context.getMainFormColumns());
            template.binding("mainTableColumns", context.getMainTableColumns());
            template.binding("treeQueryColumns", context.getTreeQueryColumns());
            template.binding("treeFormColumns", context.getTreeFormColumns());
            template.binding("treeTableColumns", context.getTreeTableColumns());
            template.binding("subQueryColumns", context.getSubQueryColumns());
            template.binding("subFormColumns", context.getSubFormColumns());
            template.binding("subTableColumns", context.getSubTableColumns());
            template.binding("imports", context.getImports());
            return template.render();
        } catch (Exception ex) {
            return "// 模板渲染失败: " + ex.getMessage();
        }
    }
}
