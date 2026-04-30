package com.forgex.basic.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.domain.param.CustomerApprovalStartParam;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import com.forgex.basic.customer.domain.param.CustomerSaveParam;
import com.forgex.basic.customer.domain.param.CustomerWorkflowCallbackParam;

import java.util.List;

public interface ICustomerService extends IService<BasicCustomer> {
    Page<CustomerDTO> page(CustomerPageParam param);
    List<CustomerDTO> list(CustomerPageParam param);
    CustomerDTO getDetailById(Long id);
    Long create(CustomerSaveParam param);
    Boolean update(CustomerSaveParam param);
    Boolean delete(Long id);
    String generateTenant(Long id);
    Long startApproval(CustomerApprovalStartParam param);
    Boolean handleWorkflowCallback(CustomerWorkflowCallbackParam param);
}
