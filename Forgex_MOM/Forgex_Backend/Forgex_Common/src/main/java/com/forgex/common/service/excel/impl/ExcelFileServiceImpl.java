package com.forgex.common.service.excel.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigItemDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigItemDTO;
import com.forgex.common.service.excel.ExcelFileService;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Excel 文件生成服务实现（基于 FastExcel Writer）。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see Workbook
 */
@Service
@RequiredArgsConstructor
public class ExcelFileServiceImpl implements ExcelFileService {

    private final ObjectMapper objectMapper;

    @Override
    public byte[] buildImportTemplateXlsx(FxExcelImportConfigDTO config) {
        if (config == null || config.getItems() == null || config.getItems().isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Workbook wb = new Workbook(bos, "Forgex", "1.0.0")) {

            Worksheet ws = wb.newWorksheet("Template");

            int row = 0;
            if (StringUtils.hasText(config.getTitle())) {
                ws.value(row++, 0, config.getTitle());
            }
            if (StringUtils.hasText(config.getSubtitle())) {
                ws.value(row++, 0, config.getSubtitle());
            }

            List<FxExcelImportConfigItemDTO> items = new ArrayList<>(config.getItems());
            items.sort(Comparator.comparing(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));

            int col = 0;
            for (FxExcelImportConfigItemDTO item : items) {
                String header = resolveHeader(item.getI18nJson(), item.getImportField());
                if (Boolean.TRUE.equals(item.getRequired())) {
                    header = header + " *";
                }
                ws.value(row, col++, header);
            }

            wb.finish();
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public byte[] buildExportFile(FxExcelExportConfigDTO config, List<Map<String, Object>> rows) {
        if (config == null || config.getItems() == null || config.getItems().isEmpty()) {
            return new byte[0];
        }

        String format = config.getExportFormat();
        if ("csv".equalsIgnoreCase(format)) {
            return buildCsv(config, rows);
        }
        return buildXlsx(config, rows);
    }

    private byte[] buildXlsx(FxExcelExportConfigDTO config, List<Map<String, Object>> rows) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Workbook wb = new Workbook(bos, "Forgex", "1.0.0")) {

            Worksheet ws = wb.newWorksheet("Export");

            List<FxExcelExportConfigItemDTO> items = new ArrayList<>(config.getItems());
            items.sort(Comparator.comparing(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));

            int rowIndex = 0;
            int colIndex = 0;
            for (FxExcelExportConfigItemDTO item : items) {
                String header = resolveHeader(item.getI18nJson(), item.getFieldName());
                ws.value(rowIndex, colIndex, header);
                applyCellStyle(ws, rowIndex, colIndex, item.getHeaderStyleJson());
                colIndex++;
            }

            if (rows != null && !rows.isEmpty()) {
                int dataRow = 1;
                for (Map<String, Object> r : rows) {
                    int c = 0;
                    for (FxExcelExportConfigItemDTO item : items) {
                        Object v = r == null ? null : r.get(item.getExportField());
                        ws.value(dataRow, c, formatValue(v));
                        applyCellStyle(ws, dataRow, c, item.getCellStyleJson());
                        c++;
                    }
                    dataRow++;
                }
            }

            wb.finish();
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private byte[] buildCsv(FxExcelExportConfigDTO config, List<Map<String, Object>> rows) {
        List<FxExcelExportConfigItemDTO> items = new ArrayList<>(config.getItems());
        items.sort(Comparator.comparing(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            FxExcelExportConfigItemDTO item = items.get(i);
            String header = resolveHeader(item.getI18nJson(), item.getFieldName());
            sb.append(escapeCsv(header));
            if (i < items.size() - 1) {
                sb.append(',');
            }
        }
        sb.append('\n');

        if (rows != null) {
            for (Map<String, Object> r : rows) {
                for (int i = 0; i < items.size(); i++) {
                    FxExcelExportConfigItemDTO item = items.get(i);
                    Object v = r == null ? null : r.get(item.getExportField());
                    sb.append(escapeCsv(formatValue(v)));
                    if (i < items.size() - 1) {
                        sb.append(',');
                    }
                }
                sb.append('\n');
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String s) {
        if (s == null) {
            return "";
        }
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }

    private String formatValue(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof java.time.LocalDateTime) {
            return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format((java.time.LocalDateTime) v);
        }
        if (v instanceof java.time.LocalDate) {
            return java.time.format.DateTimeFormatter.ISO_DATE.format((java.time.LocalDate) v);
        }
        if (v instanceof java.time.LocalTime) {
            return java.time.format.DateTimeFormatter.ISO_TIME.format((java.time.LocalTime) v);
        }
        if (v instanceof java.util.Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format((java.util.Date) v);
        }
        return String.valueOf(v);
    }

    private String resolveHeader(String i18nJson, String fallback) {
        String fallbackText = StringUtils.hasText(fallback) ? fallback : "";
        if (!StringUtils.hasText(i18nJson)) {
            return fallbackText;
        }
        try {
            JsonNode node = objectMapper.readTree(i18nJson);
            JsonNode zh = node.get("zh-CN");
            if (zh != null && zh.isTextual() && StringUtils.hasText(zh.asText())) {
                return zh.asText();
            }
            JsonNode zh2 = node.get("zh");
            if (zh2 != null && zh2.isTextual() && StringUtils.hasText(zh2.asText())) {
                return zh2.asText();
            }
            if (node.isObject()) {
                java.util.Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                if (it.hasNext()) {
                    JsonNode first = it.next().getValue();
                    if (first != null && first.isTextual() && StringUtils.hasText(first.asText())) {
                        return first.asText();
                    }
                }
            }
            return fallbackText;
        } catch (Exception e) {
            return fallbackText;
        }
    }

    private void applyCellStyle(Worksheet ws, int row, int col, String styleJson) {
        if (!StringUtils.hasText(styleJson)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(styleJson);
            org.dhatim.fastexcel.StyleSetter st = ws.style(row, col);

            JsonNode bold = node.get("bold");
            if (bold != null && bold.isBoolean() && bold.booleanValue()) {
                st.bold();
            }
            JsonNode italic = node.get("italic");
            if (italic != null && italic.isBoolean() && italic.booleanValue()) {
                st.italic();
            }
            JsonNode align = node.get("align");
            if (align != null && align.isTextual()) {
                String a = align.asText();
                if ("center".equalsIgnoreCase(a)) {
                    st.horizontalAlignment("center");
                } else if ("right".equalsIgnoreCase(a)) {
                    st.horizontalAlignment("right");
                } else if ("left".equalsIgnoreCase(a)) {
                    st.horizontalAlignment("left");
                }
            }
            JsonNode fillColor = node.get("fillColor");
            if (fillColor != null && fillColor.isTextual() && StringUtils.hasText(fillColor.asText())) {
                st.fillColor(fillColor.asText());
            }
            st.set();
        } catch (Exception ignored) {
        }
    }
}
