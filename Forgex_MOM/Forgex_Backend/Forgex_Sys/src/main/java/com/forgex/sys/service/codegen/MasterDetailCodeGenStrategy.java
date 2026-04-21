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
 * 主子表代码生成策略
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Component
public class MasterDetailCodeGenStrategy extends AbstractCodeGenStrategy {

    public MasterDetailCodeGenStrategy(CodegenTemplateRenderer templateRenderer) {
        super(templateRenderer);
    }

    @Override
    public boolean supports(String pageType) {
        return "MASTER_DETAIL".equalsIgnoreCase(pageType);
    }

    @Override
    public List<CodegenRenderFileDTO> generate(CodeGenContextDTO context) {
        List<CodegenRenderFileDTO> files = newFiles();
        String javaPath = "backend/" + context.getPackageName().replace('.', '/');
        String viewPath = "frontend/src/views/" + context.getModuleName() + "/" + context.getEntityNameLower();
        String apiPath = "frontend/src/api/" + context.getModuleName();
        addFile(files, "backend", javaPath + "/domain/entity/" + context.getEntityName() + ".java",
            "master-detail/backend/MainEntity.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/entity/" + context.getSubEntityName() + ".java",
            "master-detail/backend/SubEntity.java.btl", context);
        addFile(files, "backend", javaPath + "/controller/" + context.getEntityName() + "Controller.java",
            "master-detail/backend/MainController.java.btl", context);
        addFile(files, "backend", javaPath + "/controller/" + context.getSubEntityName() + "Controller.java",
            "master-detail/backend/SubController.java.btl", context);
        addFile(files, "backend", javaPath + "/service/" + context.getEntityName() + "Service.java",
            "master-detail/backend/MainService.java.btl", context);
        addFile(files, "backend", javaPath + "/service/" + context.getSubEntityName() + "Service.java",
            "master-detail/backend/SubService.java.btl", context);
        addFile(files, "backend", javaPath + "/service/impl/" + context.getEntityName() + "ServiceImpl.java",
            "master-detail/backend/MainServiceImpl.java.btl", context);
        addFile(files, "backend", javaPath + "/service/impl/" + context.getSubEntityName() + "ServiceImpl.java",
            "master-detail/backend/SubServiceImpl.java.btl", context);
        addFile(files, "backend", javaPath + "/mapper/" + context.getEntityName() + "Mapper.java",
            "master-detail/backend/MainMapper.java.btl", context);
        addFile(files, "backend", javaPath + "/mapper/" + context.getSubEntityName() + "Mapper.java",
            "master-detail/backend/SubMapper.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/param/" + context.getEntityName() + "PageParam.java",
            "master-detail/backend/MainPageParam.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/param/" + context.getEntityName() + "SaveParam.java",
            "master-detail/backend/MainSaveParam.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/param/" + context.getSubEntityName() + "PageParam.java",
            "master-detail/backend/SubPageParam.java.btl", context);
        addFile(files, "backend", javaPath + "/domain/param/" + context.getSubEntityName() + "SaveParam.java",
            "master-detail/backend/SubSaveParam.java.btl", context);
        addFile(files, "frontend", viewPath + "/index.vue", "master-detail/frontend/index.vue.btl", context);
        addFile(files, "frontend", viewPath + "/detail.vue", "master-detail/frontend/detail.vue.btl", context);
        addFile(files, "frontend", apiPath + "/" + context.getEntityNameLower() + ".ts", "master-detail/frontend/main-api.ts.btl", context);
        addFile(files, "frontend", apiPath + "/" + context.getSubEntityNameLower() + ".ts", "master-detail/frontend/sub-api.ts.btl", context);
        addFile(files, "sql", "sql/01_menu_permission.sql", "master-detail/sql/menu_permission.sql.btl", context);
        addFile(files, "sql", "sql/02_table_config.sql", "master-detail/sql/table_config.sql.btl", context);
        addFile(files, "sql", "sql/03_i18n_message.sql", "master-detail/sql/i18n_message.sql.btl", context);
        addFile(files, "sql", "sql/04_response_message.sql", "master-detail/sql/response_message.sql.btl", context);
        addFile(files, "doc", "README.md", "common/README.md.btl", context);
        return files;
    }
}
