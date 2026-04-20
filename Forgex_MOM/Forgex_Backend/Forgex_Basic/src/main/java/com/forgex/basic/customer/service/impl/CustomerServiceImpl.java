package com.forgex.basic.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.mapper.BasicCustomerMapper;
import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<BasicCustomerMapper, BasicCustomer> implements ICustomerService {

    private final BasicCustomerMapper customerMapper;

    /**
     * 分页查询客户列表
     *
     * @param param 查询参数
     * @return 客户分页列表
     */
    @Override
    public Page<CustomerDTO> page(CustomerPageParam param) {
        Page<BasicCustomer> entityPage = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<BasicCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicCustomer::getDeleted, 0);

        if (StringUtils.hasText(param.getCustomerCode())) {
            wrapper.like(BasicCustomer::getCustomerCode, param.getCustomerCode());
        }
        if (StringUtils.hasText(param.getCustomerName())) {
            wrapper.like(BasicCustomer::getCustomerName, param.getCustomerName());
        }
        if (StringUtils.hasText(param.getCustomerType())) {
            wrapper.eq(BasicCustomer::getCustomerType, param.getCustomerType());
        }
        if (param.getStatus() != null) {
            wrapper.eq(BasicCustomer::getStatus, param.getStatus());
        }

        Page<BasicCustomer> customerPage = customerMapper.selectPage(entityPage, wrapper);

        Page<CustomerDTO> dtoPage = new Page<>(customerPage.getCurrent(), customerPage.getSize(), customerPage.getTotal());
        List<CustomerDTO> dtoList = customerPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * 查询客户列表（不分页）
     *
     * @param param 查询参数
     * @return 客户列表
     */
    @Override
    public List<CustomerDTO> list(CustomerPageParam param) {
        LambdaQueryWrapper<BasicCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicCustomer::getDeleted, 0);

        if (param != null) {
            if (StringUtils.hasText(param.getCustomerCode())) {
                wrapper.like(BasicCustomer::getCustomerCode, param.getCustomerCode());
            }
            if (StringUtils.hasText(param.getCustomerName())) {
                wrapper.like(BasicCustomer::getCustomerName, param.getCustomerName());
            }
            if (StringUtils.hasText(param.getCustomerType())) {
                wrapper.eq(BasicCustomer::getCustomerType, param.getCustomerType());
            }
            if (param.getStatus() != null) {
                wrapper.eq(BasicCustomer::getStatus, param.getStatus());
            }
        }

        List<BasicCustomer> customerList = customerMapper.selectList(wrapper);
        return customerList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取客户详情
     *
     * @param id 客户 ID
     * @return 客户详情
     */
    @Override
    public CustomerDTO getDetailById(Long id) {
        BasicCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            return null;
        }
        return convertToDTO(customer);
    }

    /**
     * 转换为 CustomerDTO
     */
    private CustomerDTO convertToDTO(BasicCustomer customer) {
        CustomerDTO dto = new CustomerDTO();
        BeanUtils.copyProperties(customer, dto);
        return dto;
    }
}

