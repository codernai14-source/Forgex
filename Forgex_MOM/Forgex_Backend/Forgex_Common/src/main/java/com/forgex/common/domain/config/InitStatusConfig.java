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
package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 初始化状态配置。
 * <p>
 * 记录系统初始化使用次数与完成标志，供登录页判断是否跳转初始化向导。
 * 字段：
 * - {@code usageCount} 使用次数；
 * - {@code initCompleted} 是否已完成初始化。
 */
@Data
public class InitStatusConfig {
    /** 使用次数 */
    private Integer usageCount;
    /** 是否已完成初始化 */
    private Boolean initCompleted;
}
