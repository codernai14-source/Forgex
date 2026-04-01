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

import lombok.Data;

import java.util.List;

/**
 * 用户列配置保存参数
 * <p>
 * 用于保存用户个性化表格列配置，包含列的显示/隐藏状态和排序顺序。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>tableCode - 表格编码，唯一标识一个表格</li>
 *   <li>columns - 列配置列表，包含每列的显示状态和排序</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see UserColumnItemParam
 */
@Data
public class UserColumnConfigParam {
    
    /**
     * 表格编码
     * <p>唯一标识一个表格，用于关联表格配置。</p>
     */
    private String tableCode;
    
    /**
     * 列配置列表
     * <p>包含每列的字段名、显示状态和排序顺序。</p>
     */
    private List<UserColumnItemParam> columns;
}