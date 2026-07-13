package com.forgex.basic.worksection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.worksection.domain.dto.WorkSectionDTO;
import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import com.forgex.basic.worksection.domain.param.WorkSectionPageParam;

import java.util.List;

/**
 * 工段主数据服务接口。
 * <p>
 * 提供工段的分页查询、列表查询、车间级联下拉、产线级联下拉、详情查询、新增、修改、删除和批量删除能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
public interface IWorkSectionService extends IService<BasicWorkSection> {

    /**
     * 分页查询工段。
     *
     * @param param 分页查询参数，允许为 null
     * @return 工段分页结果
     */
    Page<WorkSectionDTO> page(WorkSectionPageParam param);

    /**
     * 查询工段列表（不分页）。
     *
     * @param param 查询参数，允许为 null
     * @return 工段列表
     */
    List<WorkSectionDTO> list(WorkSectionPageParam param);

    /**
     * 根据车间 ID 查询工段列表（通常用于下拉）。
     *
     * @param workshopId 车间 ID
     * @return 工段列表
     */
    List<WorkSectionDTO> listByWorkshop(Long workshopId);

    /**
     * 根据产线 ID 查询工段列表（通常用于下拉）。
     *
     * @param productionLineId 产线 ID
     * @return 工段列表
     */
    List<WorkSectionDTO> listByProductionLine(Long productionLineId);

    /**
     * 根据主键查询工段详情。
     *
     * @param id 主键 ID
     * @return 工段详情，未找到返回 null
     */
    WorkSectionDTO getDetailById(Long id);

    /**
     * 新增工段。
     *
     * @param param 工段实体参数
     * @return 新增工段的主键 ID
     */
    Long create(BasicWorkSection param);

    /**
     * 更新工段。
     *
     * @param param 工段实体参数，必须携带主键
     * @return 是否处理成功
     */
    Boolean update(BasicWorkSection param);

    /**
     * 删除工段。
     * <p>
     * 若工段下存在启用状态的工序，会抛出 {@code WORK_SECTION_REFERENCE_EXISTS} 业务异常。
     * </p>
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);

    /**
     * 批量删除工段。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     */
    Boolean batchDelete(List<Long> ids);
}
