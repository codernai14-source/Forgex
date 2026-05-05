
package com.forgex.basic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.param.PackagingTypePageParam;
import com.forgex.basic.material.mapper.BasicPackagingTypeMapper;
import com.forgex.basic.material.service.IBasicPackagingTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 包装方式服务实现类
 * <p>
 * 实现包装方式相关的业务逻辑，包括分页查询等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Service
public class BasicPackagingTypeServiceImpl extends ServiceImpl<BasicPackagingTypeMapper, BasicPackagingType> implements IBasicPackagingTypeService {

    /**
     * 分页查询包装类型。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<BasicPackagingType> pagePackagingTypes(PackagingTypePageParam param) {
        Page<BasicPackagingType> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<BasicPackagingType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicPackagingType::getDeleted, 0);

        if (StringUtils.hasText(param.getPackagingCode())) {
            queryWrapper.like(BasicPackagingType::getPackagingCode, param.getPackagingCode());
        }
        if (StringUtils.hasText(param.getPackagingName())) {
            queryWrapper.like(BasicPackagingType::getPackagingName, param.getPackagingName());
        }
        if (StringUtils.hasText(param.getPackagingMaterial())) {
            queryWrapper.like(BasicPackagingType::getPackagingMaterial, param.getPackagingMaterial());
        }
        if (param.getStatus() != null) {
            queryWrapper.eq(BasicPackagingType::getStatus, param.getStatus());
        }

        queryWrapper.orderByAsc(BasicPackagingType::getSortOrder)
                    .orderByDesc(BasicPackagingType::getCreateTime);

        return page(page, queryWrapper);
    }
}
