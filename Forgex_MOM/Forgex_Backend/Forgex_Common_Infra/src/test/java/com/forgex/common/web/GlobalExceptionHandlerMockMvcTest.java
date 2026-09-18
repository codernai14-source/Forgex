package com.forgex.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.LogoutAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全局异常处理器的 Web 映射测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class GlobalExceptionHandlerMockMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ObjectProvider<LogoutAuditService> logoutAuditService = mock(ObjectProvider.class);
        when(logoutAuditService.getIfAvailable()).thenReturn(null);
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler(logoutAuditService))
                .build();
    }

    @Test
    void mapsNotLoginExceptionTo602() throws Exception {
        mockMvc.perform(get("/test/not-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_LOGIN));
    }

    @Test
    void mapsNotRoleExceptionTo601() throws Exception {
        mockMvc.perform(get("/test/not-role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    void preservesAuthorizationCodeFromI18nBusinessException() throws Exception {
        mockMvc.perform(get("/test/i18n-business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void mapsInvalidRequestBodyToBusinessError() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.BUSINESS_ERROR))
                .andExpect(jsonPath("$.message").value("name:不能为空"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/not-login")
        void notLogin() {
            throw new NotLoginException(NotLoginException.NOT_TOKEN, "login", "token missing");
        }

        @GetMapping("/test/not-role")
        void notRole() {
            throw new NotRoleException("sys:admin");
        }

        @GetMapping("/test/i18n-business")
        void i18nBusiness() {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }

        @PostMapping("/test/validated")
        void validated(@Valid @RequestBody RequestBodyPayload payload) {
        }
    }

    static class RequestBodyPayload {
        @NotBlank(message = "不能为空")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
