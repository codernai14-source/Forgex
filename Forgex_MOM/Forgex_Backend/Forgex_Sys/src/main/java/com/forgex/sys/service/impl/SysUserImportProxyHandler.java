package com.forgex.sys.service.impl;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.service.excel.FxExcelImportHandler;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户导入代理处理器。
 * <p>
 * 以稳定 Bean 名称暴露给公共导入配置，实际用户写库逻辑委托给用户服务。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Service("sysUserImportHandler")
@RequiredArgsConstructor
public class SysUserImportProxyHandler implements FxExcelImportHandler {

    private final ISysUserService userService;

    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        return userService.executeCommonImport(param);
    }
}
