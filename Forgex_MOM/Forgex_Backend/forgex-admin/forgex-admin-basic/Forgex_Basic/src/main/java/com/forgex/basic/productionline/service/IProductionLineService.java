package com.forgex.basic.productionline.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.productionline.domain.dto.ProductionLineDTO;
import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import com.forgex.basic.productionline.domain.param.ProductionLinePageParam;

import java.util.List;

/**
 * 产线主数据服务接口。
 * <p>
 * 提供产线的分页查询、列表查询、车间级联下拉、详情查询、新增、修改、删除和批量删除能力。
 * 所有方法均围绕 {@link BasicProductionLine} / {@link ProductionLineDTO} 展开。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
public interface IProductionLineService extends IService<BasicProductionLine> {

    /**
     * 分页查询产线。
     *
     * @param param 分页查询参数，允许为 null
     * @return 产线分页结果
     */
    Page<ProductionLineDTO> page(ProductionLinePageParam param);

    /**
     * 查询产线列表（不分页）。
     *
     * @param param 查询参数，允许为 null
     * @return 产线列表
     */
    List<ProductionLineDTO> list(ProductionLinePageParam param);

    /**
     * 根据车间 ID 查询产线列表（通常用于下拉）。
     *
     * @param workshopId 车间 ID
     * @return 产线列表
     */
    List<ProductionLineDTO> listByWorkshop(Long workshopId);

    /**
     * 根据主键查询产线详情。
     *
     * @param id 主键 ID
     * @return 产线详情，未找到返回 null
     */
    ProductionLineDTO getDetailById(Long id);

    /**
     * 新增产线。
     *
     * @param param 产线实体参数
     * @return 新增产线的主键 ID
     */
    Long create(BasicProductionLine param);

    /**
     * 更新产线。
     *
     * @param param 产线实体参数，必须携带主键
     * @return 是否处理成功
     */
    Boolean update(BasicProductionLine param);

    /**
     * 删除产线。
     * <p>
     * 若产线被工段引用，会抛出 {@code PRODUCTION_LINE_REFERENCE_EXISTS} 业务异常。
     * </p>
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);

    /**
     * 批量删除产线。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     */
    Boolean batchDelete(List<Long> ids);
}
