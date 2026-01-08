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
package com.forgex.auth.service;

import com.forgex.common.web.R;

/**
 * 租户隔离跳过配置服务
 * 提供运行时热更新能力
 */
public interface TenantIgnoreService {
    /**
     * 重新加载跳过配置
     * @return 是否成功
     */
    R<Boolean> reload();
}

