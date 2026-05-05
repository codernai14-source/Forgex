package com.forgex.basic.currency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateType;
import com.forgex.basic.currency.domain.param.RateTypePageParam;

import java.util.List;

/**
 * 汇率类型服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IRateTypeService extends IService<MdmExchangeRateType> {
    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    Page<MdmExchangeRateType> page(RateTypePageParam param);
    /**
     * 查询数据列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    List<MdmExchangeRateType> list(RateTypePageParam param);
    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    Long create(MdmExchangeRateType param);
    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    Boolean update(MdmExchangeRateType param);
    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);
    /**
     * 执行汇率类型的set默认操作。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean setDefault(Long id);
}
