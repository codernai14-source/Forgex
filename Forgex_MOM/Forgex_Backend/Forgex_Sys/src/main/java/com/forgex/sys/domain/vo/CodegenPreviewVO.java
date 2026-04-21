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
package com.forgex.sys.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成预览结果
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Data
public class CodegenPreviewVO {

    /**
     * ZIP 文件名
     */
    private String zipFileName;

    /**
     * 文件列表
     */
    private List<CodegenFileVO> files = new ArrayList<>();
}
