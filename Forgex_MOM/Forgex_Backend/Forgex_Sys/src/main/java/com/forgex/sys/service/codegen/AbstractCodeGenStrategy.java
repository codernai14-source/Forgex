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
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成策略抽象基类
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@RequiredArgsConstructor
public abstract class AbstractCodeGenStrategy implements CodeGenStrategy {

    protected final CodegenTemplateRenderer templateRenderer;

    /**
     * 添加文件
     *
     * @param files 文件列表
     * @param category 分类
     * @param path 文件路径
     * @param templateName 模板名称
     * @param context 渲染上下文
     */
    protected void addFile(List<CodegenRenderFileDTO> files, String category, String path,
                           String templateName, CodeGenContextDTO context) {
        if (!shouldGenerate(category, context)) {
            return;
        }
        files.add(new CodegenRenderFileDTO(path, category, templateRenderer.render(templateName, context)));
    }

    /**
     * 创建文件列表
     *
     * @return 空列表
     */
    protected List<CodegenRenderFileDTO> newFiles() {
        return new ArrayList<>();
    }

    protected boolean shouldGenerate(String category, CodeGenContextDTO context) {
        if ("doc".equalsIgnoreCase(category)) {
            return true;
        }
        return context.getGenerateItems().stream()
            .anyMatch(item -> item != null && item.equalsIgnoreCase(category));
    }
}
