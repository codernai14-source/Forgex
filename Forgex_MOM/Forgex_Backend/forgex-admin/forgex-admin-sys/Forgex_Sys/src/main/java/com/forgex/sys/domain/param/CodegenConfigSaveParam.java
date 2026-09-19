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
package com.forgex.sys.domain.param;

import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成配置保存参数
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodegenConfigSaveParam extends CodeGenRequestDTO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 备注
     */
    private String remark;
}

