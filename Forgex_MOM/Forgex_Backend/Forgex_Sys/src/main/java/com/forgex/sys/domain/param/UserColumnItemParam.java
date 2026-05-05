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

/**
 * 用户列配置项参数。
 * <p>
 * 表示单个列的配置信息，包含字段名、显示状态、排序顺序和列宽。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>field - 字段名，对应数据表字段</li>
 *   <li>visible - 是否显示，true 时显示该列，false 时隐藏</li>
 *   <li>order - 排序顺序，数值越小排在越前面</li>
 *   <li>width - 列宽度，单位为 px</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class UserColumnItemParam {

    /**
     * 字段名。
     * <p>对应数据表的实际字段名，用于标识列。</p>
     */
    private String field;

    /**
     * 是否显示。
     * <p>true 时显示该列，false 时隐藏该列。</p>
     */
    private Boolean visible;

    /**
     * 排序顺序。
     * <p>数值越小排在越前面，用于控制列的显示顺序。</p>
     */
    private Integer order;

    /**
     * 列宽度。
     * <p>用于用户个性化配置，单位为 px。</p>
     */
    private Integer width;
}
