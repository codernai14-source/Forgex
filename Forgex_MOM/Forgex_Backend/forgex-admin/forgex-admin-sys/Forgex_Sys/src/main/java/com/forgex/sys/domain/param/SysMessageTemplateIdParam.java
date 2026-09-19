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
 * 消息模板主键参数
 * <p>
 * 用于详情、单条删除等接口，前端统一按 JSON 结构传参。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.controller.SysMessageTemplateController
 */
@Data
public class SysMessageTemplateIdParam {

    /** 消息模板主键ID */
    private Long id;

    /** 是否操作公共配置，`true` 表示操作 `tenantId=0` */
    private Boolean publicConfig;
}
