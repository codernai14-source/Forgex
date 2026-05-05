package com.forgex.basic.material.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.material.domain.dto.MaterialDTO;
import com.forgex.basic.material.domain.entity.BasicMaterial;
import com.forgex.basic.material.domain.param.MaterialPageParam;
import com.forgex.basic.material.domain.response.MaterialDetailResponse;
import com.forgex.basic.material.domain.vo.MaterialVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料管理服务接口
 * <p>
 * 定义物料相关的核心业务逻辑，继承 MyBatis-Plus 的 IService 获得基础 CRUD 能力
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
public interface IMaterialService extends IService<BasicMaterial> {

    /**
     * 执行物料的分页materials操作。
     *
     * @param tenantId 租户 ID
     * @param param 请求参数
     * @return 处理结果
     */
    IPage<MaterialVO> pageMaterials(Long tenantId, MaterialPageParam param);

    /**
     * 执行物料的列表materials操作。
     *
     * @param tenantId 租户 ID
     * @param param 请求参数
     * @return 列表数据
     */
    List<MaterialVO> listMaterials(Long tenantId, MaterialPageParam param);

    /**
     * 获取物料详情。
     *
     * @param tenantId 租户 ID
     * @param id 主键 ID
     * @return 处理结果
     */
    MaterialDetailResponse getMaterialDetail(Long tenantId, Long id);

    /**
     * 执行物料的创建物料操作。
     *
     * @param tenantId 租户 ID
     * @param dto 数据传输对象
     * @return 数据主键 ID
     */
    @Transactional(rollbackFor = Exception.class)
    Long createMaterial(Long tenantId, MaterialDTO dto);

    /**
     * 更新物料。
     *
     * @param tenantId 租户 ID
     * @param dto 数据传输对象
     */
    @Transactional(rollbackFor = Exception.class)
    void updateMaterial(Long tenantId, MaterialDTO dto);

    /**
     * 删除物料。
     *
     * @param tenantId 租户 ID
     * @param id 主键 ID
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteMaterial(Long tenantId, Long id);
}

