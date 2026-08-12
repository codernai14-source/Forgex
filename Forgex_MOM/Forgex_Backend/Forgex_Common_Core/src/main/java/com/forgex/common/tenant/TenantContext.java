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
 * 租户上下文管理类。
 * <p>
 * 使用ThreadLocal保存当前请求线程的租户ID，实现多租户数据隔离。
 * </p>
 * <p>核心功能：</p>
 * <ul>
 *   <li>{@link #set(Long)} - 设置当前线程的租户ID</li>
 *   <li>{@link #get()} - 获取当前线程的租户ID</li>
 *   <li>{@link #clear()} - 清理当前线程的租户ID</li>
 * </ul>
 * <p>使用场景：</p>
 * <ul>
 *   <li>网关拦截器：从请求头提取租户ID并设置到上下文</li>
 *   <li>MyBatis拦截器：从上下文获取租户ID并拼接到SQL</li>
 *   <li>业务逻辑：从上下文获取租户ID进行数据隔离</li>
 * </ul>
 * <p>注意事项：</p>
 * <ul>
 *   <li>务必在请求结束时调用clear()方法清理上下文，避免内存泄漏</li>
 *   <li>在异步线程中无法获取主线程的上下文，需要手动传递</li>
 *   <li>配合TenantContextInterceptor拦截器使用</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.common.tenant.TenantContextInterceptor
 */
public class TenantContext {
    /**
     * 当前线程持有的租户ID存储
     * <p>
     * 使用ThreadLocal实现线程隔离，每个请求线程都有独立的租户ID存储空间
     * </p>
     */
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的租户ID。
     * <p>
     * 将租户ID设置到当前线程的ThreadLocal中，用于后续业务逻辑获取。
     * </p>
     * <p>使用示例：</p>
     * <pre>{@code
     * // 在拦截器或过滤器中设置租户ID
     * Long tenantId = getTenantIdFromHeader();
     * TenantContext.set(tenantId);
     * 
     * try {
     *     // 执行业务逻辑
     *     businessService.doSomething();
     * } finally {
     *     // 清理上下文，避免内存泄漏
     *     TenantContext.clear();
     * }
     * }</pre>
     *
     * @param tenantId 租户ID，可以为null
     */
    public static void set(Long tenantId) {
        // 将租户ID设置到当前线程的ThreadLocal中
        HOLDER.set(tenantId);
    }

    /**
     * 获取当前线程的租户ID。
     * <p>
     * 从当前线程的ThreadLocal中获取租户ID，用于数据隔离。
     * </p>
     * <p>使用示例：</p>
     * <pre>{@code
     * // 在业务逻辑中获取租户ID
     * Long tenantId = TenantContext.get();
     * if (tenantId != null) {
     *     // 使用租户ID进行数据查询
     *     List<User> users = userService.listByTenant(tenantId);
     * }
     * }</pre>
     *
     * @return 当前线程保存的租户ID，未设置时返回null
     */
    public static Long get() {
        // 从当前线程的ThreadLocal中获取租户ID
        return HOLDER.get();
    }

    /**
     * 清理当前线程的租户ID。
     * <p>
     * 从当前线程的ThreadLocal中移除租户ID，避免内存泄漏。
     * </p>
     * <p>重要提示：</p>
     * <ul>
     *   <li>务必在请求结束时调用此方法</li>
     *   <li>建议在拦截器的afterCompletion方法中调用</li>
     *   <li>不清理可能导致内存泄漏</li>
     * </ul>
     *
     * @see TenantContext#set(Long)
     * @see TenantContext#get()
     */
    public static void clear() {
        // 从当前线程的ThreadLocal中移除租户ID
        HOLDER.remove();
    }
}

