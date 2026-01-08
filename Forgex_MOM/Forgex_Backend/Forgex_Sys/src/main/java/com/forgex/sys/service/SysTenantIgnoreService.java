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
package com.forgex.sys.service;

import com.forgex.common.web.R;

/**
 * 租户忽略配置服务。
 * <p>
 * 提供运行时重新加载忽略规则的能力，用于热更新租户隔离白名单（表/服务/Mapper方法）。
 */
public interface SysTenantIgnoreService {
    /**
     * 重新加载忽略规则。
     * 从数据库读取启用的规则并写入注册表。
     * @return 是否成功
     */
    R<Boolean> reload();
}
