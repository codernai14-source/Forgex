package com.forgex.basic.supplier.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.supplier.domain.dto.SupplierDTO;
import com.forgex.basic.supplier.domain.entity.BasicSupplier;
import com.forgex.basic.supplier.domain.param.SupplierPageParam;
import com.forgex.basic.supplier.domain.param.SupplierReviewStartParam;
import com.forgex.basic.supplier.domain.param.SupplierSaveParam;
import com.forgex.basic.supplier.domain.param.SupplierWorkflowCallbackParam;
import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierOptionDTO;
import com.forgex.common.api.dto.SupplierQueryRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartyInvokeDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncResultDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 供应商主数据服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
public interface ISupplierService extends IService<BasicSupplier> {

    /**
     * 分页查询供应商。
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<SupplierDTO> page(SupplierPageParam param);

    /**
     * 查询供应商列表。
     *
     * @param param 查询参数
     * @return 供应商列表
     */
    List<SupplierDTO> list(SupplierPageParam param);

    /**
     * 查询供应商详情。
     *
     * @param id 供应商 ID
     * @return 供应商聚合详情
     */
    SupplierDTO getDetailById(Long id);

    /**
     * 新增供应商。
     *
     * @param param 保存参数
     * @return 供应商 ID
     */
    Long create(SupplierSaveParam param);

    /**
     * 修改供应商。
     *
     * @param param 保存参数
     * @return 是否成功
     */
    Boolean update(SupplierSaveParam param);

    /**
     * 删除供应商。
     *
     * @param id 供应商 ID
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 生成供应商租户。
     *
     * @param id 供应商 ID
     * @return 关联租户编码
     */
    String generateTenant(Long id);

    /**
     * 发起供应商资质审查。
     *
     * @param param 发起参数
     * @return 工作流执行 ID
     */
    Long startQualificationReview(SupplierReviewStartParam param);

    /**
     * 处理工作流回调。
     *
     * @param param 回调参数
     * @return 是否成功
     */
    Boolean handleWorkflowCallback(SupplierWorkflowCallbackParam param);

    /**
     * 同步第三方供应商数据。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    SupplierThirdPartySyncResultDTO syncThirdPartySuppliers(SupplierThirdPartySyncRequestDTO request);

    /**
     * 导出第三方供应商数据。
     *
     * @param request 导出请求
     * @return 聚合数据
     */
    List<SupplierAggregateDTO> exportThirdPartySuppliers(SupplierThirdPartySyncRequestDTO request);

    /**
     * 调接口平台同步第三方。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    SupplierThirdPartySyncResultDTO syncToThirdParty(SupplierThirdPartyInvokeDTO request);

    /**
     * 按编码查询供应商。
     *
     * @param request 查询请求
     * @return 供应商聚合
     */
    SupplierAggregateDTO getByCode(SupplierQueryRequestDTO request);

    /**
     * 按编码列表批量查询供应商。
     *
     * @param request 查询请求
     * @return 供应商聚合列表
     */
    List<SupplierAggregateDTO> listByCodes(SupplierQueryRequestDTO request);

    /**
     * 查询供应商下拉选项。
     *
     * @param request 查询请求
     * @return 下拉选项
     */
    List<SupplierOptionDTO> listOptions(SupplierQueryRequestDTO request);

    /**
     * 写入导入模板。
     *
     * @param outputStream 输出流
     * @throws IOException 写出异常
     */
    void writeImportTemplate(OutputStream outputStream) throws IOException;

    /**
     * 导入供应商 Excel。
     *
     * @param file Excel 文件
     * @return 同步结果
     * @throws IOException 读取异常
     */
    SupplierThirdPartySyncResultDTO importExcel(MultipartFile file) throws IOException;

    /**
     * 执行公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param);

    /**
     * 导出供应商 Excel。
     *
     * @param param 查询参数
     * @param outputStream 输出流
     * @throws IOException 写出异常
     */
    void exportExcel(SupplierPageParam param, OutputStream outputStream) throws IOException;
}
