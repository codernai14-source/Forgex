package com.forgex.common.web;

import com.forgex.common.i18n.CommonPrompt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 统一响应和状态码契约测试。
 */
class RAndStatusCodeTest {

    /**
     * 所有对外状态码都应保持稳定。
     */
    @Test
    void exposesTheDocumentedStatusCodes() {
        assertEquals(200, StatusCode.SUCCESS);
        assertEquals(404, StatusCode.NOT_FOUND);
        assertEquals(500, StatusCode.BUSINESS_ERROR);
        assertEquals(601, StatusCode.UNAUTHORIZED);
        assertEquals(602, StatusCode.NOT_LOGIN);
        assertEquals(603, StatusCode.MODULE_OFFLINE);
        assertEquals(604, StatusCode.LICENSE_REQUIRED);
        assertEquals(605, StatusCode.LICENSE_INVALID);
        assertEquals(606, StatusCode.PASSWORD_EXPIRED);
    }

    /**
     * 成功响应应携带数据和成功状态码。
     */
    @Test
    void wrapsSuccessfulDataWithTheSuccessCode() {
        R<String> result = R.ok("payload");

        assertEquals(StatusCode.SUCCESS, result.getCode());
        assertEquals("payload", result.getData());
        assertNull(result.getI18n());
    }

    /**
     * 提示码失败响应应保留状态和国际化元数据。
     */
    @Test
    void retainsPromptMetadataForBusinessFailures() {
        R<Void> result = R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.OPERATION_FAILED);

        assertEquals(StatusCode.UNAUTHORIZED, result.getCode());
        assertEquals(CommonPrompt.OPERATION_FAILED, result.getMessageCode());
        assertEquals(CommonPrompt.OPERATION_FAILED.getModule(), result.getI18n().getModule());
        assertEquals(CommonPrompt.OPERATION_FAILED.getPromptCode(), result.getI18n().getCode());
    }
}
