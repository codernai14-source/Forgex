package com.forgex.basic.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.material.domain.entity.BasicMaterialPackagingRelation;
import com.forgex.basic.material.domain.param.MaterialPackagingSaveParam;
import com.forgex.basic.material.domain.param.MaterialPackagingSlotSaveParam;
import com.forgex.basic.material.domain.vo.MaterialPackagingRelationVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料包装规格关联服务接口。
 * <p>
 * 定义小包装、中包装、大包装三个固定槽位的查询、保存与删除联动能力。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
public interface IMaterialPackagingRelationService extends IService<BasicMaterialPackagingRelation> {

    /**
     * 查询指定物料的三槽包装规格。
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @return 三槽包装规格关联列表
     */
    List<MaterialPackagingRelationVO> listByMaterial(Long tenantId, Long materialId);

    /**
     * 查询指定包装规格已关联的物料。
     *
     * @param tenantId 租户 ID
     * @param packagingTypeId 包装规格 ID
     * @return 物料包装规格关联列表
     */
    List<MaterialPackagingRelationVO> listByPackagingType(Long tenantId, Long packagingTypeId);

    /**
     * 保存指定物料的小、中、大包装规格槽位。
     *
     * @param tenantId 租户 ID
     * @param param 保存参数
     */
    @Transactional(rollbackFor = Exception.class)
    void saveByMaterial(Long tenantId, MaterialPackagingSaveParam param);

    /**
     * 保存单个物料槽位绑定。
     *
     * @param tenantId 租户 ID
     * @param param 单槽保存参数
     */
    @Transactional(rollbackFor = Exception.class)
    void saveSlot(Long tenantId, MaterialPackagingSlotSaveParam param);

    /**
     * 删除单条物料包装规格关联。
     *
     * @param tenantId 租户 ID
     * @param id 关联 ID
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteRelation(Long tenantId, Long id);

    /**
     * 按包装规格软删除关联。
     *
     * @param tenantId 租户 ID
     * @param packagingTypeIds 包装规格 ID 集合
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteByPackagingTypeIds(Long tenantId, List<Long> packagingTypeIds);

    /**
     * 按物料软删除关联。
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteByMaterialId(Long tenantId, Long materialId);
}
