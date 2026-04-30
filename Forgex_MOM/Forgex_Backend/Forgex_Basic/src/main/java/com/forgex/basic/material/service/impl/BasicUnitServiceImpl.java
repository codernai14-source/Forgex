
package com.forgex.basic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.param.UnitPageParam;
import com.forgex.basic.material.mapper.BasicUnitMapper;
import com.forgex.basic.material.service.IBasicUnitService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 计量单位服务实现类
 * <p>
 * 实现计量单位相关的业务逻辑，包括分页查询等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Service
public class BasicUnitServiceImpl extends ServiceImpl<BasicUnitMapper, BasicUnit> implements IBasicUnitService {

    @Override
    public Page<BasicUnit> pageUnits(UnitPageParam param) {
        Page<BasicUnit> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        LambdaQueryWrapper<BasicUnit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicUnit::getDeleted, 0);
        
        if (StringUtils.hasText(param.getUnitCode())) {
            queryWrapper.like(BasicUnit::getUnitCode, param.getUnitCode());
        }
        if (StringUtils.hasText(param.getUnitName())) {
            queryWrapper.like(BasicUnit::getUnitName, param.getUnitName());
        }
        if (StringUtils.hasText(param.getUnitCategory())) {
            queryWrapper.eq(BasicUnit::getUnitCategory, param.getUnitCategory());
        }
        if (param.getStatus() != null) {
            queryWrapper.eq(BasicUnit::getStatus, param.getStatus());
        }
        
        queryWrapper.orderByAsc(BasicUnit::getSortOrder)
                    .orderByDesc(BasicUnit::getCreateTime);
        
        return page(page, queryWrapper);
    }
}
