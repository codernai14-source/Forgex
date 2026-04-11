package com.forgex.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.report.domain.dto.ReportTemplateDTO;
import com.forgex.report.domain.entity.ReportTemplate;
import com.forgex.report.domain.param.ReportTemplateParam;

import java.util.List;

/**
 * 报表模板服务接口
 * <p>
 * 提供报表模板的业务逻辑接口
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see IService
 * @see ReportTemplate
 * @see ReportTemplateDTO
 */
public interface IReportTemplateService extends IService<ReportTemplate> {

    /**
     * 分页查询报表模板
     * <p>
     * 根据查询条件动态构建查询语句，返回分页结果
     * 支持关联查询分类名称和数据源名称
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see ReportTemplateParam
     * @see ReportTemplateDTO
     */
    Page<ReportTemplateDTO> pageByParam(ReportTemplateParam param);

    /**
     * 根据 ID 获取模板详情
     *
     * @param id 模板 ID
     * @return 模板 DTO
     * @throws com.forgex.common.exception.BusinessException 当模板不存在时抛出
     * @see ReportTemplateDTO
     */
    ReportTemplateDTO getById(Long id);

    /**
     * 根据编码获取模板
     *
     * @param code 模板编码
     * @param engineType 引擎类型
     * @return 模板 DTO
     * @throws com.forgex.common.exception.BusinessException 当模板不存在时抛出
     * @see ReportTemplateDTO
     */
    ReportTemplateDTO getByCode(String code, String engineType);

    /**
     * 保存模板
     * <p>
     * 支持新增和更新，根据 ID 判断
     * 编码唯一性校验
     * </p>
     *
     * @param dto 模板 DTO
     * @return 保存后的模板 DTO
     * @throws IllegalArgumentException 当参数为空时抛出
     * @see ReportTemplateDTO
     */
    ReportTemplateDTO save(ReportTemplateDTO dto);

    /**
     * 更新模板内容
     * <p>
     * 用于报表设计器保存时调用
     * </p>
     *
     * @param id 模板 ID
     * @param content 模板内容
     * @throws com.forgex.common.exception.BusinessException 当模板不存在时抛出
     */
    void updateContent(Long id, String content);

    /**
     * 批量删除模板
     *
     * @param ids 模板 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void deleteByIds(List<Long> ids);

    /**
     * 导出模板文件
     * <p>
     * 将模板内容导出为文件
     * </p>
     *
     * @param id 模板 ID
     * @return 文件路径
     * @throws com.forgex.common.exception.BusinessException 当模板不存在或导出失败时抛出
     */
    String exportTemplate(Long id);

    /**
     * 导入模板文件
     * <p>
     * 从文件导入模板内容
     * </p>
     *
     * @param filePath 文件路径
     * @param engineType 引擎类型
     * @return 导入后的模板 DTO
     * @throws IllegalArgumentException 当文件不存在或格式错误时抛出
     * @see ReportTemplateDTO
     */
    ReportTemplateDTO importTemplate(String filePath, String engineType);
}
