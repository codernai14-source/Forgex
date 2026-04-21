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

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成配置查询 DTO
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodegenConfigQueryDTO extends BaseGetParam {

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 页面类型
     */
    private String pageType;

    /**
     * 模块编码
     */
    private String moduleName;

    /**
     * 业务编码
     */
    private String bizName;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 主表名称
     */
    private String mainTableName;
}

