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
 * 租户上下文
 * 使用 ThreadLocal 保存当前请求线程的租户ID，实现数据隔离
 */
public class TenantContext {
    /** 当前线程持有的租户ID */
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     * @param tenantId 租户ID
     */
    public static void set(Long tenantId) {
        HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     * @return 当前线程保存的租户ID
     */
    public static Long get() {
        return HOLDER.get();
    }

    /**
     * 清理当前租户ID
     * @see TenantContext#set(Long)
     * @see TenantContext#get()
     */
    public static void clear() {
        HOLDER.remove();
    }
}

