package com.forgex.basic.worksection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.mapper.BasicFactoryMapper;
import com.forgex.basic.process.domain.entity.BasicProcess;
import com.forgex.basic.process.mapper.BasicProcessMapper;
import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import com.forgex.basic.productionline.mapper.BasicProductionLineMapper;
import com.forgex.basic.worksection.domain.dto.WorkSectionDTO;
import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import com.forgex.basic.worksection.domain.param.WorkSectionPageParam;
import com.forgex.basic.worksection.mapper.BasicWorkSectionMapper;
import com.forgex.basic.worksection.service.IWorkSectionService;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import com.forgex.basic.workshop.mapper.BasicWorkshopMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 工段主数据服务实现。
 * <p>
 * 负责工段主数据的分页/列表/详情查询与保存逻辑，包含：
 * <ul>
 *   <li>工段编码唯一性校验（租户内 + 排除自身）</li>
 *   <li>所属车间存在性、启用状态校验</li>
 *   <li>所属产线（可选）存在性、启用状态校验</li>
 *   <li>保存时填充车间、产线快照字段（workshopCode / workshopName / productionLineCode / productionLineName）</li>
 *   <li>删除前校验工段下是否存在启用状态的工序，存在则抛出 {@code WORK_SECTION_REFERENCE_EXISTS}</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkSectionServiceImpl extends ServiceImpl<BasicWorkSectionMapper, BasicWorkSection> implements IWorkSectionService {

    private final BasicWorkSectionMapper workSectionMapper;
    private final BasicWorkshopMapper workshopMapper;
    private final BasicProductionLineMapper productionLineMapper;
    private final BasicProcessMapper processMapper;
    private final BasicFactoryMapper factoryMapper;

    /**
     * 分页查询工段。
     *
     * @param param 分页查询参数，允许为 null
     * @return 工段分页结果
     */
    @Override
    public Page<WorkSectionDTO> page(WorkSectionPageParam param) {
        WorkSectionPageParam safeParam = param == null ? new WorkSectionPageParam() : param;
        Page<BasicWorkSection> entityPage = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicWorkSection> workSectionPage = workSectionMapper.selectPage(entityPage, buildWrapper(safeParam));

        Page<WorkSectionDTO> dtoPage = new Page<>(workSectionPage.getCurrent(), workSectionPage.getSize(), workSectionPage.getTotal());
        dtoPage.setRecords(workSectionPage.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    /**
     * 查询工段列表（不分页）。
     *
     * @param param 查询参数，允许为 null
     * @return 工段列表
     */
    @Override
    public List<WorkSectionDTO> list(WorkSectionPageParam param) {
        WorkSectionPageParam safeParam = param == null ? new WorkSectionPageParam() : param;
        return workSectionMapper.selectList(buildWrapper(safeParam)).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据车间 ID 查询工段列表（通常用于下拉）。
     *
     * @param workshopId 车间 ID
     * @return 工段列表
     */
    @Override
    public List<WorkSectionDTO> listByWorkshop(Long workshopId) {
        if (workshopId == null) {
            return List.of();
        }
        return workSectionMapper.selectList(new LambdaQueryWrapper<BasicWorkSection>()
                        .eq(BasicWorkSection::getWorkshopId, workshopId)
                        .eq(BasicWorkSection::getDeleted, false)
                        .orderByAsc(BasicWorkSection::getSortOrder)
                        .orderByDesc(BasicWorkSection::getCreateTime))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据产线 ID 查询工段列表（通常用于下拉）。
     *
     * @param productionLineId 产线 ID
     * @return 工段列表
     */
    @Override
    public List<WorkSectionDTO> listByProductionLine(Long productionLineId) {
        if (productionLineId == null) {
            return List.of();
        }
        return workSectionMapper.selectList(new LambdaQueryWrapper<BasicWorkSection>()
                        .eq(BasicWorkSection::getProductionLineId, productionLineId)
                        .eq(BasicWorkSection::getDeleted, false)
                        .orderByAsc(BasicWorkSection::getSortOrder)
                        .orderByDesc(BasicWorkSection::getCreateTime))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据主键查询工段详情。
     *
     * @param id 主键 ID
     * @return 工段详情，未找到返回 null
     */
    @Override
    public WorkSectionDTO getDetailById(Long id) {
        if (id == null) {
            return null;
        }
        BasicWorkSection entity = workSectionMapper.selectById(id);
        return entity == null ? null : toDTO(entity);
    }

    /**
     * 新增工段。
     *
     * @param param 工段实体参数
     * @return 新增工段的主键 ID
     * @throws I18nBusinessException 参数无效 / 编码重复 / 车间不存在或已禁用 / 产线不存在或已禁用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BasicWorkSection param) {
        validateSave(param, true);
        fillSnapshots(param);
        normalizeFields(param);
        workSectionMapper.insert(param);
        return param.getId();
    }

    /**
     * 更新工段。
     *
     * @param param 工段实体参数，必须携带主键
     * @return 是否处理成功
     * @throws I18nBusinessException 参数无效 / 编码重复 / 车间不存在或已禁用 / 产线不存在或已禁用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BasicWorkSection param) {
        validateSave(param, false);
        BasicWorkSection existing = requireWorkSection(param.getId());
        // 编码在创建后保持不可变，保持与车间/产线主数据一致风格
        if (!Objects.equals(normalizeCode(existing.getWorkSectionCode()), normalizeCode(param.getWorkSectionCode()))) {
            throw basicException(BasicPromptEnum.WORK_SECTION_CODE_EXISTS, "工段编码创建后不可修改");
        }
        param.setWorkSectionCode(existing.getWorkSectionCode());
        fillSnapshots(param);
        normalizeFields(param);
        workSectionMapper.updateById(param);
        return true;
    }

    /**
     * 删除工段。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     * @throws I18nBusinessException 工段不存在 / 工段下存在启用状态的工序（{@code WORK_SECTION_REFERENCE_EXISTS}）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireWorkSection(id);
        assertNoActiveProcess(List.of(id));
        workSectionMapper.deleteById(id);
        return true;
    }

    /**
     * 批量删除工段。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     * @throws I18nBusinessException 任一工段存在启用状态的工序（{@code WORK_SECTION_REFERENCE_EXISTS}）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        assertNoActiveProcess(ids);
        workSectionMapper.deleteBatchIds(ids);
        return true;
    }

    /**
     * 构建分页/列表查询条件。
     */
    private LambdaQueryWrapper<BasicWorkSection> buildWrapper(WorkSectionPageParam param) {
        return new LambdaQueryWrapper<BasicWorkSection>()
                .eq(BasicWorkSection::getDeleted, false)
                .eq(param.getWorkshopId() != null, BasicWorkSection::getWorkshopId, param.getWorkshopId())
                .eq(param.getProductionLineId() != null, BasicWorkSection::getProductionLineId, param.getProductionLineId())
                .like(StringUtils.hasText(param.getWorkSectionCode()), BasicWorkSection::getWorkSectionCode, param.getWorkSectionCode())
                .like(StringUtils.hasText(param.getWorkSectionName()), BasicWorkSection::getWorkSectionName, param.getWorkSectionName())
                .eq(param.getStatus() != null, BasicWorkSection::getStatus, param.getStatus())
                .orderByAsc(BasicWorkSection::getSortOrder)
                .orderByDesc(BasicWorkSection::getCreateTime);
    }

    /**
     * 校验工段保存参数：基础必填、编码唯一、关联车间、关联产线。
     */
    private void validateSave(BasicWorkSection param, boolean create) {
        if (param == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_DATA_REQUIRED);
        }
        if (!create && param.getId() == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_NOT_FOUND);
        }
        if (!StringUtils.hasText(param.getWorkSectionCode())) {
            throw basicException(BasicPromptEnum.WORK_SECTION_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(param.getWorkSectionName())) {
            throw basicException(BasicPromptEnum.WORK_SECTION_NAME_REQUIRED);
        }
        // 编码唯一性校验
        BasicWorkSection same = findByCode(normalizeCode(param.getWorkSectionCode()));
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw basicException(BasicPromptEnum.WORK_SECTION_CODE_EXISTS);
        }
        // 车间校验
        BasicWorkshop workshop = requireWorkshop(param.getWorkshopId());
        if (!Boolean.TRUE.equals(workshop.getStatus())) {
            throw basicException(BasicPromptEnum.WORK_SECTION_WORKSHOP_DISABLED);
        }
        // 产线校验（可选，但若指定则必须存在且启用）
        if (param.getProductionLineId() != null) {
            BasicProductionLine productionLine = requireProductionLine(param.getProductionLineId());
            if (!Integer.valueOf(1).equals(productionLine.getStatus())) {
                throw basicException(BasicPromptEnum.WORK_SECTION_PRODUCTION_LINE_DISABLED);
            }
        }
    }

    /**
     * 填充车间与产线快照字段。
     */
    private void fillSnapshots(BasicWorkSection param) {
        if (param.getWorkshopId() != null) {
            BasicWorkshop workshop = workshopMapper.selectById(param.getWorkshopId());
            if (workshop != null) {
                param.setWorkshopCode(workshop.getWorkshopCode());
                param.setWorkshopName(workshop.getWorkshopName());
            }
        }
        if (param.getProductionLineId() != null) {
            BasicProductionLine productionLine = productionLineMapper.selectById(param.getProductionLineId());
            if (productionLine != null) {
                param.setProductionLineCode(productionLine.getProductionLineCode());
                param.setProductionLineName(productionLine.getProductionLineName());
            }
        } else {
            // 产线 ID 为空时清空快照，避免脏数据
            param.setProductionLineCode(null);
            param.setProductionLineName(null);
        }
    }

    /**
     * 规范化字段。
     */
    private void normalizeFields(BasicWorkSection param) {
        param.setWorkSectionCode(normalizeCode(param.getWorkSectionCode()));
        param.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        param.setSortOrder(param.getSortOrder() == null ? 0 : param.getSortOrder());
    }

    /**
     * 校验指定工段集合下是否仍存在启用状态的工序。
     */
    private void assertNoActiveProcess(List<Long> workSectionIds) {
        if (CollectionUtils.isEmpty(workSectionIds)) {
            return;
        }
        Long count = processMapper.selectCount(new LambdaQueryWrapper<BasicProcess>()
                .in(BasicProcess::getWorkSectionId, workSectionIds)
                .eq(BasicProcess::getStatus, 1)
                .eq(BasicProcess::getDeleted, false));
        if (count != null && count > 0) {
            throw basicException(BasicPromptEnum.WORK_SECTION_REFERENCE_EXISTS);
        }
    }

    /**
     * 按工段编码查询未删除的工段。
     */
    private BasicWorkSection findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return workSectionMapper.selectOne(new LambdaQueryWrapper<BasicWorkSection>()
                .eq(BasicWorkSection::getWorkSectionCode, code)
                .eq(BasicWorkSection::getDeleted, false)
                .last("LIMIT 1"));
    }

    /**
     * 校验工段存在性。
     */
    private BasicWorkSection requireWorkSection(Long id) {
        if (id == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_NOT_FOUND);
        }
        BasicWorkSection entity = workSectionMapper.selectById(id);
        if (entity == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验车间存在性。
     */
    private BasicWorkshop requireWorkshop(Long workshopId) {
        if (workshopId == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_WORKSHOP_NOT_FOUND);
        }
        BasicWorkshop workshop = workshopMapper.selectById(workshopId);
        if (workshop == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_WORKSHOP_NOT_FOUND);
        }
        return workshop;
    }

    /**
     * 校验产线存在性。
     */
    private BasicProductionLine requireProductionLine(Long productionLineId) {
        BasicProductionLine productionLine = productionLineMapper.selectById(productionLineId);
        if (productionLine == null) {
            throw basicException(BasicPromptEnum.WORK_SECTION_PRODUCTION_LINE_NOT_FOUND);
        }
        return productionLine;
    }

    /**
     * 将工段实体转换为 DTO，并补充工厂维度信息。
     */
    private WorkSectionDTO toDTO(BasicWorkSection entity) {
        if (entity == null) {
            return null;
        }
        WorkSectionDTO dto = new WorkSectionDTO();
        BeanUtils.copyProperties(entity, dto);
        // 工厂信息从车间关联获取
        if (entity.getWorkshopId() != null) {
            BasicWorkshop workshop = workshopMapper.selectById(entity.getWorkshopId());
            if (workshop != null && workshop.getFactoryId() != null) {
                BasicFactory factory = factoryMapper.selectById(workshop.getFactoryId());
                if (factory != null) {
                    dto.setFactoryId(factory.getId());
                    dto.setFactoryCode(factory.getFactoryCode());
                    dto.setFactoryName(factory.getFactoryName());
                }
            }
        }
        return dto;
    }

    /**
     * 去除字符串两端空格，null 保持为 null。
     */
    private String normalizeCode(String code) {
        return StringUtils.hasText(code) ? code.trim() : null;
    }

    /**
     * 统一构造业务异常。
     */
    private I18nBusinessException basicException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }
}
