package com.forgex.basic.material.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.param.PackagingTypePageParam;
import com.forgex.basic.material.domain.vo.PackagingTypeVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 包装规格服务接口。
 * <p>
 * 提供包装规格主数据的分页查询、列表查询、创建、更新和删除联动能力。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
public interface IBasicPackagingTypeService extends IService<BasicPackagingType> {

    /**
     * 分页查询包装规格列表。
     *
     * @param tenantId 租户 ID
     * @param param 查询参数
     * @return 包装规格分页列表
     */
    Page<PackagingTypeVO> pagePackagingTypes(Long tenantId, PackagingTypePageParam param);

    /**
     * 查询启用状态的包装规格列表。
     *
     * @param tenantId 租户 ID
     * @return 包装规格列表
     */
    List<PackagingTypeVO> listAvailable(Long tenantId);

    /**
     * 创建包装规格。
     *
     * @param tenantId 租户 ID
     * @param packagingType 包装规格实体
     * @return 包装规格 ID
     */
    @Transactional(rollbackFor = Exception.class)
    Long createPackagingType(Long tenantId, BasicPackagingType packagingType);

    /**
     * 更新包装规格。
     *
     * @param tenantId 租户 ID
     * @param packagingType 包装规格实体
     */
    @Transactional(rollbackFor = Exception.class)
    void updatePackagingType(Long tenantId, BasicPackagingType packagingType);

    /**
     * 删除包装规格并同步删除关联关系。
     *
     * @param tenantId 租户 ID
     * @param id 包装规格 ID
     */
    @Transactional(rollbackFor = Exception.class)
    void deletePackagingType(Long tenantId, Long id);

    /**
     * 批量删除包装规格并同步删除关联关系。
     *
     * @param tenantId 租户 ID
     * @param ids 包装规格 ID 集合
     */
    @Transactional(rollbackFor = Exception.class)
    void batchDeletePackagingTypes(Long tenantId, List<Long> ids);
}
