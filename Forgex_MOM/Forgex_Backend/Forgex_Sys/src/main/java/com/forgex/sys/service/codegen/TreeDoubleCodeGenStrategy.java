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
package com.forgex.sys.service.codegen;

import com.forgex.sys.domain.dto.CodeGenContextDTO;
import com.forgex.sys.domain.dto.CodegenRenderFileDTO;
import com.forgex.sys.service.codegen.template.CodegenTemplateRenderer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 双表左树右表代码生成策略。
 *
 * @author coder_nai@163.com
 * @since 2026-04-24
 */
@Component
public class TreeDoubleCodeGenStrategy extends AbstractCodeGenStrategy {

    public TreeDoubleCodeGenStrategy(CodegenTemplateRenderer templateRenderer) {
        super(templateRenderer);
    }

    @Override
    public boolean supports(String pageType) {
        return "TREE_DOUBLE".equalsIgnoreCase(pageType);
    }

    @Override
    public List<CodegenRenderFileDTO> generate(CodeGenContextDTO context) {
        List<CodegenRenderFileDTO> files = newFiles();
        String javaPath = "backend/" + context.getPackageName().replace('.', '/');
        String viewPath = "frontend/src/views/" + context.getModuleName() + "/" + context.getEntityNameLower();
        String apiPath = "frontend/src/api/" + context.getModuleName();
        String androidBasePath = "android/" + context.getAndroidFeatureKey();

        addFile(files, "backend", javaPath + "/domain/entity/" + context.getEntityName() + ".java",
            "tree-double/backend/Entity.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/entity/" + context.getTreeEntityName() + ".java",
            "tree-double/backend/TreeEntity.java.btl", context);
        addFile(files, "backend", javaPath + "/controller/" + context.getEntityName() + "Controller.java",
            "tree-double/backend/Controller.java.btl", context);
        addFile(files, "backend", javaPath + "/service/" + context.getEntityName() + "Service.java",
            "tree-double/backend/Service.java.btl", context);
        addFile(files, "backend", javaPath + "/service/impl/" + context.getEntityName() + "ServiceImpl.java",
            "tree-double/backend/ServiceImpl.java.btl", context);
        addFile(files, "backend", javaPath + "/mapper/" + context.getEntityName() + "Mapper.java",
            "tree-double/backend/Mapper.java.btl", context);
        addFile(files, "backend", "backend/resources/mapper/" + context.getEntityName() + "Mapper.xml",
            "tree-double/backend/Mapper.xml.btl", context);
        addFile(files, "backend", javaPath + "/domain/param/" + context.getEntityName() + "PageParam.java",
            "tree-double/backend/PageParam.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/param/" + context.getEntityName() + "SaveParam.java",
            "tree-double/backend/SaveParam.java.btl", context);
        addFile(files, "frontend", viewPath + "/index.vue", "tree-double/frontend/index.vue.btl", context);
        addFile(files, "frontend", apiPath + "/" + context.getEntityNameLower() + ".ts", "tree-double/frontend/api.ts.btl", context);
        addFile(files, "sql", "sql/01_menu_permission.sql", "tree-double/sql/menu_permission.sql.btl", context);
        addFile(files, "sql", "sql/02_table_config.sql", "tree-double/sql/table_config.sql.btl", context);
        addFile(files, "sql", "sql/03_i18n_message.sql", "tree-double/sql/i18n_message.sql.btl", context);
        addFile(files, "sql", "sql/04_response_message.sql", "tree-double/sql/response_message.sql.btl", context);
        addAndroidFiles(files, androidBasePath, "tree-double/android", context);
        addFile(files, "doc", "README.md", "common/README.md.btl", context);
        return files;
    }

    private void addAndroidFiles(List<CodegenRenderFileDTO> files, String androidBasePath, String templateRoot, CodeGenContextDTO context) {
        if (!context.isAndroidEnabled()) {
            return;
        }
        addFile(files, "android", androidBasePath + "/api/" + context.getEntityName() + "Api.kt",
            templateRoot + "/Api.kt.btl", context);
        addFile(files, "android", androidBasePath + "/model/" + context.getEntityName() + "Models.kt",
            templateRoot + "/Models.kt.btl", context);
        addFile(files, "android", androidBasePath + "/repository/" + context.getEntityName() + "Repository.kt",
            templateRoot + "/Repository.kt.btl", context);
        addFile(files, "android", androidBasePath + "/ui/" + context.getEntityName() + "ListViewModel.kt",
            templateRoot + "/ListViewModel.kt.btl", context);
        addFile(files, "android", androidBasePath + "/ui/" + context.getEntityName() + "ListScreen.kt",
            templateRoot + "/ListScreen.kt.btl", context);
        addFile(files, "android", androidBasePath + "/ui/" + context.getEntityName() + "FormViewModel.kt",
            templateRoot + "/FormViewModel.kt.btl", context);
        addFile(files, "android", androidBasePath + "/ui/" + context.getEntityName() + "FormScreen.kt",
            templateRoot + "/FormScreen.kt.btl", context);
        addFile(files, "android", androidBasePath + "/navigation/" + context.getEntityName() + "Routes.kt",
            templateRoot + "/Routes.kt.btl", context);
        addFile(files, "android", androidBasePath + "/navigation/" + context.getEntityName() + "Navigation.kt",
            templateRoot + "/Navigation.kt.btl", context);
        addFile(files, "android", androidBasePath + "/README.md", templateRoot + "/README.md.btl", context);
    }
}
