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
 * 消息模板批量删除参数
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.controller.SysMessageTemplateController
 */
@Data
public class SysMessageTemplateBatchDeleteParam {

    /** 待删除的消息模板主键ID列表 */
    private List<Long> ids;

    /** 是否操作公共配置，`true` 表示操作 `tenantId=0` */
    private Boolean publicConfig;
}
