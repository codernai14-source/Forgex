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

/**
 * 客户服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface ICustomerService extends IService<BasicCustomer> {
    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    Page<CustomerDTO> page(CustomerPageParam param);
    /**
     * 查询数据列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    List<CustomerDTO> list(CustomerPageParam param);
    /**
     * 根据主键查询详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    CustomerDTO getDetailById(Long id);
    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    Long create(CustomerSaveParam param);
    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    Boolean update(CustomerSaveParam param);
    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);
    /**
     * 生成关联租户。
     *
     * @param id 主键 ID
     * @return 字符串结果
     */
    String generateTenant(Long id);
    /**
     * 发起审批流程。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    Long startApproval(CustomerApprovalStartParam param);
    /**
     * 处理工作流回调。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    Boolean handleWorkflowCallback(CustomerWorkflowCallbackParam param);
}
