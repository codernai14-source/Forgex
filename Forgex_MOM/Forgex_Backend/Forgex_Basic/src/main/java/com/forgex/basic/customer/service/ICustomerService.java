package com.forgex.basic.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.domain.param.CustomerPageParam;

import java.util.List;

public interface ICustomerService extends IService<BasicCustomer> {

    /**
     * 分页查询客户列表
     *
     * @param param 查询参数
     * @return 客户分页列表
     */
    Page<CustomerDTO> page(CustomerPageParam param);

    /**
     * 查询客户列表（不分页）
     *
     * @param param 查询参数
     * @return 客户列表
     */
    List<CustomerDTO> list(CustomerPageParam param);

    /**
     * 根据 ID 获取客户详情
     *
     * @param id 客户 ID
     * @return 客户详情
     */
    CustomerDTO getDetailById(Long id);
}
