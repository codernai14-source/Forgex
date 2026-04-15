package com.forgex.basic.customer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.mapper.BasicCustomerMapper;
import com.forgex.basic.customer.service.ICustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerServiceImpl extends ServiceImpl<BasicCustomerMapper, BasicCustomer> implements ICustomerService {
}

