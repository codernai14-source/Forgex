package com.forgex.common.tenant;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 通用字段自动填充处理器
 * 插入与更新时自动写入租户ID、时间戳等
 */
public class TenantMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入填充
     * 逻辑：
     * 1. 从租户上下文获取租户ID并写入
     * 2. 写入创建时间、更新时间、逻辑删除标志
     * @param metaObject MyBatis 元对象
     * @see TenantContext#get()
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
        }
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    /**
     * 更新填充
     * 逻辑：仅更新时间戳
     * @param metaObject MyBatis 元对象
     * @see java.time.LocalDateTime#now()
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

