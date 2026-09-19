package com.forgex.basic.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.customer.domain.entity.BasicCustomerInvoice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户开票信息 Mapper 接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
public interface BasicCustomerInvoiceMapper extends BaseMapper<BasicCustomerInvoice> {
}
