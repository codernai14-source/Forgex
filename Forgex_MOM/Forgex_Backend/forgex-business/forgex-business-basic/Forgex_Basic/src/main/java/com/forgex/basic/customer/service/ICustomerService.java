package com.forgex.basic.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.domain.param.CustomerApprovalStartParam;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import com.forgex.basic.customer.domain.param.CustomerSaveParam;
import com.forgex.basic.customer.domain.param.CustomerWorkflowCallbackParam;
import com.forgex.common.api.dto.CustomerAggregateDTO;
import com.forgex.common.api.dto.CustomerThirdPartyInvokeDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncResultDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
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
     * 批量删除数据。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     */
    Boolean batchDelete(List<Long> ids);
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

    /**
     * 同步第三方客户数据。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    CustomerThirdPartySyncResultDTO syncThirdPartyCustomers(CustomerThirdPartySyncRequestDTO request);

    /**
     * 导出第三方客户数据。
     *
     * @param request 导出请求
     * @return 客户聚合列表
     */
    List<CustomerAggregateDTO> exportThirdPartyCustomers(CustomerThirdPartySyncRequestDTO request);

    /**
     * 调接口平台同步客户到第三方。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    CustomerThirdPartySyncResultDTO syncToThirdParty(CustomerThirdPartyInvokeDTO request);

    /**
     * 从第三方拉取客户数据。
     *
     * @param request 调用请求
     * @return 写入结果
     */
    CustomerThirdPartySyncResultDTO pullFromThirdParty(CustomerThirdPartyInvokeDTO request);

    /**
     * 写入客户导入模板。
     *
     * @param outputStream 输出流
     * @throws IOException 写出异常
     */
    void writeImportTemplate(OutputStream outputStream) throws IOException;

    /**
     * 导入客户 Excel。
     *
     * @param file Excel 文件
     * @return 导入结果
     * @throws IOException 读取异常
     */
    CustomerThirdPartySyncResultDTO importExcel(MultipartFile file) throws IOException;

    /**
     * 执行客户公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param);

    /**
     * 导出客户 Excel。
     *
     * @param param 查询参数
     * @param outputStream 输出流
     * @throws IOException 写出异常
     */
    void exportExcel(CustomerPageParam param, OutputStream outputStream) throws IOException;
}
