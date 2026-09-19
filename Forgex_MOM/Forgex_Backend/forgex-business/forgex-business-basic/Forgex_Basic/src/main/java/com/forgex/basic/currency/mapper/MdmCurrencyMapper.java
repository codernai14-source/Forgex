package com.forgex.basic.currency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.currency.domain.entity.MdmCurrency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 币种主数据 Mapper 接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
public interface MdmCurrencyMapper extends BaseMapper<MdmCurrency> {
}
