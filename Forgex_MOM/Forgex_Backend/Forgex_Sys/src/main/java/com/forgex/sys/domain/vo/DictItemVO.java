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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据字典项 VO
 * 用于前端下拉框、单选框等组件
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 * @version 1.1.0
 */
@Data
@Schema(description = "数据字典项")
public class DictItemVO {
    
    /**
     * 字典项名称（显示值）
     */
    @Schema(description = "字典项名称")
    private String label;
    
    /**
     * 字典项值（实际值）
     */
    @Schema(description = "字典项值")
    private String value;

    /**
     * 标签样式配置
     * 用于前端渲染标签时的样式配置，支持颜色和图标
     * 
     * @see TagStyleVO
     */
    @Schema(description = "标签样式配置")
    private TagStyleVO tagStyle;
}
