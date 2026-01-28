package com.forgex.sys.service.impl;

import com.forgex.sys.domain.dto.CodeGenContextDTO;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.service.CodeGenService;
import com.forgex.sys.service.DbMetaService;
import lombok.RequiredArgsConstructor;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现类（增强版）
 * <p>
 * 使用 Beetl 模板引擎生成完整的代码，包括：
 * - 后端代码（Entity、Mapper、Service、Controller、Param）
 * - 前端代码（Vue 页面、API）
 * - SQL 脚本（菜单权限、表格配置、多语言）
 * </p>
 * 
 * @author Forgex Team
 * @version 2.0.0
 */
@Service
@RequiredArgsConstructor
public class CodeGenServiceImpl implements CodeGenService {

    private final GroupTemplate groupTemplate;
    private final DbMetaService dbMetaService;
    
    /**
     * BaseEntity 中的字段，生成时需要排除
     */
    private static final Set<String> BASE_ENTITY_FIELDS = new HashSet<>(Arrays.asList(
        "id", "tenant_id", "create_time", "create_by", "update_time", "update_by", "deleted"
    ));

    @Override
    public Map<String, String> preview(CodeGenRequestDTO req) {
        Map<String, String> files = new LinkedHashMap<>();
        
        // 参数校验
        if (req == null || !StringUtils.hasText(req.getTableName())) {
            return files;
        }
        
        // 构建代码生成上下文
        CodeGenContextDTO context = buildContext(req);
        
        // 生成后端代码
        files.putAll(generateBackendCode(context));
        
        // 生成前端代码
        files.putAll(generateFrontendCode(context));
        
        // 生成 SQL 脚本
        files.putAll(generateSqlScripts(context));
        
        // 生成 README
        files.put("README.md", generateReadme(context));
        
        return files;
    }

    @Override
    public byte[] generateZip(CodeGenRequestDTO req) {
        Map<String, String> files = preview(req);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            for (Map.Entry<String, String> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                
                byte[] bytes = (entry.getValue() == null ? "" : entry.getValue())
                    .getBytes(StandardCharsets.UTF_8);
                zos.write(bytes);
                
                zos.closeEntry();
            }
            
            zos.finish();
            return baos.toByteArray();
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    /**
     * 构建代码生成上下文
     */
    private CodeGenContextDTO buildContext(CodeGenRequestDTO req) {
        CodeGenContextDTO context = new CodeGenContextDTO();
        
        // 查询表元数据
        TableMetaDTO tableMeta = dbMetaService.getTableMeta(req.getTableName());
        List<ColumnMetaDTO> columns = dbMetaService.listColumns(req.getTableName());
        
        // 基础信息
        context.setTableName(req.getTableName());
        context.setTableComment(tableMeta.getTableComment());
        context.setEntityName(StringUtils.hasText(req.getEntityName()) ? 
            req.getEntityName() : toCamelCase(req.getTableName(), true));
        context.setEntityNameLower(toLowerCamelCase(context.getEntityName()));
        context.setModuleName(StringUtils.hasText(req.getModuleName()) ? 
            req.getModuleName() : "sys");
        context.setBasePackage(StringUtils.hasText(req.getBasePackage()) ? 
            req.getBasePackage() : "com.forgex." + context.getModuleName());
        context.setMode(StringUtils.hasText(req.getMode()) ? 
            req.getMode().toUpperCase() : "SINGLE");
        context.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // 处理列信息
        columns.forEach(col -> {
            col.setIsBaseField(BASE_ENTITY_FIELDS.contains(col.getColumnName()));
        });
        
        context.setColumns(columns);
        context.setQueryColumns(filterQueryColumns(columns));
        context.setSaveColumns(filterSaveColumns(columns));
        context.setTableColumns(filterTableColumns(columns));
        
        // 菜单权限信息
        context.setMenuPath(context.getEntityNameLower());
        context.setMenuNameCn(tableMeta.getTableComment());
        context.setMenuNameEn(context.getEntityName());
        context.setMenuIcon("TableOutlined");
        context.setMenuOrderNum(100);
        context.setComponentKey(context.getEntityName());
        context.setPermKeyPrefix(context.getModuleName() + ":" + context.getEntityNameLower());
        
        // 模块信息
        context.setModuleNameCn(context.getModuleName() + "模块");
        context.setModuleNameEn(context.getModuleName().toUpperCase());
        context.setModuleIcon("AppstoreOutlined");
        context.setModuleOrderNum(10);
        
        // 表格配置信息
        context.setTableCode(context.getModuleName() + "_" + context.getEntityNameLower());
        context.setTableNameCn(tableMeta.getTableComment());
        context.setTableNameEn(context.getEntityName());
        
        // 多语言信息
        context.setI18nPrefix(context.getModuleName().toUpperCase() + "_" + 
            toUnderscoreCase(context.getEntityName()).toUpperCase());
        
        return context;
    }

    /**
     * 生成后端代码
     */
    private Map<String, String> generateBackendCode(CodeGenContextDTO context) {
        Map<String, String> files = new LinkedHashMap<>();
        String basePath = "backend/" + context.getBasePackage().replace('.', '/');
        
        // Entity
        files.put(basePath + "/domain/entity/" + context.getEntityName() + ".java",
            renderTemplate("Entity.java.btl", context));
        
        // Mapper
        files.put(basePath + "/mapper/" + context.getEntityName() + "Mapper.java",
            renderTemplate("Mapper.java.btl", context));
        
        // Service
        files.put(basePath + "/service/" + context.getEntityName() + "Service.java",
            renderTemplate("Service.java.btl", context));
        
        // ServiceImpl
        files.put(basePath + "/service/impl/" + context.getEntityName() + "ServiceImpl.java",
            renderTemplate("ServiceImpl.java.btl", context));
        
        // Controller
        files.put(basePath + "/controller/" + context.getEntityName() + "Controller.java",
            renderTemplate("Controller.java.btl", context));
        
        // PageParam
        files.put(basePath + "/domain/param/" + context.getEntityName() + "PageParam.java",
            renderTemplate("PageParam.java.btl", context));
        
        // SaveParam
        files.put(basePath + "/domain/param/" + context.getEntityName() + "SaveParam.java",
            renderTemplate("SaveParam.java.btl", context));
        
        return files;
    }

    /**
     * 生成前端代码
     */
    private Map<String, String> generateFrontendCode(CodeGenContextDTO context) {
        Map<String, String> files = new LinkedHashMap<>();
        String basePath = "frontend/src";
        
        // Vue 页面
        files.put(basePath + "/views/" + context.getModuleName() + "/" + 
            context.getEntityNameLower() + "/index.vue",
            renderTemplate("vue/index.vue.btl", context));
        
        // API
        files.put(basePath + "/api/" + context.getModuleName() + "/" + 
            context.getEntityNameLower() + ".ts",
            renderTemplate("vue/api.ts.btl", context));
        
        return files;
    }

    /**
     * 生成 SQL 脚本
     */
    private Map<String, String> generateSqlScripts(CodeGenContextDTO context) {
        Map<String, String> files = new LinkedHashMap<>();
        String basePath = "sql";
        
        // 菜单权限 SQL
        files.put(basePath + "/01_menu_permission.sql",
            renderTemplate("sql/menu_permission.sql.btl", context));
        
        // 表格配置 SQL
        files.put(basePath + "/02_table_config.sql",
            renderTemplate("sql/table_config.sql.btl", context));
        
        // 多语言 SQL
        files.put(basePath + "/03_i18n_message.sql",
            renderTemplate("sql/i18n_message.sql.btl", context));
        
        return files;
    }

    /**
     * 生成 README
     */
    private String generateReadme(CodeGenContextDTO context) {
        return "# " + context.getTableComment() + " 代码生成\n\n" +
               "## 基本信息\n\n" +
               "- 表名：" + context.getTableName() + "\n" +
               "- 实体类：" + context.getEntityName() + "\n" +
               "- 模块：" + context.getModuleName() + "\n" +
               "- 包名：" + context.getBasePackage() + "\n" +
               "- 生成时间：" + context.getDate() + "\n\n" +
               "## 文件说明\n\n" +
               "### 后端代码\n\n" +
               "- Entity：实体类\n" +
               "- Mapper：数据访问层\n" +
               "- Service：业务逻辑层\n" +
               "- Controller：控制器层\n" +
               "- Param：参数类\n\n" +
               "### 前端代码\n\n" +
               "- index.vue：页面组件\n" +
               "- api.ts：API 接口\n\n" +
               "### SQL 脚本\n\n" +
               "- 01_menu_permission.sql：菜单和权限初始化\n" +
               "- 02_table_config.sql：表格配置初始化\n" +
               "- 03_i18n_message.sql：多语言配置初始化\n\n" +
               "## 使用说明\n\n" +
               "1. 将后端代码复制到对应模块\n" +
               "2. 将前端代码复制到前端项目\n" +
               "3. 执行 SQL 脚本初始化数据\n" +
               "4. 重启后端服务\n" +
               "5. 刷新前端页面\n";
    }

    /**
     * 渲染模板
     */
    private String renderTemplate(String templateName, CodeGenContextDTO context) {
        try {
            Template template = groupTemplate.getTemplate(templateName);
            template.binding("tableName", context.getTableName());
            template.binding("tableComment", context.getTableComment());
            template.binding("entityName", context.getEntityName());
            template.binding("entityNameLower", context.getEntityNameLower());
            template.binding("moduleName", context.getModuleName());
            template.binding("basePackage", context.getBasePackage());
            template.binding("mode", context.getMode());
            template.binding("date", context.getDate());
            template.binding("columns", context.getColumns());
            template.binding("queryColumns", context.getQueryColumns());
            template.binding("saveColumns", context.getSaveColumns());
            template.binding("tableColumns", context.getTableColumns());
            template.binding("menuPath", context.getMenuPath());
            template.binding("menuNameCn", context.getMenuNameCn());
            template.binding("menuNameEn", context.getMenuNameEn());
            template.binding("menuIcon", context.getMenuIcon());
            template.binding("menuOrderNum", context.getMenuOrderNum());
            template.binding("componentKey", context.getComponentKey());
            template.binding("permKeyPrefix", context.getPermKeyPrefix());
            template.binding("moduleNameCn", context.getModuleNameCn());
            template.binding("moduleNameEn", context.getModuleNameEn());
            template.binding("moduleIcon", context.getModuleIcon());
            template.binding("moduleOrderNum", context.getModuleOrderNum());
            template.binding("tableCode", context.getTableCode());
            template.binding("tableNameCn", context.getTableNameCn());
            template.binding("tableNameEn", context.getTableNameEn());
            template.binding("i18nPrefix", context.getI18nPrefix());
            
            return template.render();
        } catch (Exception e) {
            return "// 模板渲染失败：" + e.getMessage();
        }
    }

    /**
     * 过滤查询列
     */
    private List<ColumnMetaDTO> filterQueryColumns(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(col -> !col.getIsBaseField())
            .filter(col -> !col.getIsPrimaryKey())
            .limit(5) // 最多5个查询条件
            .peek(col -> {
                // 设置查询类型
                if ("String".equals(col.getJavaType())) {
                    col.setQueryType("input");
                    col.setQueryOperator("like");
                } else if ("Integer".equals(col.getJavaType()) || "Long".equals(col.getJavaType())) {
                    col.setQueryType("input");
                    col.setQueryOperator("eq");
                } else if ("LocalDate".equals(col.getJavaType()) || "LocalDateTime".equals(col.getJavaType())) {
                    col.setQueryType("date");
                    col.setQueryOperator("eq");
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * 过滤保存列
     */
    private List<ColumnMetaDTO> filterSaveColumns(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(col -> !col.getIsBaseField())
            .filter(col -> !col.getIsPrimaryKey())
            .filter(col -> !col.getIsAutoIncrement())
            .peek(col -> {
                // 设置表单类型
                if ("String".equals(col.getJavaType())) {
                    if (col.getCharacterMaximumLength() != null && col.getCharacterMaximumLength() > 200) {
                        col.setFormType("textarea");
                    } else {
                        col.setFormType("input");
                    }
                } else if ("Integer".equals(col.getJavaType()) || "Long".equals(col.getJavaType()) || 
                           "BigDecimal".equals(col.getJavaType())) {
                    col.setFormType("number");
                } else if ("LocalDate".equals(col.getJavaType())) {
                    col.setFormType("date");
                } else if ("LocalDateTime".equals(col.getJavaType())) {
                    col.setFormType("datetime");
                } else if ("Boolean".equals(col.getJavaType())) {
                    col.setFormType("select");
                } else {
                    col.setFormType("input");
                }
                
                // 设置是否必填
                col.setRequired(!col.getIsNullable());
            })
            .collect(Collectors.toList());
    }

    /**
     * 过滤表格列
     */
    private List<ColumnMetaDTO> filterTableColumns(List<ColumnMetaDTO> columns) {
        return columns.stream()
            .filter(col -> !col.getIsBaseField() || "id".equals(col.getColumnName()))
            .peek(col -> {
                // 设置列宽
                if ("String".equals(col.getJavaType())) {
                    col.setWidth(150);
                } else if ("Integer".equals(col.getJavaType()) || "Long".equals(col.getJavaType())) {
                    col.setWidth(100);
                } else if ("LocalDate".equals(col.getJavaType())) {
                    col.setWidth(120);
                } else if ("LocalDateTime".equals(col.getJavaType())) {
                    col.setWidth(180);
                } else {
                    col.setWidth(120);
                }
                
                // 设置对齐方式
                if ("Integer".equals(col.getJavaType()) || "Long".equals(col.getJavaType()) || 
                    "BigDecimal".equals(col.getJavaType())) {
                    col.setAlign("right");
                } else if ("LocalDate".equals(col.getJavaType()) || "LocalDateTime".equals(col.getJavaType())) {
                    col.setAlign("center");
                } else {
                    col.setAlign("left");
                }
                
                // 设置是否可排序
                col.setSortable(!"String".equals(col.getJavaType()));
            })
            .collect(Collectors.toList());
    }

    /**
     * 转换为驼峰命名
     */
    private String toCamelCase(String str, boolean upperFirst) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        
        String[] parts = str.split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            
            String lower = part.toLowerCase();
            if (i == 0 && !upperFirst) {
                sb.append(lower);
            } else {
                sb.append(Character.toUpperCase(lower.charAt(0)));
                if (lower.length() > 1) {
                    sb.append(lower.substring(1));
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * 转换为小写驼峰命名
     */
    private String toLowerCamelCase(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 转换为下划线命名
     */
    private String toUnderscoreCase(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
}
