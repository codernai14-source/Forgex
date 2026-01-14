package com.forgex.common.service.excel.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigItemDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigItemDTO;
import com.forgex.common.service.excel.ExcelFileService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Excel 文件导入导出服务实现（基于 Apache POI）。
 * <p>
 * 该实现类基于数据库中的导入/导出配置（表头、字段顺序、多语言、样式 JSON 等），
 * 使用 Apache POI 动态构建 Excel 工作簿，实现如下功能：<br>
 * 1) 生成导入模板（包含标题、说明与字段校验提示）；<br>
 * 2) 生成导出文件（支持 xlsx/csv、多语言列头与丰富样式）；<br>
 * 3) 解析上传的导入文件并转换为指定实体类型列表。<br>
 * </p>
 *
 * <p>
 * 样式相关字段以 JSON 形式由前端配置，例如字体、颜色、对齐方式、边框和冻结列等，
 * 在 {@link #applyCellStyle(Workbook, Cell, String)} 中统一转化为 POI {@link CellStyle}。<br>
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see Workbook
 * @see com.forgex.common.service.excel.ExcelFileService
 * @see FxExcelExportConfigDTO
 * @see FxExcelImportConfigDTO
 */
@Service
@RequiredArgsConstructor
public class ExcelFileServiceImpl implements ExcelFileService {

    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] buildImportTemplateXlsx(FxExcelImportConfigDTO config) {
        if (config == null || config.getItems() == null || config.getItems().isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Template");

            List<FxExcelImportConfigItemDTO> items = new ArrayList<>(config.getItems());
            items.sort(Comparator.comparing(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));

            int columnSize = items.isEmpty() ? 1 : items.size();

            int rowIndex = 0;
            if (StringUtils.hasText(config.getTitle())) {
                Row row = sheet.createRow(rowIndex);
                Cell cell = row.createCell(0);
                cell.setCellValue(config.getTitle());
                if (columnSize > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, columnSize - 1));
                }
                rowIndex++;
            }
            if (StringUtils.hasText(config.getSubtitle())) {
                Row row = sheet.createRow(rowIndex);
                Cell cell = row.createCell(0);
                cell.setCellValue(config.getSubtitle());
                if (columnSize > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, columnSize - 1));
                }
                rowIndex++;
            }

            Row headerRow = sheet.createRow(rowIndex);
            int colIndex = 0;
            for (FxExcelImportConfigItemDTO item : items) {
                String header = resolveHeader(item.getI18nJson(), item.getImportField());
                if (Boolean.TRUE.equals(item.getRequired())) {
                    header = header + " *";
                }
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(header);
            }

            sheet.createFreezePane(0, rowIndex + 1);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * 根据导出配置生成 xlsx 格式文件。
     * <p>
     * 支持：<br>
     * 1) 标题行与说明行合并单元格并应用主表样式；<br>
     * 2) 列头与内容分别应用子表配置的 headerStyleJson/cellStyleJson；<br>
     * 3) 按列配置或主表配置冻结前若干列。<br>
     * </p>
     *
     * @param config 导出配置（主+子）
     * @param rows   导出数据行
     * @return 生成的 xlsx 文件字节数组
     */
    private byte[] buildXlsx(FxExcelExportConfigDTO config, List<Map<String, Object>> rows) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Export");

            List<FxExcelExportConfigItemDTO> items = new ArrayList<>(config.getItems());
            items.sort(Comparator.comparing(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));

            int columnSize = items.isEmpty() ? 1 : items.size();

            int rowIndex = 0;
            if (StringUtils.hasText(config.getTitle())) {
                Row titleRow = sheet.createRow(rowIndex);
                Cell cell = titleRow.createCell(0);
                cell.setCellValue(config.getTitle());
                applyCellStyle(wb, cell, config.getHeaderStyleJson());
                if (columnSize > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, columnSize - 1));
                }
                rowIndex++;
            }
            if (StringUtils.hasText(config.getSubtitle())) {
                Row subRow = sheet.createRow(rowIndex);
                Cell cell = subRow.createCell(0);
                cell.setCellValue(config.getSubtitle());
                applyCellStyle(wb, cell, config.getHeaderStyleJson());
                if (columnSize > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, columnSize - 1));
                }
                rowIndex++;
            }

            Row headerRow = sheet.createRow(rowIndex);
            int colIndex = 0;
            for (FxExcelExportConfigItemDTO item : items) {
                String header = resolveHeader(item.getI18nJson(), item.getFieldName());
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(header);
                applyCellStyle(wb, cell, item.getHeaderStyleJson());
            }

            int freezeCols = resolveFreezeColumnCount(config, items);
            if (freezeCols < 0) {
                freezeCols = 0;
            }
            sheet.createFreezePane(freezeCols, rowIndex + 1);

            if (rows != null && !rows.isEmpty()) {
                int dataRowIndex = rowIndex + 1;
                for (Map<String, Object> r : rows) {
                    Row dataRow = sheet.createRow(dataRowIndex++);
                    int c = 0;
                    for (FxExcelExportConfigItemDTO item : items) {
                        Cell cell = dataRow.createCell(c++);
                        Object v = r == null ? null : r.get(item.getExportField());
                        cell.setCellValue(formatValue(v));
                        applyCellStyle(wb, cell, item.getCellStyleJson());
                    }
                }
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * 根据导出配置生成 CSV 格式文件。
     * <p>
     * 仅输出纯文本内容，不包含样式信息。列头与数据内容均按导出配置字段顺序输出，
     * 并对逗号、双引号、换行符进行转义。<br>
     * </p>
     *
     * @param config 导出配置（主+子）
     * @param rows   导出数据行
     * @return CSV 字节数组（UTF-8 编码）
     */
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

    /**
     * 对 CSV 单元格文本进行转义。
     * <p>
     * 若包含逗号、双引号或换行符，则使用双引号包裹，并将内部双引号替换为两个双引号。<br>
     * </p>
     *
     * @param s 原始文本
     * @return 转义后的文本
     */
    private String escapeCsv(String s) {
        if (s == null) {
            return "";
        }
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }

    /**
     * 将导出数据值格式化为字符串。
     *
     * @param v 原始值对象
     * @return 适合写入 Excel/CSV 的字符串表示
     */
    private String formatValue(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof LocalDateTime) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format((LocalDateTime) v);
        }
        if (v instanceof LocalDate) {
            return DateTimeFormatter.ISO_DATE.format((LocalDate) v);
        }
        if (v instanceof LocalTime) {
            return DateTimeFormatter.ISO_TIME.format((LocalTime) v);
        }
        if (v instanceof java.util.Date) {
            Instant instant = ((java.util.Date) v).toInstant();
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(ldt);
        }
        return String.valueOf(v);
    }

    /**
     * 解析列头多语言文本。
     * <p>
     * 优先按 JSON 中的 {@code zh-CN}、{@code zh} 键获取文本，
     * 若解析失败或文本为空，则回退为传入的默认列头。<br>
     * </p>
     *
     * @param i18nJson 多语言 JSON 字符串
     * @param fallback 默认列头文本
     * @return 解析后的列头文本
     */
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

    /**
     * 根据样式 JSON 为单元格应用样式。
     * <p>
     * 支持字段：bold、italic、fontSize、fontColor、align、border、fillColor、wrapText、dataFormat 等，
     * 具体取值由前端配置并存储在 headerStyleJson/cellStyleJson 中。<br>
     * </p>
     *
     * @param wb        工作簿对象
     * @param cell      需要设置样式的单元格
     * @param styleJson 样式 JSON 字符串
     */
    private void applyCellStyle(Workbook wb, Cell cell, String styleJson) {
        if (!StringUtils.hasText(styleJson)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(styleJson);
            CellStyle base = cell.getCellStyle();
            CellStyle style = wb.createCellStyle();
            if (base != null) {
                style.cloneStyleFrom(base);
            }
            Font font = wb.createFont();

            JsonNode bold = node.get("bold");
            if (bold != null && bold.isBoolean() && bold.booleanValue()) {
                font.setBold(true);
            }
            JsonNode italic = node.get("italic");
            if (italic != null && italic.isBoolean() && italic.booleanValue()) {
                font.setItalic(true);
            }
            JsonNode fontSize = node.get("fontSize");
            if (fontSize != null && fontSize.canConvertToInt()) {
                font.setFontHeightInPoints((short) fontSize.intValue());
            }
            JsonNode fontColor = node.get("fontColor");
            if (fontColor != null && fontColor.isTextual() && StringUtils.hasText(fontColor.asText())) {
                Short colorIndex = resolveColorIndex(fontColor.asText());
                if (colorIndex != null) {
                    font.setColor(colorIndex);
                }
            }
            style.setFont(font);

            JsonNode align = node.get("align");
            if (align != null && align.isTextual()) {
                String a = align.asText();
                if ("center".equalsIgnoreCase(a)) {
                    style.setAlignment(HorizontalAlignment.CENTER);
                } else if ("right".equalsIgnoreCase(a)) {
                    style.setAlignment(HorizontalAlignment.RIGHT);
                } else if ("left".equalsIgnoreCase(a)) {
                    style.setAlignment(HorizontalAlignment.LEFT);
                }
            }

            JsonNode border = node.get("border");
            if (border != null && border.isTextual()) {
                BorderStyle borderStyle = resolveBorderStyle(border.asText());
                if (borderStyle != null) {
                    style.setBorderTop(borderStyle);
                    style.setBorderBottom(borderStyle);
                    style.setBorderLeft(borderStyle);
                    style.setBorderRight(borderStyle);
                }
            }

            JsonNode fillColor = node.get("fillColor");
            if (fillColor != null && fillColor.isTextual() && StringUtils.hasText(fillColor.asText())) {
                Short colorIndex = resolveColorIndex(fillColor.asText());
                if (colorIndex != null) {
                    style.setFillForegroundColor(colorIndex);
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
            }

            JsonNode wrapText = node.get("wrapText");
            if (wrapText != null && wrapText.isBoolean()) {
                style.setWrapText(wrapText.booleanValue());
            }

            JsonNode dataFormat = node.get("dataFormat");
            if (dataFormat != null && dataFormat.isTextual() && StringUtils.hasText(dataFormat.asText())) {
                DataFormat df = wb.createDataFormat();
                style.setDataFormat(df.getFormat(dataFormat.asText()));
            }

            cell.setCellStyle(style);
        } catch (Exception ignored) {
        }
    }

    /**
     * 解析需要冻结的列数。
     * <p>
     * 优先使用主表 headerStyleJson 中的 freezeCols 配置；<br>
     * 若列头样式中存在 {@code fixed = true} 的列，则以该列的下标（+1）作为冻结列数的最大值。<br>
     * </p>
     *
     * @param config 导出主表配置
     * @param items  导出字段子项列表
     * @return 冻结列数（&gt;=0）
     */
    private int resolveFreezeColumnCount(FxExcelExportConfigDTO config, List<FxExcelExportConfigItemDTO> items) {
        int result = 0;
        try {
            if (config != null && StringUtils.hasText(config.getHeaderStyleJson())) {
                JsonNode node = objectMapper.readTree(config.getHeaderStyleJson());
                JsonNode freezeCols = node.get("freezeCols");
                if (freezeCols != null && freezeCols.canConvertToInt()) {
                    result = Math.max(result, freezeCols.intValue());
                }
            }
        } catch (Exception ignored) {
        }
        for (int i = 0; i < items.size(); i++) {
            FxExcelExportConfigItemDTO item = items.get(i);
            String styleJson = item.getHeaderStyleJson();
            if (!StringUtils.hasText(styleJson)) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(styleJson);
                JsonNode fixed = node.get("fixed");
                if (fixed != null && fixed.isBoolean() && fixed.booleanValue()) {
                    result = Math.max(result, i + 1);
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * 将颜色名称解析为 POI 颜色索引。
     * <p>
     * 颜色名称需为 {@link IndexedColors} 的枚举名称，如：RED、BLUE、YELLOW 等。<br>
     * </p>
     *
     * @param colorName 颜色名称
     * @return 对应的颜色索引；解析失败时返回 {@code null}
     */
    private Short resolveColorIndex(String colorName) {
        try {
            String upper = colorName.trim().toUpperCase();
            IndexedColors ic = IndexedColors.valueOf(upper);
            return ic.getIndex();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将边框类型字符串解析为 POI 边框样式。
     *
     * @param border 边框类型字符串（thin/medium/thick/dashed/dotted 等）
     * @return 对应的 {@link BorderStyle}；解析失败时返回 {@code null}
     */
    private BorderStyle resolveBorderStyle(String border) {
        String v = border.trim().toLowerCase();
        if ("thin".equals(v)) {
            return BorderStyle.THIN;
        }
        if ("medium".equals(v)) {
            return BorderStyle.MEDIUM;
        }
        if ("thick".equals(v)) {
            return BorderStyle.THICK;
        }
        if ("dashed".equals(v)) {
            return BorderStyle.DASHED;
        }
        if ("dotted".equals(v)) {
            return BorderStyle.DOTTED;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> parseImportFile(FxExcelImportConfigDTO config, InputStream inputStream, Class<T> targetClass) {
        List<T> result = new ArrayList<>();
        if (config == null || config.getItems() == null || config.getItems().isEmpty() || inputStream == null || targetClass == null) {
            return result;
        }
        List<FxExcelImportConfigItemDTO> items = new ArrayList<>(config.getItems());
        items.sort(Comparator.comparing(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                return result;
            }
            int headerRowIndex = 0;
            if (StringUtils.hasText(config.getTitle())) {
                headerRowIndex++;
            }
            if (StringUtils.hasText(config.getSubtitle())) {
                headerRowIndex++;
            }
            int firstDataRow = headerRowIndex + 1;
            int lastRow = sheet.getLastRowNum();
            for (int rowIndex = firstDataRow; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                T instance = targetClass.getDeclaredConstructor().newInstance();
                boolean allBlank = true;
                for (int c = 0; c < items.size(); c++) {
                    FxExcelImportConfigItemDTO item = items.get(c);
                    Cell cell = row.getCell(c);
                    String raw = getCellStringValue(cell);
                    if (StringUtils.hasText(raw)) {
                        allBlank = false;
                    }
                    Object value = convertImportValue(raw, item.getFieldType(), targetClass, item.getImportField());
                    if (value != null) {
                        setFieldValue(instance, item.getImportField(), value);
                    }
                }
                if (!allBlank) {
                    result.add(instance);
                }
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 将单元格内容转为字符串。
     * <p>
     * 数值类型会根据是否为日期单元格分别处理：<br>
     * - 日期单元格格式化为 {@code yyyy-MM-dd HH:mm:ss}；<br>
     * - 普通数值使用 {@link BigDecimal} 去除多余小数位与科学计数法。<br>
     * </p>
     *
     * @param cell 单元格对象
     * @return 字符串表示；空单元格返回 {@code null}
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date d = cell.getDateCellValue();
                    if (d == null) {
                        yield null;
                    }
                    Instant instant = d.toInstant();
                    LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    yield DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(ldt);
                } else {
                    double n = cell.getNumericCellValue();
                    BigDecimal bd = BigDecimal.valueOf(n);
                    yield bd.stripTrailingZeros().toPlainString();
                }
            }
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield Double.toString(cell.getNumericCellValue());
                }
            }
            case BLANK, _NONE, ERROR -> null;
        };
    }

    /**
     * 将原始字符串值转换为目标实体字段需要的类型。
     *
     * @param raw         原始字符串值
     * @param fieldType   配置中的字段类型（time/date/datetime/dict/string/number 等）
     * @param targetClass 目标实体类型
     * @param fieldName   目标实体字段名
     * @return 转换后的值对象；解析失败时返回原始字符串
     */
    private Object convertImportValue(String raw, String fieldType, Class<?> targetClass, String fieldName) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        Field field = findField(targetClass, fieldName);
        if (field == null) {
            return null;
        }
        Class<?> type = field.getType();
        String ft = fieldType == null ? "" : fieldType.toLowerCase();
        try {
            if (type == String.class) {
                return raw;
            }
            if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
                BigDecimal bd = new BigDecimal(raw);
                if (type == Integer.class || type == int.class) {
                    return bd.intValue();
                }
                if (type == Long.class || type == long.class) {
                    return bd.longValue();
                }
                if (type == Short.class || type == short.class) {
                    return bd.shortValue();
                }
                if (type == Double.class || type == double.class) {
                    return bd.doubleValue();
                }
                if (type == Float.class || type == float.class) {
                    return bd.floatValue();
                }
                if (type == BigDecimal.class) {
                    return bd;
                }
            }
            if (type == Boolean.class || type == boolean.class) {
                String v = raw.trim();
                if ("1".equals(v) || "true".equalsIgnoreCase(v) || "是".equals(v)) {
                    return true;
                }
                if ("0".equals(v) || "false".equalsIgnoreCase(v) || "否".equals(v)) {
                    return false;
                }
            }
            if (type == LocalDate.class || "date".equals(ft)) {
                return LocalDate.parse(raw.substring(0, 10));
            }
            if (type == LocalDateTime.class || "datetime".equals(ft)) {
                return LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (type == LocalTime.class || "time".equals(ft)) {
                return LocalTime.parse(raw.substring(11), DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
            if (type == java.util.Date.class) {
                LocalDateTime ldt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception ignored) {
        }
        return raw;
    }

    /**
     * 通过反射为目标对象字段赋值。
     *
     * @param target    目标对象实例
     * @param fieldName 字段名
     * @param value     需要设置的字段值
     */
    private void setFieldValue(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ignored) {
        }
    }

    /**
     * 在类及其父类层次结构中查找指定字段。
     *
     * @param type      起始类型
     * @param fieldName 字段名
     * @return 匹配到的字段；未找到时返回 {@code null}
     */
    private Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
