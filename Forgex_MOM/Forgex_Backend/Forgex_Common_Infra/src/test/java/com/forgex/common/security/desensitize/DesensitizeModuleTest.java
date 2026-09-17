package com.forgex.common.security.desensitize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 审计脱敏模块测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class DesensitizeModuleTest {

    /**
     * 字段名兜底应遮蔽 password / phone。
     */
    @Test
    void maskSensitiveFieldNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new DesensitizeModule());
        String json = mapper.writeValueAsString(new Sample("Aa123456", "13800138000"));
        assertFalse(json.contains("Aa123456"));
        assertTrue(json.contains("***") || json.contains("*"));
        assertFalse(json.contains("13800138000"));
    }

    /**
     * 脱敏样例对象。
     */
    public static class Sample {
        public String password;
        public String userPhone;

        public Sample(String password, String userPhone) {
            this.password = password;
            this.userPhone = userPhone;
        }
    }
}
