package com.forgex.basic.shift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.shift.domain.dto.ShiftDTO;
import com.forgex.basic.shift.domain.entity.BasicShift;
import com.forgex.basic.shift.domain.param.ShiftPageParam;

import java.util.List;

/**
 * 班次主数据服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
public interface IShiftService extends IService<BasicShift> {

    Page<ShiftDTO> page(ShiftPageParam param);

    List<ShiftDTO> list(ShiftPageParam param);

    ShiftDTO detail(Long id);

    Long create(ShiftDTO param);

    Boolean update(ShiftDTO param);

    Boolean delete(Long id);

    Boolean batchDelete(List<Long> ids);
}
