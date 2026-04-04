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
 * 消息模板主键参数（详情、单条删除等接口使用）。
 * <p>
 * 前端统一以 JSON 传递 {@code { "id": ... }}，避免原始 Long 与 Content-Type 不一致导致的绑定失败。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.controller.SysMessageTemplateController
 */
@Data
public class SysMessageTemplateIdParam {

    /**
     * 消息模板主键 ID
     */
    private Long id;
}
