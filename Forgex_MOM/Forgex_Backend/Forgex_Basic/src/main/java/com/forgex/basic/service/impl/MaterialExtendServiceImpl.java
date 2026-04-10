package com.forgex.basic.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.BusinessException;
import com.forgex.basic.domain.dto.MaterialExtendDTO;
import com.forgex.basic.domain.entity.BasicMaterialExtend;
import com.forgex.basic.domain.vo.MaterialExtendVO;
import com.forgex.basic.mapper.BasicMaterialExtendMapper;
import com.forgex.basic.service.IMaterialExtendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料扩展信息服务实现类
 * <p>
 * 提供物料扩展信息相关的业务逻辑处理，包括：
 * 1. 根据物料ID查询扩展信息
 * 2. 根据模块查询扩展信息
 * 3. 创建/更新扩展信息
 * 4. 删除扩展信息
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see IMaterialExtendService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialExtendServiceImpl extends ServiceImpl<BasicMaterialExtendMapper, BasicMaterialExtend>
        implements IMaterialExtendService {

    private final BasicMaterialExtendMapper materialExtendMapper;

    /**
     * 根据物料ID查询扩展信息列表
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @return 扩展信息 VO 列表
     */
    public List<MaterialExtendVO> getExtendsByMaterialId(Long tenantId, Long materialId) {
        LambdaQueryWrapper<BasicMaterialExtend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtend::getMaterialId, materialId)
                .eq(BasicMaterialExtend::getTenantId, tenantId)
                .eq(BasicMaterialExtend::getDeleted, 0);

        List<BasicMaterialExtend> extendList = materialExtendMapper.selectList(wrapper);

        return extendList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据物料ID和模块查询扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @param module 模块编码
     * @return 扩展信息 VO
     */
    public MaterialExtendVO getExtendByModule(Long tenantId, Long materialId, String module) {
        if (!StringUtils.hasText(module)) {
            throw new BusinessException("模块编码不能为空");
        }

        LambdaQueryWrapper<BasicMaterialExtend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtend::getMaterialId, materialId)
                .eq(BasicMaterialExtend::getTenantId, tenantId)
                .eq(BasicMaterialExtend::getModule, module)
                .eq(BasicMaterialExtend::getDeleted, 0)
                .last("LIMIT 1");

        BasicMaterialExtend extend = materialExtendMapper.selectOne(wrapper);

        return extend != null ? convertToVO(extend) : null;
    }

    /**
     * 保存或更新物料扩展信息
     *
     * @param tenantId   租户 ID
     * @param materialId 物料 ID
     * @param dto        扩展信息 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateExtend(Long tenantId, Long materialId, MaterialExtendDTO dto) {
        if (!StringUtils.hasText(dto.getModule())) {
            throw new BusinessException("模块编码不能为空");
        }

        // 查询是否已存在该模块的扩展信息
        LambdaQueryWrapper<BasicMaterialExtend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtend::getMaterialId, materialId)
                .eq(BasicMaterialExtend::getTenantId, tenantId)
                .eq(BasicMaterialExtend::getModule, dto.getModule())
                .eq(BasicMaterialExtend::getDeleted, 0);

        BasicMaterialExtend existing = materialExtendMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新已有扩展信息
            existing.setExtendJson(dto.getExtendJson());
            materialExtendMapper.updateById(existing);
            log.info("更新物料扩展信息成功，物料 ID: {}, 模块: {}", materialId, dto.getModule());
        } else {
            // 创建新的扩展信息
            BasicMaterialExtend extend = new BasicMaterialExtend();
            extend.setMaterialId(materialId);
            extend.setModule(dto.getModule());
            extend.setExtendJson(dto.getExtendJson());
            extend.setTenantId(tenantId);
            materialExtendMapper.insert(extend);
            log.info("创建物料扩展信息成功，物料 ID: {}, 模块: {}", materialId, dto.getModule());
        }
    }

    /**
     * 批量保存物料扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @param dtoList 扩展信息 DTO 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveExtends(Long tenantId, Long materialId, List<MaterialExtendDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }

        for (MaterialExtendDTO dto : dtoList) {
            saveOrUpdateExtend(tenantId, materialId, dto);
        }
    }

    /**
     * 删除物料扩展信息
     *
     * @param tenantId 租户 ID
     * @param id 扩展信息 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteExtend(Long tenantId, Long id) {
        BasicMaterialExtend extend = materialExtendMapper.selectById(id);
        if (extend == null || !extend.getTenantId().equals(tenantId)) {
            throw new BusinessException("扩展信息不存在");
        }

        materialExtendMapper.deleteById(id);
        log.info("删除物料扩展信息成功，扩展信息 ID: {}", id);
    }

    /**
     * 根据物料ID删除所有扩展信息
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteExtendsByMaterialId(Long tenantId, Long materialId) {
        LambdaQueryWrapper<BasicMaterialExtend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtend::getMaterialId, materialId)
                .eq(BasicMaterialExtend::getTenantId, tenantId);

        materialExtendMapper.delete(wrapper);
        log.info("删除物料所有扩展信息成功，物料 ID: {}", materialId);
    }

    /**
     * 转换为 MaterialExtendVO
     *
     * @param extend 扩展信息实体
     * @return 扩展信息 VO
     */
    private MaterialExtendVO convertToVO(BasicMaterialExtend extend) {
        MaterialExtendVO vo = new MaterialExtendVO();
        BeanUtils.copyProperties(extend, vo);
        return vo;
    }
}
