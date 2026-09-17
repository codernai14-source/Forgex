package com.forgex.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 强制访问控制下读上写规则测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class MandatoryAccessControlHandlerTest {

    /**
     * 主体密级低于数据密级时不可读，但可写更高密级数据。
     */
    @Test
    void readDownWriteUp() {
        assertTrue(MandatoryAccessControlHandler.canRead(2, 1));
        assertFalse(MandatoryAccessControlHandler.canRead(1, 3));
        assertTrue(MandatoryAccessControlHandler.canWrite(1, 3));
        assertFalse(MandatoryAccessControlHandler.canWrite(3, 0));
    }
}
