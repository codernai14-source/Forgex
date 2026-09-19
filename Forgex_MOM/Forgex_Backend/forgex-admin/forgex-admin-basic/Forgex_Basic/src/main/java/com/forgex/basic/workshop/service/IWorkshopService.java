package com.forgex.basic.workshop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.workshop.domain.dto.WorkshopDTO;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import com.forgex.basic.workshop.domain.param.WorkshopPageParam;

import java.util.List;

/**
 * 车间主数据服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
public interface IWorkshopService extends IService<BasicWorkshop> {

    Page<WorkshopDTO> page(WorkshopPageParam param);

    List<WorkshopDTO> list(WorkshopPageParam param);

    WorkshopDTO detail(Long id);

    Long create(BasicWorkshop param);

    Boolean update(BasicWorkshop param);

    Boolean delete(Long id);

    Boolean batchDelete(List<Long> ids);

    /**
     * 根据工厂 ID 查询启用的车间列表（通常用于下拉）。
     *
     * @param factoryId 工厂 ID
     * @return 启用状态的车间列表
     */
    List<WorkshopDTO> listByFactory(Long factoryId);
}
