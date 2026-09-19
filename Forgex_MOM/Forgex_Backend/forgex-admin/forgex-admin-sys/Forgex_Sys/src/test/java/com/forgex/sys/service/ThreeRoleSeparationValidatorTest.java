package com.forgex.sys.service;

import com.forgex.common.security.perm.ThreeRoleSeparation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 三员分立矩阵测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class ThreeRoleSeparationValidatorTest {

    /**
     * admin / auditor 应归一到保留角色族。
     */
    @Test
    void normalizeReservedFamilies() {
        assertEquals(ThreeRoleSeparation.SYS_ADMIN, ThreeRoleSeparation.family("admin"));
        assertEquals(ThreeRoleSeparation.AUDIT_ADMIN, ThreeRoleSeparation.family("auditor"));
        assertTrue(ThreeRoleSeparation.isReserved("SEC_ADMIN"));
        assertTrue(ThreeRoleSeparation.isAuditPermission("sys:operation-log:view"));
        assertFalse(ThreeRoleSeparation.isAuditPermission("sys:user:add"));
        assertTrue(ThreeRoleSeparation.isSecurityPermission("sys:kms:rotate"));
    }
}
