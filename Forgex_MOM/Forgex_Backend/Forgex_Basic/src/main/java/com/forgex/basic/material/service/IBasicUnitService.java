
package com.forgex.basic.material.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.param.UnitPageParam;

/**
 * 计量单位服务接口
 * <p>
 * 提供计量单位相关的业务逻辑处理，包括：
 * 1. 计量单位分页查询
 * 2. 计量单位 CRUD 操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
public interface IBasicUnitService extends IService<BasicUnit> {
    
    /**
     * 分页查询计量单位列表
     *
     * @param param 查询参数
     * @return 计量单位分页列表
     */
    Page<BasicUnit> pageUnits(UnitPageParam param);
}
