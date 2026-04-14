package com.forgex.basic.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.BusinessException;
import com.forgex.basic.domain.dto.MaterialDTO;
import com.forgex.basic.domain.dto.MaterialExtendDTO;
import com.forgex.basic.domain.entity.BasicMaterial;
import com.forgex.basic.domain.entity.BasicMaterialExtend;
import com.forgex.basic.domain.param.MaterialPageParam;
import com.forgex.basic.domain.response.MaterialDetailResponse;
import com.forgex.basic.domain.vo.MaterialVO;
import com.forgex.basic.domain.vo.MaterialExtendVO;
import com.forgex.basic.mapper.BasicMaterialMapper;
import com.forgex.basic.mapper.BasicMaterialExtendMapper;
import com.forgex.basic.service.IMaterialService;
import com.forgex.basic.util.MaterialCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料管理服务实现类
 * <p>
 * 提供物料相关的业务逻辑处理，包括：
 * 1. 物料 CRUD 操作
 * 2. 物料分页查询
 * 3. 物料编码生成
 * 4. 物料导入导出
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IMaterialService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends ServiceImpl<BasicMaterialMapper, BasicMaterial> implements IMaterialService {

    private final BasicMaterialMapper materialMapper;
    private final BasicMaterialExtendMapper materialExtendMapper;
    private final MaterialCodeGenerator materialCodeGenerator;

    /**
     * 分页查询物料列表
     * <p>
     * 支持多条件筛选，返回物料 VO 列表
     * </p>
     *
     * @param tenantId 租户 ID
     * @param param 查询参数
     * @return 物料分页列表
     */
    @Override
    public IPage<MaterialVO> pageMaterials(Long tenantId, MaterialPageParam param) {
        // 泛型改为 BasicMaterial，以匹配 Mapper 操作的实体类型
        Page<BasicMaterial> entityPage = new Page<>(param.getPageNum(), param.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<BasicMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterial::getTenantId, tenantId)
                .eq(BasicMaterial::getDeleted, 0);

        // 动态添加查询条件
        if (StringUtils.hasText(param.getMaterialCode())) {
            wrapper.like(BasicMaterial::getMaterialCode, param.getMaterialCode());
        }
        if (StringUtils.hasText(param.getMaterialName())) {
            wrapper.like(BasicMaterial::getMaterialName, param.getMaterialName());
        }
        if (StringUtils.hasText(param.getMaterialType())) {
            wrapper.eq(BasicMaterial::getMaterialType, param.getMaterialType());
        }
        if (StringUtils.hasText(param.getMaterialCategory())) {
            wrapper.eq(BasicMaterial::getMaterialCategory, param.getMaterialCategory());
        }
        if (param.getStatus() != null) {
            wrapper.eq(BasicMaterial::getStatus, param.getStatus());
        }
        if (StringUtils.hasText(param.getApprovalStatus())) {
            wrapper.eq(BasicMaterial::getApprovalStatus, param.getApprovalStatus());
        }

        // 执行分页查询，返回实体分页对象
        IPage<BasicMaterial> materialPage = materialMapper.selectPage(entityPage, wrapper);

        // 构建 VO 分页对象，复用实体分页的分页属性
        IPage<MaterialVO> voPage = new Page<>(materialPage.getCurrent(), materialPage.getSize(), materialPage.getTotal());
        List<MaterialVO> voList = materialPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 根据 ID 查询物料详情
     * <p>
     * 返回物料主表信息和所有扩展信息
     * </p>
     *
     * @param tenantId 租户 ID
     * @param id 物料 ID
     * @return 物料详情响应
     */
    @Override
    public MaterialDetailResponse getMaterialDetail(Long tenantId, Long id) {
        // 查询物料主表
        BasicMaterial material = materialMapper.selectById(id);
        if (material == null || !material.getTenantId().equals(tenantId)) {
            throw new BusinessException("物料不存在");
        }

        MaterialDetailResponse response = new MaterialDetailResponse();
        BeanUtils.copyProperties(material, response);

        // 查询所有扩展信息
        LambdaQueryWrapper<BasicMaterialExtend> extendWrapper = new LambdaQueryWrapper<>();
        extendWrapper.eq(BasicMaterialExtend::getMaterialId, id)
                .eq(BasicMaterialExtend::getTenantId, tenantId)
                .eq(BasicMaterialExtend::getDeleted, 0);
        List<BasicMaterialExtend> extendsList = materialExtendMapper.selectList(extendWrapper);

        // 转换为 VO
        List<MaterialExtendVO> extendVOList = extendsList.stream()
                .map(this::convertToExtendVO)
                .collect(Collectors.toList());
        response.setExtendList(extendVOList);

        return response;
    }

    /**
     * 创建物料
     * <p>
     * 支持自动生成物料编码，支持保存扩展信息
     * </p>
     *
     * @param tenantId 租户 ID
     * @param dto 物料 DTO
     * @return 物料 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createMaterial(Long tenantId, MaterialDTO dto) {
        // 校验物料编码唯一性
        if (!StringUtils.hasText(dto.getMaterialCode())) {
            // 自动生成编码
            dto.setMaterialCode(materialCodeGenerator.generateMaterialCode(tenantId));
        } else if (!materialCodeGenerator.validateMaterialCodeUnique(dto.getMaterialCode(), tenantId)) {
            throw new BusinessException("物料编码已存在");
        }

        // 创建物料主表
        BasicMaterial material = new BasicMaterial();
        BeanUtils.copyProperties(dto, material);
        material.setTenantId(tenantId);
        materialMapper.insert(material);

        // 保存扩展信息
        if (dto.getExtendList() != null && !dto.getExtendList().isEmpty()) {
            for (MaterialExtendDTO extendDTO : dto.getExtendList()) {
                BasicMaterialExtend extend = new BasicMaterialExtend();
                extend.setMaterialId(material.getId());
                extend.setModule(extendDTO.getModule());
                extend.setExtendJson(extendDTO.getExtendJson());
                extend.setTenantId(tenantId);
                materialExtendMapper.insert(extend);
            }
        }

        log.info("创建物料成功，物料 ID: {}, 物料编码：{}", material.getId(), material.getMaterialCode());
        return material.getId();
    }

    /**
     * 更新物料
     * <p>
     * 支持更新主表信息和扩展信息
     * </p>
     *
     * @param tenantId 租户 ID
     * @param dto 物料 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMaterial(Long tenantId, MaterialDTO dto) {
        // 校验物料是否存在
        BasicMaterial existing = materialMapper.selectById(dto.getId());
        if (existing == null || !existing.getTenantId().equals(tenantId)) {
            throw new BusinessException("物料不存在");
        }

        // 校验物料编码唯一性（排除自身）
        if (StringUtils.hasText(dto.getMaterialCode())) {
            Integer count = materialMapper.countByMaterialCodeExclude(dto.getMaterialCode(), dto.getId(), tenantId);
            if (count != null && count > 0) {
                throw new BusinessException("物料编码已存在");
            }
        }

        // 更新物料主表
        BasicMaterial material = new BasicMaterial();
        BeanUtils.copyProperties(dto, material);
        materialMapper.updateById(material);

        // 更新扩展信息（先删除后新增）
        LambdaQueryWrapper<BasicMaterialExtend> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(BasicMaterialExtend::getMaterialId, dto.getId())
                .eq(BasicMaterialExtend::getTenantId, tenantId);
        materialExtendMapper.delete(deleteWrapper);

        if (dto.getExtendList() != null && !dto.getExtendList().isEmpty()) {
            for (MaterialExtendDTO extendDTO : dto.getExtendList()) {
                BasicMaterialExtend extend = new BasicMaterialExtend();
                extend.setMaterialId(dto.getId());
                extend.setModule(extendDTO.getModule());
                extend.setExtendJson(extendDTO.getExtendJson());
                extend.setTenantId(tenantId);
                materialExtendMapper.insert(extend);
            }
        }

        log.info("更新物料成功，物料 ID: {}", dto.getId());
    }

    /**
     * 删除物料
     * <p>
     * 逻辑删除，同时删除关联的扩展信息
     * </p>
     *
     * @param tenantId 租户 ID
     * @param id 物料 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMaterial(Long tenantId, Long id) {
        // 校验物料是否存在
        BasicMaterial material = materialMapper.selectById(id);
        if (material == null || !material.getTenantId().equals(tenantId)) {
            throw new BusinessException("物料不存在");
        }

        // 逻辑删除主表
        materialMapper.deleteById(id);

        // 逻辑删除扩展信息
        LambdaQueryWrapper<BasicMaterialExtend> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(BasicMaterialExtend::getMaterialId, id)
                .eq(BasicMaterialExtend::getTenantId, tenantId);
        materialExtendMapper.delete(deleteWrapper);

        log.info("删除物料成功，物料 ID: {}", id);
    }

    /**
     * 转换为 MaterialVO
     */
    private MaterialVO convertToVO(BasicMaterial material) {
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        return vo;
    }

    /**
     * 转换为 MaterialExtendVO
     */
    private MaterialExtendVO convertToExtendVO(BasicMaterialExtend extend) {
        MaterialExtendVO vo = new MaterialExtendVO();
        BeanUtils.copyProperties(extend, vo);
        return vo;
    }
}
