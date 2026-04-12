package com.forgex.basic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.domain.dto.MaterialExtendDTO;
import com.forgex.basic.domain.entity.BasicMaterialExtend;
import com.forgex.basic.domain.vo.MaterialExtendVO;

import java.util.List;

/**
 * 物料扩展信息服务接口
 * <p>
 * 提供物料扩展信息相关的业务方法，包括：
 * 1. 根据物料ID查询扩展信息
 * 2. 根据模块查询扩展信息
 * 3. 创建/更新扩展信息
 * 4. 删除扩展信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
public interface IMaterialExtendService extends IService<BasicMaterialExtend> {

    /**
     * 根据物料ID查询扩展信息列表
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @return 扩展信息 VO 列表
     */
    List<MaterialExtendVO> getExtendsByMaterialId(Long tenantId, Long materialId);

    /**
     * 根据物料ID和模块查询扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @param module 模块编码
     * @return 扩展信息 VO
     */
    MaterialExtendVO getExtendByModule(Long tenantId, Long materialId, String module);

    /**
     * 保存或更新物料扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @param dto 扩展信息 DTO
     */
    void saveOrUpdateExtend(Long tenantId, Long materialId, MaterialExtendDTO dto);

    /**
     * 批量保存物料扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @param dtoList 扩展信息 DTO 列表
     */
    void batchSaveExtends(Long tenantId, Long materialId, List<MaterialExtendDTO> dtoList);

    /**
     * 删除物料扩展信息
     *
     * @param tenantId 租户 ID
     * @param id 扩展信息 ID
     */
    void deleteExtend(Long tenantId, Long id);

    /**
     * 根据物料ID删除所有扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     */
    void deleteExtendsByMaterialId(Long tenantId, Long materialId);
}

