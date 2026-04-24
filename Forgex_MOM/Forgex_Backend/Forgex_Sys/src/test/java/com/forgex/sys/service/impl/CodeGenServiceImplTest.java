package com.forgex.sys.service.impl;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.dto.CodegenDatasourceDTO;
import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.vo.CodegenPreviewVO;
import com.forgex.sys.service.DbMetaService;
import com.forgex.sys.service.ICodegenConfigService;
import com.forgex.sys.service.ICodegenDatasourceService;
import com.forgex.sys.service.codegen.CodeGenStrategyFactory;
import com.forgex.sys.service.codegen.MasterDetailCodeGenStrategy;
import com.forgex.sys.service.codegen.SingleTableCodeGenStrategy;
import com.forgex.sys.service.codegen.TreeDoubleCodeGenStrategy;
import com.forgex.sys.service.codegen.TreeSingleCodeGenStrategy;
import com.forgex.sys.service.codegen.template.CodegenTemplateRenderer;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CodeGenServiceImplTest {

    private ICodegenDatasourceService datasourceService;
    private ICodegenConfigService configService;
    private DbMetaService dbMetaService;
    private CodeGenServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        datasourceService = Mockito.mock(ICodegenDatasourceService.class);
        configService = Mockito.mock(ICodegenConfigService.class);
        dbMetaService = Mockito.mock(DbMetaService.class);

        GroupTemplate groupTemplate = new GroupTemplate(
            new ClasspathResourceLoader("templates/codegen/"),
            org.beetl.core.Configuration.defaultConfiguration()
        );
        CodegenTemplateRenderer renderer = new CodegenTemplateRenderer(groupTemplate);
        CodeGenStrategyFactory strategyFactory = new CodeGenStrategyFactory(List.of(
            new SingleTableCodeGenStrategy(renderer),
            new MasterDetailCodeGenStrategy(renderer),
            new TreeSingleCodeGenStrategy(renderer),
            new TreeDoubleCodeGenStrategy(renderer)
        ));
        service = new CodeGenServiceImpl(datasourceService, configService, dbMetaService, strategyFactory);

        SysCodegenDatasource datasource = new SysCodegenDatasource();
        datasource.setId(1L);
        datasource.setDatasourceCode("forgex_admin");
        datasource.setDatasourceName("Forgex Admin");
        datasource.setSchemaName("forgex_admin");
        when(datasourceService.requireEntity(1L)).thenReturn(datasource);
        when(datasourceService.getDatasourceById(anyLong())).thenReturn(new CodegenDatasourceDTO());

        mockMainTable("biz_category");
        mockTreeTable("biz_category");
        mockSubTable("biz_category_detail");
    }

    @Test
    void previewSingleShouldContainAndroidFiles() {
        CodegenPreviewVO preview = service.preview(buildSingleRequest());
        assertNotNull(preview);
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().startsWith("android/basic/")));
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().equals("frontend/src/views/basic/bizCategory/index.vue")));
    }

    @Test
    void previewTreeSingleShouldContainTreeFiles() {
        CodegenPreviewVO preview = service.preview(buildTreeSingleRequest());
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().contains("android/category/")));
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().endsWith("/controller/BizCategoryController.java")));
        assertFalse(preview.getFiles().stream().anyMatch(file -> file.getContent().contains("模板渲染失败")));
    }

    @Test
    void previewTreeDoubleShouldContainTreeEntityAndAndroidFiles() {
        CodegenPreviewVO preview = service.preview(buildTreeDoubleRequest());
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().endsWith("/domain/entity/BizProduct.java")));
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().endsWith("/domain/entity/BizTree.java")));
        assertTrue(preview.getFiles().stream().anyMatch(file -> file.getPath().startsWith("android/categoryBiz/")));
    }

    @Test
    void generateZipShouldReturnBytes() {
        byte[] zipBytes = service.generateZip(buildTreeDoubleRequest());
        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);
    }

    @Test
    void treeSingleShouldRejectNonSelfReference() {
        CodeGenRequestDTO request = buildTreeSingleRequest();
        request.setTreeTableName("sys_dept");
        assertThrows(I18nBusinessException.class, () -> service.preview(request));
    }

    @Test
    void treeDoubleShouldRejectTypeMismatch() {
        CodeGenRequestDTO request = buildTreeDoubleRequest();
        when(dbMetaService.listColumns(Mockito.any(), eq("forgex_admin"), eq("biz_product")))
            .thenReturn(mainColumnsWithStringTreeFilter());
        assertThrows(I18nBusinessException.class, () -> service.preview(request));
    }

    private void mockMainTable(String tableName) {
        TableMetaDTO meta = new TableMetaDTO();
        meta.setTableName(tableName);
        meta.setTableComment("Category");
        when(dbMetaService.getTableMeta(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(meta);
        if ("biz_product".equals(tableName)) {
            when(dbMetaService.listColumns(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(productColumns());
            return;
        }
        when(dbMetaService.listColumns(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(mainColumns());
    }

    private void mockTreeTable(String tableName) {
        TableMetaDTO meta = new TableMetaDTO();
        meta.setTableName(tableName);
        meta.setTableComment("Category Tree");
        when(dbMetaService.getTableMeta(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(meta);
        when(dbMetaService.listColumns(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(treeColumns());
    }

    private void mockSubTable(String tableName) {
        TableMetaDTO meta = new TableMetaDTO();
        meta.setTableName(tableName);
        meta.setTableComment("Category Detail");
        when(dbMetaService.getTableMeta(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(meta);
        when(dbMetaService.listColumns(Mockito.any(), eq("forgex_admin"), eq(tableName))).thenReturn(subColumns());
    }

    private CodeGenRequestDTO buildSingleRequest() {
        CodeGenRequestDTO request = baseRequest("SINGLE", "biz_category");
        request.setGenerateItems(List.of("backend", "frontend", "sql", "android"));
        request.setAndroidFeatureKey("basic");
        return request;
    }

    private CodeGenRequestDTO buildTreeSingleRequest() {
        CodeGenRequestDTO request = baseRequest("TREE_SINGLE", "biz_category");
        request.setTreeTableName("biz_category");
        request.setTreeEntityName("BizCategory");
        request.setTreePkColumn("id");
        request.setTreeParentColumn("parent_id");
        request.setTreeLabelColumn("name");
        request.setTreeSortColumn("order_num");
        request.setGenerateItems(List.of("backend", "frontend", "sql", "android"));
        request.setAndroidFeatureKey("category");
        return request;
    }

    private CodeGenRequestDTO buildTreeDoubleRequest() {
        mockMainTable("biz_product");
        TableMetaDTO treeMeta = new TableMetaDTO();
        treeMeta.setTableName("biz_category");
        treeMeta.setTableComment("Category Tree");
        when(dbMetaService.getTableMeta(Mockito.any(), eq("forgex_admin"), eq("biz_category"))).thenReturn(treeMeta);
        when(dbMetaService.listColumns(Mockito.any(), eq("forgex_admin"), eq("biz_category"))).thenReturn(treeColumns());

        CodeGenRequestDTO request = baseRequest("TREE_DOUBLE", "biz_product");
        request.setEntityName("BizProduct");
        request.setBizName("bizProduct");
        request.setMenuName("Product");
        request.setTreeTableName("biz_category");
        request.setTreeEntityName("BizTree");
        request.setTreePkColumn("id");
        request.setTreeParentColumn("parent_id");
        request.setTreeLabelColumn("name");
        request.setTreeSortColumn("order_num");
        request.setTreeFilterColumn("category_id");
        request.setGenerateItems(List.of("backend", "frontend", "sql", "android"));
        request.setAndroidFeatureKey("categoryBiz");
        return request;
    }

    private CodeGenRequestDTO baseRequest(String pageType, String mainTableName) {
        CodeGenRequestDTO request = new CodeGenRequestDTO();
        request.setDatasourceId(1L);
        request.setSchemaName("forgex_admin");
        request.setPageType(pageType);
        request.setMainTableName(mainTableName);
        request.setMainPkColumn("id");
        request.setModuleName("basic");
        request.setBizName("bizCategory");
        request.setEntityName("BizCategory");
        request.setPackageName("com.forgex.basic");
        request.setAuthor("test");
        request.setMenuName("Category");
        request.setMenuIcon("TableOutlined");
        request.setParentMenuPath("/basic");
        request.setTableCodePrefix("basic");
        return request;
    }

    private List<ColumnMetaDTO> mainColumns() {
        return List.of(
            column("id", "id", "Long", true, false, false),
            column("parent_id", "parentId", "Long", false, false, true),
            column("name", "name", "String", false, false, false),
            column("order_num", "orderNum", "Integer", false, false, true)
        );
    }

    private List<ColumnMetaDTO> treeColumns() {
        return List.of(
            column("id", "id", "Long", true, false, false),
            column("parent_id", "parentId", "Long", false, false, true),
            column("name", "name", "String", false, false, false),
            column("order_num", "orderNum", "Integer", false, false, true)
        );
    }

    private List<ColumnMetaDTO> subColumns() {
        return List.of(
            column("id", "id", "Long", true, false, false),
            column("biz_category_id", "bizCategoryId", "Long", false, false, false),
            column("detail_name", "detailName", "String", false, false, true),
            column("sort_no", "sortNo", "Integer", false, false, true)
        );
    }

    private List<ColumnMetaDTO> productColumns() {
        return List.of(
            column("id", "id", "Long", true, false, false),
            column("category_id", "categoryId", "Long", false, false, true),
            column("product_name", "productName", "String", false, false, false),
            column("order_num", "orderNum", "Integer", false, false, true)
        );
    }

    private List<ColumnMetaDTO> mainColumnsWithStringTreeFilter() {
        return List.of(
            column("id", "id", "Long", true, false, false),
            column("category_id", "categoryId", "String", false, false, true),
            column("product_name", "productName", "String", false, false, false)
        );
    }

    private ColumnMetaDTO column(String columnName, String javaFieldName, String javaType,
                                 boolean primaryKey, boolean autoIncrement, boolean nullable) {
        ColumnMetaDTO column = new ColumnMetaDTO();
        column.setColumnName(columnName);
        column.setColumnComment(columnName);
        column.setJavaFieldName(javaFieldName);
        column.setJavaType(javaType);
        column.setIsPrimaryKey(primaryKey);
        column.setIsAutoIncrement(autoIncrement);
        column.setIsNullable(nullable);
        column.setNeedImport(false);
        column.setQueryable(!primaryKey);
        column.setTableShow(true);
        column.setFormShow(!primaryKey);
        column.setRequired(!nullable);
        column.setWidth(160);
        column.setAlign("left");
        column.setQueryType("input");
        column.setQueryOperator("String".equals(javaType) ? "like" : "eq");
        column.setFormType("String".equals(javaType) ? "input" : "number");
        column.setSortDirection("DESC");
        return column;
    }
}
