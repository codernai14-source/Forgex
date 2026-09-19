package com.forgex.basic.currency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 汇率日志 Mapper 接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
public interface MdmExchangeRateLogMapper extends BaseMapper<MdmExchangeRateLog> {
}
