/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.CodeGenContextDTO;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.dto.CodegenRenderFileDTO;
import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.vo.CodegenFileVO;
import com.forgex.sys.domain.vo.CodegenPreviewVO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.CodeGenService;
import com.forgex.sys.service.DbMetaService;
import com.forgex.sys.service.ICodegenConfigService;
import com.forgex.sys.service.ICodegenDatasourceService;
import com.forgex.sys.service.codegen.CodeGenStrategy;
import com.forgex.sys.service.codegen.CodeGenStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Service
@RequiredArgsConstructor
public class CodeGenServiceImpl implements CodeGenService {

    private static final Set<String> BASE_ENTITY_FIELDS = new HashSet<>(
        List.of("id", "tenant_id", "create_time", "create_by", "update_time", "update_by", "deleted")
    );

    private final ICodegenDatasourceService datasourceService;
    private final ICodegenConfigService codegenConfigService;
    private final DbMetaService dbMetaService;
    private final CodeGenStrategyFactory strategyFactory;

    @Override
    public CodegenPreviewVO preview(CodeGenRequestDTO req) {
        validateRequest(req);
        CodeGenContextDTO context = buildContext(req);
        CodeGenStrategy strategy = strategyFactory.getStrategy(context.getPageType());
        List<CodegenRenderFileDTO> renderFiles = strategy.generate(context);
        CodegenPreviewVO preview = new CodegenPreviewVO();
        preview.setZipFileName(buildZipFileName(context));
        for (CodegenRenderFileDTO renderFile : renderFiles) {
            CodegenFileVO fileVO = new CodegenFileVO();
            fileVO.setPath(renderFile.getPath());
            fileVO.setName(extractName(renderFile.getPath()));
            fileVO.setCategory(renderFile.getCategory());
            fileVO.setContent(renderFile.getContent());
            preview.getFiles().add(fileVO);
        }
        return preview;
    }

    @Override
    public byte[] generateZip(CodeGenRequestDTO req) {
        CodegenPreviewVO preview = preview(req);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (CodegenFileVO file : preview.getFiles()) {
                ZipEntry entry = new ZipEntry(file.getPath());
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write((file.getContent() == null ? "" : file.getContent()).getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_PARAM_EMPTY);
        }
    }

    @Override
    public CodegenPreviewVO previewByConfigId(Long configId) {
        CodeGenRequestDTO request = codegenConfigService.buildRequest(configId);
        CodegenPreviewVO preview = preview(request);
        codegenConfigService.markGenerated(configId);
        return preview;
    }

    @Override
    public byte[] generateZipByConfigId(Long configId) {
        CodeGenRequestDTO request = codegenConfigService.buildRequest(configId);
        byte[] zip = generateZip(request);
        codegenConfigService.markGenerated(configId);
        return zip;
    }

    private void validateRequest(CodeGenRequestDTO req) {
        if (req == null
            || req.getDatasourceId() == null
            || !StringUtils.hasText(req.getSchemaName())
            || !StringUtils.hasText(req.getPageType())
            || !StringUtils.hasText(req.getMainTableName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_PARAM_EMPTY);
        }
        if ("MASTER_DETAIL".equalsIgnoreCase(req.getPageType())) {
            if (!StringUtils.hasText(req.getSubTableName())
                || !StringUtils.hasText(req.getMainPkColumn())
                || !StringUtils.hasText(req.getSubFkColumn())
                || !StringUtils.hasText(req.getSubPkColumn())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_PARAM_EMPTY);
            }
        }
    }

    private CodeGenContextDTO buildContext(CodeGenRequestDTO req) {
        SysCodegenDatasource datasource = datasourceService.requireEntity(req.getDatasourceId());
        TableMetaDTO mainTableMeta = dbMetaService.getTableMeta(datasource, req.getSchemaName(), req.getMainTableName());
        List<ColumnMetaDTO> mainColumns = withDefaultColumnFeatures(
            preferRequestColumns(req.getMainColumns(), dbMetaService.listColumns(datasource, req.getSchemaName(), req.getMainTableName()))
        );
        TableMetaDTO subTableMeta = null;
        List<ColumnMetaDTO> subColumns = new ArrayList<>();
        if ("MASTER_DETAIL".equalsIgnoreCase(req.getPageType())) {
            subTableMeta = dbMetaService.getTableMeta(datasource, req.getSchemaName(), req.getSubTableName());
            subColumns = withDefaultColumnFeatures(
                preferRequestColumns(req.getSubColumns(), dbMetaService.listColumns(datasource, req.getSchemaName(), req.getSubTableName()))
            );
            validateMainSubRelation(mainColumns, subColumns, req);
        }

        CodeGenContextDTO context = new CodeGenContextDTO();
        context.setDatasourceCode(datasource.getDatasourceCode());
        context.setSchemaName(req.getSchemaName());
        context.setPageType(req.getPageType().toUpperCase(Locale.ROOT));
        context.setMainTableName(req.getMainTableName());
        context.setMainTableComment(mainTableMeta.getTableComment());
        context.setSubTableName(req.getSubTableName());
        context.setSubTableComment(subTableMeta == null ? null : subTableMeta.getTableComment());
        context.setEntityName(StringUtils.hasText(req.getEntityName()) ? req.getEntityName() : toCamelCase(req.getMainTableName(), true));
        context.setEntityNameLower(toLowerCamel(context.getEntityName()));
        String subEntityName = StringUtils.hasText(req.getSubEntityName())
            ? req.getSubEntityName()
            : (StringUtils.hasText(req.getSubTableName()) ? toCamelCase(req.getSubTableName(), true) : null);
        context.setSubEntityName(subEntityName);
        context.setSubEntityNameLower(toLowerCamel(subEntityName));
        context.setModuleName(defaultText(req.getModuleName(), "system"));
        context.setBizName(defaultText(req.getBizName(), context.getEntityNameLower()));
        context.setPackageName(defaultText(req.getPackageName(), "com.forgex." + context.getModuleName()));
        context.setAuthor(defaultText(req.getAuthor(), "Forgex CodeGen"));
        context.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        context.setMainPkColumn(defaultText(req.getMainPkColumn(), detectPrimaryKey(mainColumns)));
        context.setSubPkColumn(req.getSubPkColumn());
        context.setSubFkColumn(req.getSubFkColumn());
        context.setMainPkJavaField(resolveJavaFieldName(mainColumns, context.getMainPkColumn(), "id"));
        context.setSubPkJavaField(resolveJavaFieldName(subColumns, context.getSubPkColumn(), "id"));
        context.setSubFkJavaField(resolveJavaFieldName(subColumns, context.getSubFkColumn(), context.getEntityNameLower() + "Id"));
        context.setMenuName(defaultText(req.getMenuName(), defaultText(mainTableMeta.getTableComment(), context.getEntityName())));
        context.setMenuIcon(defaultText(req.getMenuIcon(), "TableOutlined"));
        context.setMenuPath("/" + context.getModuleName() + "/" + context.getEntityNameLower());
        context.setParentMenuPath(defaultText(req.getParentMenuPath(), "/system/codegen"));
        context.setPermKeyPrefix(context.getModuleName() + ":" + context.getBizName());
        String tablePrefix = defaultText(req.getTableCodePrefix(), context.getModuleName());
        context.setMainTableCode(tablePrefix + "_" + context.getEntityNameLower());
        context.setSubTableCode(StringUtils.hasText(context.getSubEntityNameLower())
            ? tablePrefix + "_" + context.getSubEntityNameLower()
            : null);
        context.setI18nPrefix((context.getModuleName() + "." + context.getBizName()).toLowerCase(Locale.ROOT));
        context.setGenerateItems(CollectionUtils.isEmpty(req.getGenerateItems())
            ? List.of("backend", "frontend", "sql")
            : req.getGenerateItems());
        context.setMainColumns(mainColumns);
        context.setSubColumns(subColumns);
        context.setMainQueryColumns(filterQueryColumns(mainColumns));
        context.setMainFormColumns(filterFormColumns(mainColumns));
        context.setMainTableColumns(filterTableColumns(mainColumns));
        context.setSubQueryColumns(filterQueryColumns(subColumns));
        context.setSubFormColumns(filterFormColumns(subColumns));
        context.setSubTableColumns(filterTableColumns(subColumns));
        String subDefaultSortColumn = resolveSubDefaultSortColumn(subColumns);
        context.setSubDefaultSortColumn(subDefaultSortColumn);
        context.setSubDefaultSortJavaField(resolveJavaFieldName(subColumns, subDefaultSortColumn, "id"));
        context.getImports().addAll(collectImports(mainColumns));
        context.getImports().addAll(collectImports(subColumns));
        return context;
    }

    private List<ColumnMetaDTO> preferRequestColumns(List<ColumnMetaDTO> requestColumns, List<ColumnMetaDTO> metaColumns) {
        return CollectionUtils.isEmpty(requestColumns) ? metaColumns : requestColumns;
    }

    private List<ColumnMetaDTO> withDefaultColumnFeatures(List<ColumnMetaDTO> columns) {
        for (ColumnMetaDTO column : columns) {
            column.setIsBaseField(BASE_ENTITY_FIELDS.contains(column.getColumnName()));
            if (!StringUtils.hasText(column.getQueryType())) {
                column.setQueryType(resolveQueryType(column.getJavaType()));
            }
            if (!StringUtils.hasText(column.getQueryOperator())) {
                column.setQueryOperator("String".equals(column.getJavaType()) ? "like" : "eq");
            }
            if (!StringUtils.hasText(column.getFormType())) {
                column.setFormType(resolveFormType(column));
            }
            if (column.getRequired() == null) {
                column.setRequired(Boolean.FALSE.equals(column.getIsNullable()));
            }
            if (column.getWidth() == null) {
                column.setWidth(resolveWidth(column.getJavaType()));
            }
            if (!StringUtils.hasText(column.getAlign())) {
                column.setAlign(resolveAlign(column.getJavaType()));
            }
            if (column.getSortable() == null) {
                column.setSortable(!"String".equals(column.getJavaType()));
            }
        }
        return columns;
    }

    private void validateMainSubRelation(List<ColumnMetaDTO> mainColumns, List<ColumnMetaDTO> subColumns, CodeGenRequestDTO req) {
        ColumnMetaDTO mainPk = findColumn(mainColumns, req.getMainPkColumn());
        ColumnMetaDTO subFk = findColumn(subColumns, req.getSubFkColumn());
        ColumnMetaDTO subPk = findColumn(subColumns, req.getSubPkColumn());
        if (mainPk == null || subFk == null || subPk == null || !StringUtils.pathEquals(mainPk.getJavaType(), subFk.getJavaType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_MAIN_SUB_RELATION_INVALID);
        }
        if (findColumn(subColumns, resolveSubDefaultSortColumn(subColumns)) == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_MAIN_SUB_RELATION_INVALID);
        }
    }

    private ColumnMetaDTO findColumn(List<ColumnMetaDTO> columns, String columnName) {
        return columns.stream()
            .filter(item -> StringUtils.pathEquals(item.getColumnName(), columnName))
            .findFirst()
            .orElse(null);
    }

    private String resolveJavaFieldName(List<ColumnMetaDTO> columns, String columnName, String defaultValue) {
        ColumnMetaDTO column = findColumn(columns, columnName);
        return column == null || !StringUtils.hasText(column.getJavaFieldName()) ? defaultValue : column.getJavaFieldName();
    }

    private String detectPrimaryKey(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(column -> Boolean.TRUE.equals(column.getIsPrimaryKey()))
            .map(ColumnMetaDTO::getColumnName)
            .findFirst()
            .orElse("id");
    }

    private List<ColumnMetaDTO> filterQueryColumns(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(column -> !Boolean.TRUE.equals(column.getIsBaseField()))
            .filter(column -> !Boolean.TRUE.equals(column.getIsPrimaryKey()))
            .limit(6)
            .toList();
    }

    private List<ColumnMetaDTO> filterFormColumns(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(column -> !Boolean.TRUE.equals(column.getIsBaseField()))
            .filter(column -> !Boolean.TRUE.equals(column.getIsPrimaryKey()))
            .filter(column -> !Boolean.TRUE.equals(column.getIsAutoIncrement()))
            .toList();
    }

    private List<ColumnMetaDTO> filterTableColumns(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(column -> !Boolean.TRUE.equals(column.getIsBaseField()) || "id".equals(column.getColumnName()))
            .limit(8)
            .toList();
    }

    private String resolveSubDefaultSortColumn(List<ColumnMetaDTO> columns) {
        if (CollectionUtils.isEmpty(columns)) {
            return "id";
        }
        List<String> candidates = List.of("sort_no", "sort_num", "sort_order", "order_num", "create_time", "id");
        for (String candidate : candidates) {
            if (findColumn(columns, candidate) != null) {
                return candidate;
            }
        }
        return columns.stream()
            .filter(column -> Boolean.TRUE.equals(column.getIsPrimaryKey()))
            .map(ColumnMetaDTO::getColumnName)
            .findFirst()
            .orElse(columns.get(0).getColumnName());
    }

    private Set<String> collectImports(List<ColumnMetaDTO> columns) {
        Set<String> imports = new HashSet<>();
        for (ColumnMetaDTO column : columns) {
            if (Boolean.TRUE.equals(column.getNeedImport()) && StringUtils.hasText(column.getJavaTypeFullName())) {
                imports.add(column.getJavaTypeFullName());
            }
        }
        return imports;
    }

    private String resolveQueryType(String javaType) {
        if ("LocalDate".equals(javaType) || "LocalDateTime".equals(javaType)) {
            return "date";
        }
        return "String".equals(javaType) ? "input" : "input";
    }

    private String resolveFormType(ColumnMetaDTO column) {
        String javaType = column.getJavaType();
        if ("LocalDate".equals(javaType)) {
            return "date";
        }
        if ("LocalDateTime".equals(javaType)) {
            return "datetime";
        }
        if ("BigDecimal".equals(javaType) || "Integer".equals(javaType) || "Long".equals(javaType)
            || "Float".equals(javaType) || "Double".equals(javaType)) {
            return "number";
        }
        if ("Boolean".equals(javaType)) {
            return "select";
        }
        if (column.getCharacterMaximumLength() != null && column.getCharacterMaximumLength() > 200) {
            return "textarea";
        }
        return "input";
    }

    private Integer resolveWidth(String javaType) {
        if ("LocalDateTime".equals(javaType)) {
            return 180;
        }
        if ("LocalDate".equals(javaType)) {
            return 140;
        }
        if ("Integer".equals(javaType) || "Long".equals(javaType) || "BigDecimal".equals(javaType)) {
            return 120;
        }
        return 160;
    }

    private String resolveAlign(String javaType) {
        if ("Integer".equals(javaType) || "Long".equals(javaType) || "BigDecimal".equals(javaType)) {
            return "right";
        }
        if ("LocalDate".equals(javaType) || "LocalDateTime".equals(javaType)) {
            return "center";
        }
        return "left";
    }

    private String buildZipFileName(CodeGenContextDTO context) {
        return "codegen-" + context.getBizName() + "-" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".zip";
    }

    private String extractName(String path) {
        if (!StringUtils.hasText(path) || !path.contains("/")) {
            return path;
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String toCamelCase(String value, boolean upperFirst) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String[] parts = value.split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            String lower = parts[i].toLowerCase(Locale.ROOT);
            if (i == 0 && !upperFirst) {
                builder.append(lower);
            } else {
                builder.append(Character.toUpperCase(lower.charAt(0)));
                if (lower.length() > 1) {
                    builder.append(lower.substring(1));
                }
            }
        }
        return builder.toString();
    }

    private String toLowerCamel(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }
}
