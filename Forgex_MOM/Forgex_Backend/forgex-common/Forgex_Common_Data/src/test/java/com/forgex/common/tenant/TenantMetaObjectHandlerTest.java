package com.forgex.common.tenant;

import com.forgex.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TenantMetaObjectHandler} 的插入和更新字段填充测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class TenantMetaObjectHandlerTest {

    private final TenantMetaObjectHandler handler = new TenantMetaObjectHandler();

    @BeforeAll
    static void initializeTableMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test-entity.xml"), TestEntity.class);
    }

    @AfterEach
    void clearContexts() {
        TenantContext.clear();
        UserContext.clear();
    }

    @Test
    void fillsTenantAuditAndLogicalDeleteFieldsOnInsert() {
        TenantContext.set(101L);
        UserContext.set(202L);
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertEquals(101L, entity.getTenantId());
        assertEquals("202", entity.getCreateBy());
        assertEquals("202", entity.getUpdateBy());
        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
        assertFalse(entity.getDeleted());
    }

    @Test
    void onlyUpdatesMutableAuditFieldsOnUpdate() {
        UserContext.set(303L);
        TestEntity entity = new TestEntity();
        entity.setTenantId(101L);
        entity.setCreateBy("202");
        entity.setCreateTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.updateFill(metaObject);

        assertEquals(101L, entity.getTenantId());
        assertEquals("202", entity.getCreateBy());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), entity.getCreateTime());
        assertEquals("303", entity.getUpdateBy());
        assertNotNull(entity.getUpdateTime());
        assertNull(entity.getDeleted());
    }

    @TableName("test_entity")
    static class TestEntity extends BaseEntity {
    }
}
