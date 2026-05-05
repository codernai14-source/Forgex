package com.forgex.basic.currency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.currency.domain.entity.MdmCurrency;
import com.forgex.basic.currency.domain.param.CurrencyPageParam;

import java.util.List;

/**
 * 币种服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface ICurrencyService extends IService<MdmCurrency> {
    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    Page<MdmCurrency> page(CurrencyPageParam param);
    /**
     * 查询数据列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    List<MdmCurrency> list(CurrencyPageParam param);
    /**
     * 查询数据详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    MdmCurrency detail(Long id);
    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    Long create(MdmCurrency param);
    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    Boolean update(MdmCurrency param);
    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);
    /**
     * 设置本位币。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean setBase(Long id);
    /**
     * 启用数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean enable(Long id);
    /**
     * 禁用数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean disable(Long id);
}
