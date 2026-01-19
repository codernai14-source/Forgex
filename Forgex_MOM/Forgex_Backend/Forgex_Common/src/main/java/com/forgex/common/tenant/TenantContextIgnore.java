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
package com.forgex.common.tenant;

/**
 * 方法级租户隔离忽略上下文
 * 通过 ThreadLocal 控制本次调用是否跳过租户隔离
 */
public final class TenantContextIgnore {
    private static final ThreadLocal<Boolean> HOLDER = new ThreadLocal<>();

    /** 设置是否忽略租户隔离 */
    public static void setIgnore(boolean ignore) {
        HOLDER.set(ignore);
    }

    /** 当前是否忽略租户隔离 */
    public static boolean isIgnore() {
        Boolean v = HOLDER.get();
        return v != null && v;
    }

    /** 清理状态 */
    public static void clear() {
        HOLDER.remove();
    }
}

