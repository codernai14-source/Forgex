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
package com.forgex.sys.domain.vo;
import lombok.Data;

/**
 * 初始化状态返回对象。
 * <p>
 * 字段：
 * - {@code firstUse} 是否首次使用（需要进入初始化向导）；
 * - {@code usageCount} 使用次数计数（0 表示从未初始化）。
 */
@Data
public class InitStatusVO {
    /** 是否首次使用 */
    private boolean firstUse;
    /** 使用次数计数 */
    private int usageCount;
}
