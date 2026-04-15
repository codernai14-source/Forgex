package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.label.domain.param.PrintExceptionQueryParam;
import com.forgex.basic.label.domain.vo.PlaceholderVO;
import com.forgex.basic.label.domain.vo.PrintExceptionVO;
import com.forgex.basic.label.domain.vo.TemplateValidateResultVO;

import java.util.List;

/**
 * 标签通用工具 Service
 */
public interface LabelCommonService {

    /**
     * 获取标准占位符列表
     *
     * @param tenantId 租户 ID
     * @return 占位符列表
     */
    List<PlaceholderVO> getStandardPlaceholders(Long tenantId);

    /**
     * 校验模板 JSON 合法性
     *
     * @param templateContent 模板内容
     * @param tenantId 租户 ID
     * @return 校验结果
     */
    TemplateValidateResultVO validateTemplateJson(String templateContent, Long tenantId);

    /**
     * 分页查询打印异常日志
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 异常日志分页数据
     */
    IPage<PrintExceptionVO> pageExceptions(PrintExceptionQueryParam param, Long tenantId);
}
