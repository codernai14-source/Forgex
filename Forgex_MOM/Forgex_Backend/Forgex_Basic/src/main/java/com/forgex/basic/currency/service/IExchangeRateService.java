package com.forgex.basic.currency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.currency.domain.entity.MdmCurrencyExchangeRate;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateLog;
import com.forgex.basic.currency.domain.param.ExchangeRateApprovalStartParam;
import com.forgex.basic.currency.domain.param.ExchangeRateCurrentParam;
import com.forgex.basic.currency.domain.param.ExchangeRatePageParam;
import com.forgex.basic.currency.domain.param.ExchangeRateWorkflowCallbackParam;

/**
 * 汇率服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IExchangeRateService extends IService<MdmCurrencyExchangeRate> {
    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    Page<MdmCurrencyExchangeRate> page(ExchangeRatePageParam param);
    /**
     * 查询数据详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    MdmCurrencyExchangeRate detail(Long id);
    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    Long create(MdmCurrencyExchangeRate param);
    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    Boolean update(MdmCurrencyExchangeRate param);
    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);
    /**
     * 发起审批流程。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    Long startApproval(ExchangeRateApprovalStartParam param);
    /**
     * 处理工作流回调。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    Boolean handleWorkflowCallback(ExchangeRateWorkflowCallbackParam param);
    /**
     * 执行汇率的当前操作。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    MdmCurrencyExchangeRate current(ExchangeRateCurrentParam param);
    /**
     * 执行汇率的日志分页操作。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    Page<MdmExchangeRateLog> logPage(ExchangeRatePageParam param);
}
