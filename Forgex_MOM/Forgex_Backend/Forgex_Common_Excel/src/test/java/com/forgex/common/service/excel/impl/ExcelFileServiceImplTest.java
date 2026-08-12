package com.forgex.common.service.excel.impl;

import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigItemDTO;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelFileServiceImplTest {

    private final ExcelFileServiceImpl service = new ExcelFileServiceImpl(new com.fasterxml.jackson.databind.ObjectMapper());

    @Test
    void buildExportResponseShouldStreamBeanData() throws Exception {
        FxExcelExportConfigDTO config = new FxExcelExportConfigDTO();
        config.setExportFormat("xlsx");
        config.setItems(List.of(item("name", "名称", 1), item("createdAt", "创建时间", 2)));

        ResponseEntity<InputStreamResource> response = service.buildExportResponse(
            config,
            List.of(new ExportRow("Alice", LocalDateTime.of(2026, 6, 26, 10, 15, 30))),
            "user-export.xlsx"
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("user-export.xlsx"));

        InputStreamResource body = response.getBody();
        assertNotNull(body);

        try (InputStream inputStream = body.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            assertEquals("Alice", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
            assertEquals("2026-06-26 10:15:30", workbook.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
        }
    }

    private FxExcelExportConfigItemDTO item(String exportField, String fieldName, int orderNum) {
        FxExcelExportConfigItemDTO item = new FxExcelExportConfigItemDTO();
        item.setExportField(exportField);
        item.setFieldName(fieldName);
        item.setOrderNum(orderNum);
        return item;
    }

    private static final class ExportRow {
        private final String name;
        private final LocalDateTime createdAt;

        private ExportRow(String name, LocalDateTime createdAt) {
            this.name = name;
            this.createdAt = createdAt;
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
