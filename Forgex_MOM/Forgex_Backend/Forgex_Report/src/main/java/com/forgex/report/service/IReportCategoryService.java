package com.forgex.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.report.domain.dto.ReportCategoryDTO;
import com.forgex.report.domain.entity.ReportCategory;
import com.forgex.report.domain.param.ReportCategoryParam;

import java.util.List;

/**
 * 报表分类服务接口
 * <p>
 * 提供报表分类的业务逻辑接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IService
 * @see ReportCategory
 * @see ReportCategoryDTO
 */
public interface IReportCategoryService extends IService<ReportCategory> {

    /**
     * 分页查询报表分类
     * <p>
     * 根据查询条件动态构建查询语句，返回分页结果
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see ReportCategoryParam
     * @see ReportCategoryDTO
     */
    Page<ReportCategoryDTO> pageByParam(ReportCategoryParam param);

    /**
     * 查询所有分类列表
     * <p>
     * 按排序号升序排列
     * </p>
     *
     * @return 分类列表
     * @see ReportCategoryDTO
     */
    List<ReportCategoryDTO> listAll();

    /**
     * 根据 ID 获取分类详情
     *
     * @param id 分类 ID
     * @return 分类 DTO
     * @throws com.forgex.common.exception.I18nBusinessException 当分类不存在时抛出
     * @see ReportCategoryDTO
     */
    ReportCategoryDTO getById(Long id);

    /**
     * 保存分类
     * <p>
     * 支持新增和更新，根据 ID 判断
     * </p>
     *
     * @param dto 分类 DTO
     * @return 保存后的分类 DTO
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see ReportCategoryDTO
     */
    ReportCategoryDTO save(ReportCategoryDTO dto);

    /**
     * 批量删除分类
     *
     * @param ids 分类 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void deleteByIds(List<Long> ids);
}
