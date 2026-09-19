package com.forgex.sys.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验用户直属上级字段在初始化和升级脚本中保持同步。
 */
class SysUserSuperiorColumnMigrationTest {

    @Test
    void shouldDeclareSuperiorUserIdInInitialAndUpgradeSql() throws Exception {
        Path repositoryRoot = findRepositoryRoot();
        String initSql = Files.readString(repositoryRoot.resolve(
                "Forgex_Doc/部署/数据库初始化脚本/forgex_admin.sql"));
        String upgradeSql = Files.readString(repositoryRoot.resolve(
                "Forgex_Doc/数据库/脚本与修复/sql_fix/20260812_sys_user_superior_user_id.sql"));

        assertTrue(initSql.contains("`superior_user_id` bigint"));
        assertTrue(upgradeSql.contains("`superior_user_id` bigint"));
        assertTrue(upgradeSql.contains("information_schema.COLUMNS"));
        assertTrue(upgradeSql.contains("ADD COLUMN `superior_user_id`"));
    }

    private Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.isDirectory(current.resolve("Forgex_Doc"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("无法定位仓库根目录");
        }
        return current;
    }
}
