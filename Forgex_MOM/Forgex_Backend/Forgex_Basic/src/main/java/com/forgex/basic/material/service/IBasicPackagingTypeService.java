
package com.forgex.basic.material.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.param.PackagingTypePageParam;

/**
 * 包装方式服务接口
 * <p>
 * 提供包装方式相关的业务逻辑处理，包括：
 * 1. 包装方式分页查询
 * 2. 包装方式 CRUD 操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
public interface IBasicPackagingTypeService extends IService<BasicPackagingType> {

    /**
     * 分页查询包装方式列表
     *
     * @param param 查询参数
     * @return 包装方式分页列表
     */
    Page<BasicPackagingType> pagePackagingTypes(PackagingTypePageParam param);
}
