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

    IPage<MaterialVO> pageMaterials(Long tenantId, MaterialPageParam param);

    List<MaterialVO> listMaterials(Long tenantId, MaterialPageParam param);

    MaterialDetailResponse getMaterialDetail(Long tenantId, Long id);

    @Transactional(rollbackFor = Exception.class)
    Long createMaterial(Long tenantId, MaterialDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void updateMaterial(Long tenantId, MaterialDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void deleteMaterial(Long tenantId, Long id);
}

