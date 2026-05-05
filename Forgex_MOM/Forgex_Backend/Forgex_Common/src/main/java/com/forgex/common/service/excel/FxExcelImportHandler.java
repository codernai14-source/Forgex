package com.forgex.common.service.excel;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;

/**
 * Excel 公共导入业务处理器。
 * <p>
 * 各业务模块实现该接口，并用导入配置主表中的处理器 Bean 名称进行绑定。
 * 公共导入接口只负责配置校验、权限校验和处理器分发，具体唯一键判断、校验和写库逻辑由业务处理器完成。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
public interface FxExcelImportHandler {

    /**
     * 执行导入。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO handle(FxExcelImportExecuteParam param);
}
