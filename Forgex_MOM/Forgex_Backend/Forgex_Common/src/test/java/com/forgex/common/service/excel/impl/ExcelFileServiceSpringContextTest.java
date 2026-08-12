package com.forgex.common.service.excel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.service.excel.ExcelConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class ExcelFileServiceSpringContextTest {

    @Test
    void springContextShouldCreateExcelFileServiceWithAllDependencies() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ObjectMapper.class, (Supplier<ObjectMapper>) ObjectMapper::new);
            context.registerBean(ExcelConfigService.class, () -> mock(ExcelConfigService.class));
            context.register(ExcelFileServiceImpl.class);

            assertDoesNotThrow(context::refresh);
        }
    }
}
