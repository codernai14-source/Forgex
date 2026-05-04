package com.forgex.sys.service.impl;

import cn.hutool.json.JSONUtil;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.dto.CodegenDatasourceDTO;
import com.forgex.sys.domain.entity.SysCodegenConfig;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.param.CodegenConfigSaveParam;
import com.forgex.sys.mapper.SysCodegenConfigMapper;
import com.forgex.sys.service.ICodegenDatasourceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * codegen配置serviceimpltest实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class CodegenConfigServiceImplTest {

    @Test
    void saveAndBuildRequestShouldRoundTripTreeAndAndroidFields() {
        SysCodegenConfigMapper mapper = mock(SysCodegenConfigMapper.class);
        ICodegenDatasourceService datasourceService = mock(ICodegenDatasourceService.class);

        SysCodegenDatasource datasource = new SysCodegenDatasource();
        datasource.setId(1L);
        datasource.setDatasourceCode("forgex_admin");
        when(datasourceService.requireEntity(1L)).thenReturn(datasource);

        ArgumentCaptor<SysCodegenConfig> captor = ArgumentCaptor.forClass(SysCodegenConfig.class);
        doAnswer(invocation -> {
            SysCodegenConfig entity = invocation.getArgument(0);
            entity.setId(99L);
            return 1;
        }).when(mapper).insert(any(SysCodegenConfig.class));
        doAnswer(invocation -> 1).when(mapper).updateById(any(SysCodegenConfig.class));
        when(datasourceService.getDatasourceById(eq(1L))).thenReturn(new CodegenDatasourceDTO());

        CodegenConfigServiceImpl service = new CodegenConfigServiceImpl(mapper, datasourceService);

        CodegenConfigSaveParam param = new CodegenConfigSaveParam();
        param.setDatasourceId(1L);
        param.setSchemaName("forgex_admin");
        param.setPageType("TREE_DOUBLE");
        param.setMainTableName("biz_product");
        param.setTreeTableName("biz_category");
        param.setMainPkColumn("id");
        param.setTreePkColumn("id");
        param.setTreeParentColumn("parent_id");
        param.setTreeLabelColumn("name");
        param.setTreeSortColumn("order_num");
        param.setTreeFilterColumn("category_id");
        param.setModuleName("basic");
        param.setBizName("bizProduct");
        param.setEntityName("BizProduct");
        param.setTreeEntityName("BizTree");
        param.setPackageName("com.forgex.basic");
        param.setAuthor("test");
        param.setMenuName("产品");
        param.setAndroidFeatureKey("categoryBiz");
        param.setGenerateItems(List.of("backend", "frontend", "sql", "android"));

        Long id = service.saveConfig(param);
        assertEquals(99L, id);

        org.mockito.Mockito.verify(mapper).insert(captor.capture());
        SysCodegenConfig saved = captor.getValue();
        assertNotNull(saved.getConfigJson());
        assertTrue(saved.getConfigJson().contains("\"treeFilterColumn\":\"category_id\""));
        assertTrue(saved.getConfigJson().contains("\"androidFeatureKey\":\"categoryBiz\""));

        when(mapper.selectById(99L)).thenReturn(saved);
        CodeGenRequestDTO request = service.buildRequest(99L);
        assertEquals("TREE_DOUBLE", request.getPageType());
        assertEquals("biz_category", request.getTreeTableName());
        assertEquals("category_id", request.getTreeFilterColumn());
        assertEquals("categoryBiz", request.getAndroidFeatureKey());
        assertEquals(List.of("backend", "frontend", "sql", "android"), request.getGenerateItems());
    }
}
