package com.forgex.basic.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BasicCustomerMapper extends BaseMapper<BasicCustomer> {
}

