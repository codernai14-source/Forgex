package com.forgex.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 操作日志切面测试。
 */
class OperationLogAspectTest {

    /**
     * 审计记录二进制下载响应时不能提前消费响应流。
     *
     * @throws Throwable 切面执行异常
     */
    @Test
    void shouldNotConsumeInputStreamResourceWhenRecordingDownloadResponse() throws Throwable {
        byte[] content = "login-log-export".getBytes();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(content));
        ResponseEntity<InputStreamResource> response = ResponseEntity.ok(resource);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(response);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        OperationLogRecorder recorder = mock(OperationLogRecorder.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<OperationLogRecorder> recorderProvider = mock(ObjectProvider.class);
        when(recorderProvider.getIfAvailable()).thenReturn(recorder);
        OperationLogAspect aspect = new OperationLogAspect(new ObjectMapper(), recorderProvider);

        aspect.around(joinPoint, downloadOperationLog());

        MockHttpOutputMessage output = new MockHttpOutputMessage();
        new ResourceHttpMessageConverter().write(resource, null, output);
        assertArrayEquals(content, output.getBodyAsBytes());
        ArgumentCaptor<OperationLogRecord> recordCaptor = ArgumentCaptor.forClass(OperationLogRecord.class);
        org.mockito.Mockito.verify(recorder).record(recordCaptor.capture());
        assertEquals("[binary response omitted]", recordCaptor.getValue().getResponseResult());
    }

    /**
     * 构造下载操作日志注解。
     *
     * @return 下载操作日志注解
     */
    private OperationLog downloadOperationLog() {
        return new OperationLog() {
            @Override
            public String module() {
                return "sys";
            }

            @Override
            public String menuPath() {
                return "/system/login-log";
            }

            @Override
            public OperationType operationType() {
                return OperationType.DOWNLOAD;
            }

            @Override
            public String detailTemplateCode() {
                return "LOGIN_LOG_EXPORT";
            }

            @Override
            public String[] detailFields() {
                return new String[0];
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return OperationLog.class;
            }
        };
    }
}
