package com.forgex.sys.controller;

import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.ExcelPromptEnum;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.service.excel.FxExcelImportExecuteService;
import com.forgex.common.service.i18n.I18nMessageService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.service.ExcelExportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExcelConfigControllerTest {

    private ExcelConfigService excelConfigService;
    private FxExcelImportExecuteService excelImportExecuteService;
    private PermKeyService permKeyService;
    private ExcelConfigController controller;

    @BeforeEach
    void setUp() {
        excelConfigService = mock(ExcelConfigService.class);
        excelImportExecuteService = mock(FxExcelImportExecuteService.class);
        permKeyService = mock(PermKeyService.class);
        controller = new ExcelConfigController(
            excelConfigService,
            mock(ExcelFileService.class),
            excelImportExecuteService,
            permKeyService,
            mock(I18nMessageService.class),
            mock(ExcelExportService.class)
        );
        UserContext.set(1L);
        TenantContext.set(100L);
        when(permKeyService.hasAllPerms(eq(1L), eq(100L), eq(Set.of("sys:user:import")))).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        TenantContext.clear();
    }

    @Test
    void executeImportShouldReturnFailureWhenResultContainsFailedRows() {
        FxExcelImportConfigDTO config = new FxExcelImportConfigDTO();
        config.setImportPermission("sys:user:import");
        when(excelConfigService.getImportConfigByCode("sys_user")).thenReturn(config);

        FxExcelImportResultDTO result = new FxExcelImportResultDTO();
        result.setTotalCount(1);
        result.addError("account");
        when(excelImportExecuteService.execute(any(FxExcelImportExecuteParam.class))).thenReturn(result);

        FxExcelImportExecuteParam param = new FxExcelImportExecuteParam();
        param.setTableCode("sys_user");

        R<FxExcelImportResultDTO> response = controller.executeImport(param);

        assertEquals(StatusCode.BUSINESS_ERROR, response.getCode());
        assertEquals(ExcelPromptEnum.EXCEL_IMPORT_FAILED, response.getMessageCode());
        assertSame(result, response.getData());
    }
}
