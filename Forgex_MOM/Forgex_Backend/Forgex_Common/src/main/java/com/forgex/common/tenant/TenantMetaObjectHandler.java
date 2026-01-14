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

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 通用字段自动填充处理器。
 * <p>
 * 在插入与更新时自动写入租户ID、审计字段与时间戳等，避免业务层重复赋值。
 * <p>
 * 填充来源：
 * <ul>
 *     <li>租户：{@link TenantContext}</li>
 *     <li>用户：{@link UserContext}</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
 * @see TenantContext
 * @see UserContext
 */
public class TenantMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入填充。
     * 逻辑：
     * <ol>
     *     <li>从租户上下文获取租户ID并写入</li>
     *     <li>从用户上下文获取用户ID并写入创建人/更新人</li>
     *     <li>写入创建时间、更新时间、逻辑删除标志</li>
     * </ol>
     *
     * @param metaObject MyBatis 元对象
     * @see TenantContext#get()
     * @see UserContext#get()
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
        }
        Long userId = UserContext.get();
        if (userId != null) {
            String operator = String.valueOf(userId);
            this.strictInsertFill(metaObject, "createBy", String.class, operator);
            this.strictInsertFill(metaObject, "updateBy", String.class, operator);
        }
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
    }

    /**
     * 更新填充。
     * <p>
     * 逻辑：写入更新时间与更新人。
     *
     * @param metaObject MyBatis 元对象
     * @see java.time.LocalDateTime#now()
     * @see UserContext#get()
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        Long userId = UserContext.get();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, String.valueOf(userId));
        }
    }
}

