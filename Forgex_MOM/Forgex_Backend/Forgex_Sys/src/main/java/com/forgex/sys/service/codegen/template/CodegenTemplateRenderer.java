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
            template.binding("subTableName", context.getSubTableName());
            template.binding("subTableComment", context.getSubTableComment());
            template.binding("mainPkColumn", context.getMainPkColumn());
            template.binding("mainPkJavaField", context.getMainPkJavaField());
            template.binding("subPkColumn", context.getSubPkColumn());
            template.binding("subPkJavaField", context.getSubPkJavaField());
            template.binding("subFkColumn", context.getSubFkColumn());
            template.binding("subFkJavaField", context.getSubFkJavaField());
            template.binding("subDefaultSortColumn", context.getSubDefaultSortColumn());
            template.binding("subDefaultSortJavaField", context.getSubDefaultSortJavaField());
            template.binding("menuName", context.getMenuName());
            template.binding("menuIcon", context.getMenuIcon());
            template.binding("menuPath", context.getMenuPath());
            template.binding("parentMenuPath", context.getParentMenuPath());
            template.binding("permKeyPrefix", context.getPermKeyPrefix());
            template.binding("mainTableCode", context.getMainTableCode());
            template.binding("subTableCode", context.getSubTableCode());
            template.binding("i18nPrefix", context.getI18nPrefix());
            template.binding("mainColumns", context.getMainColumns());
            template.binding("subColumns", context.getSubColumns());
            template.binding("mainQueryColumns", context.getMainQueryColumns());
            template.binding("mainFormColumns", context.getMainFormColumns());
            template.binding("mainTableColumns", context.getMainTableColumns());
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
