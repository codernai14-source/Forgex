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
 * 编码规则详情查询参数
 * <p>
 * 用于根据 ID 查询编码规则详情，包含主表和明细表的完整信息。
 * 通常用于编辑页面或详情页面的数据加载。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 */
@Data
public class EncodeRuleGetParam {

    /**
     * 主键 ID（必填）
     */
    private Long id;
}
