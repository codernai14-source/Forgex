package com.forgex.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.report.domain.dto.ReportDatasourceDTO;
import com.forgex.report.domain.entity.ReportDatasource;
import com.forgex.report.domain.param.ReportDatasourceParam;

import java.util.List;

/**
 * 报表数据源服务接口
 * <p>
 * 提供报表数据源的业务逻辑接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IService
 * @see ReportDatasource
 * @see ReportDatasourceDTO
 */
public interface IReportDatasourceService extends IService<ReportDatasource> {

    /**
     * 分页查询报表数据源
     * <p>
     * 根据查询条件动态构建查询语句，返回分页结果
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see ReportDatasourceParam
     * @see ReportDatasourceDTO
     */
    Page<ReportDatasourceDTO> pageByParam(ReportDatasourceParam param);

    /**
     * 查询所有数据源列表
     * <p>
     * 仅返回启用状态的数据源
     * </p>
     *
     * @return 数据源列表
     * @see ReportDatasourceDTO
     */
    List<ReportDatasourceDTO> listEnabled();

    /**
     * 根据 ID 获取数据源详情
     *
     * @param id 数据源 ID
     * @return 数据源 DTO
     * @throws com.forgex.common.exception.BusinessException 当数据源不存在时抛出
     * @see ReportDatasourceDTO
     */
    ReportDatasourceDTO getById(Long id);

    /**
     * 根据编码获取数据源
     *
     * @param code 数据源编码
     * @return 数据源 DTO
     * @throws com.forgex.common.exception.BusinessException 当数据源不存在时抛出
     * @see ReportDatasourceDTO
     */
    ReportDatasourceDTO getByCode(String code);

    /**
     * 保存数据源
     * <p>
     * 支持新增和更新，根据 ID 判断
     * 密码字段会自动加密处理
     * </p>
     *
     * @param dto 数据源 DTO
     * @return 保存后的数据源 DTO
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see ReportDatasourceDTO
     */
    ReportDatasourceDTO save(ReportDatasourceDTO dto);

    /**
     * 测试数据源连接
     * <p>
     * 验证数据源配置是否正确
     * </p>
     *
     * @param dto 数据源 DTO
     * @return 连接是否成功
     * @throws Exception 当连接失败时抛出异常
     */
    boolean testConnection(ReportDatasourceDTO dto);

    /**
     * 批量删除数据源
     *
     * @param ids 数据源 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void deleteByIds(List<Long> ids);
}
