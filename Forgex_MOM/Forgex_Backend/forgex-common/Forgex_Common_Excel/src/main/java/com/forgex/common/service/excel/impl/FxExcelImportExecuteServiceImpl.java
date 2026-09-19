package com.forgex.common.service.excel.impl;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.ExcelPromptEnum;
import com.forgex.common.enums.FxExcelImportMode;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.service.excel.FxExcelImportExecuteService;
import com.forgex.common.service.excel.FxExcelImportHandler;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Excel 公共导入执行服务实现。
 * <p>
 * 按导入配置中的处理器 Bean 名称查找业务处理器，并把导入模式、导入配置和导入数据交给业务处理器执行。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Service
@RequiredArgsConstructor
public class FxExcelImportExecuteServiceImpl implements FxExcelImportExecuteService {

    private final ApplicationContext applicationContext;

    /**
     * 处理execute。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Override
    public FxExcelImportResultDTO execute(FxExcelImportExecuteParam param) {
        if (param == null || param.getImportConfig() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_TEMPLATE_CONFIG_MISSING);
        }
        FxExcelImportMode.parse(param.getImportMode());
        if (CollectionUtils.isEmpty(param.getImportData())
                || param.getImportData().values().stream().allMatch(CollectionUtils::isEmpty)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_DATA_EMPTY);
        }

        String handlerBeanName = param.getImportConfig().getHandlerBeanName();
        if (!StringUtils.hasText(handlerBeanName)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_HANDLER_NOT_CONFIGURED);
        }
        if (!applicationContext.containsBean(handlerBeanName)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_HANDLER_NOT_FOUND, handlerBeanName);
        }

        Object handlerBean = applicationContext.getBean(handlerBeanName);
        if (!(handlerBean instanceof FxExcelImportHandler handler)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_HANDLER_TYPE_INVALID, handlerBeanName);
        }
        FxExcelImportResultDTO result = handler.handle(param);
        return result == null ? new FxExcelImportResultDTO() : result;
    }
}
