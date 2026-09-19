package com.forgex.basic.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.mapper.BasicFactoryMapper;
import com.forgex.basic.process.domain.dto.ProcessDTO;
import com.forgex.basic.process.domain.entity.BasicProcess;
import com.forgex.basic.process.domain.param.ProcessPageParam;
import com.forgex.basic.process.mapper.BasicProcessMapper;
import com.forgex.basic.process.service.IProcessService;
import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import com.forgex.basic.productionline.mapper.BasicProductionLineMapper;
import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import com.forgex.basic.worksection.mapper.BasicWorkSectionMapper;
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
 * 工序主数据服务实现。
 * <p>
 * 负责工序主数据的分页/列表/详情查询与保存逻辑，包含：
 * <ul>
 *   <li>工序编码唯一性校验（租户内 + 排除自身）</li>
 *   <li>所属工段存在性、启用状态校验</li>
 *   <li>保存时填充工段、产线、车间快照字段（含冗余的产线与车间信息）</li>
 *   <li>删除前校验：当前工艺路线 / BOM 模块未上线，引用校验方法保留空实现并标注 TODO，后续接入后启用</li>
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
public class ProcessServiceImpl extends ServiceImpl<BasicProcessMapper, BasicProcess> implements IProcessService {

    private final BasicProcessMapper processMapper;
    private final BasicWorkSectionMapper workSectionMapper;
    private final BasicProductionLineMapper productionLineMapper;
    private final BasicWorkshopMapper workshopMapper;
    private final BasicFactoryMapper factoryMapper;

    /**
     * 分页查询工序。
     *
     * @param param 分页查询参数，允许为 null
     * @return 工序分页结果
     */
    @Override
    public Page<ProcessDTO> page(ProcessPageParam param) {
        ProcessPageParam safeParam = param == null ? new ProcessPageParam() : param;
        Page<BasicProcess> entityPage = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicProcess> processPage = processMapper.selectPage(entityPage, buildWrapper(safeParam));

        Page<ProcessDTO> dtoPage = new Page<>(processPage.getCurrent(), processPage.getSize(), processPage.getTotal());
        dtoPage.setRecords(processPage.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    /**
     * 查询工序列表（不分页）。
     *
     * @param param 查询参数，允许为 null
     * @return 工序列表
     */
    @Override
    public List<ProcessDTO> list(ProcessPageParam param) {
        ProcessPageParam safeParam = param == null ? new ProcessPageParam() : param;
        return processMapper.selectList(buildWrapper(safeParam)).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据工段 ID 查询工序列表（通常用于下拉）。
     *
     * @param workSectionId 工段 ID
     * @return 工序列表
     */
    @Override
    public List<ProcessDTO> listByWorkSection(Long workSectionId) {
        if (workSectionId == null) {
            return List.of();
        }
        return processMapper.selectList(new LambdaQueryWrapper<BasicProcess>()
                        .eq(BasicProcess::getWorkSectionId, workSectionId)
                        .eq(BasicProcess::getDeleted, false)
                        .orderByAsc(BasicProcess::getSortOrder)
                        .orderByDesc(BasicProcess::getCreateTime))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据主键查询工序详情。
     *
     * @param id 主键 ID
     * @return 工序详情，未找到返回 null
     */
    @Override
    public ProcessDTO getDetailById(Long id) {
        if (id == null) {
            return null;
        }
        BasicProcess entity = processMapper.selectById(id);
        return entity == null ? null : toDTO(entity);
    }

    /**
     * 新增工序。
     *
     * @param param 工序实体参数
     * @return 新增工序的主键 ID
     * @throws I18nBusinessException 参数无效 / 编码重复 / 工段不存在或已禁用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BasicProcess param) {
        validateSave(param, true);
        fillSnapshots(param);
        normalizeFields(param);
        processMapper.insert(param);
        return param.getId();
    }

    /**
     * 更新工序。
     *
     * @param param 工序实体参数，必须携带主键
     * @return 是否处理成功
     * @throws I18nBusinessException 参数无效 / 编码重复 / 工段不存在或已禁用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BasicProcess param) {
        validateSave(param, false);
        BasicProcess existing = requireProcess(param.getId());
        // 编码在创建后保持不可变
        if (!Objects.equals(normalizeCode(existing.getProcessCode()), normalizeCode(param.getProcessCode()))) {
            throw basicException(BasicPromptEnum.PROCESS_CODE_EXISTS, "工序编码创建后不可修改");
        }
        param.setProcessCode(existing.getProcessCode());
        fillSnapshots(param);
        normalizeFields(param);
        processMapper.updateById(param);
        return true;
    }

    /**
     * 删除工序。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     * @throws I18nBusinessException 工序不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireProcess(id);
        assertNoRouteOrBomReference(List.of(id));
        processMapper.deleteById(id);
        return true;
    }

    /**
     * 批量删除工序。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        assertNoRouteOrBomReference(ids);
        processMapper.deleteBatchIds(ids);
        return true;
    }

    /**
     * 构建分页/列表查询条件。
     */
    private LambdaQueryWrapper<BasicProcess> buildWrapper(ProcessPageParam param) {
        return new LambdaQueryWrapper<BasicProcess>()
                .eq(BasicProcess::getDeleted, false)
                .eq(param.getWorkSectionId() != null, BasicProcess::getWorkSectionId, param.getWorkSectionId())
                .eq(param.getWorkshopId() != null, BasicProcess::getWorkshopId, param.getWorkshopId())
                .eq(param.getProductionLineId() != null, BasicProcess::getProductionLineId, param.getProductionLineId())
                .like(StringUtils.hasText(param.getProcessCode()), BasicProcess::getProcessCode, param.getProcessCode())
                .like(StringUtils.hasText(param.getProcessName()), BasicProcess::getProcessName, param.getProcessName())
                .eq(StringUtils.hasText(param.getProcessType()), BasicProcess::getProcessType, param.getProcessType())
                .eq(StringUtils.hasText(param.getReportType()), BasicProcess::getReportType, param.getReportType())
                .eq(StringUtils.hasText(param.getQcTriggerPoint()), BasicProcess::getQcTriggerPoint, param.getQcTriggerPoint())
                .eq(param.getStatus() != null, BasicProcess::getStatus, param.getStatus())
                .orderByAsc(BasicProcess::getSortOrder)
                .orderByDesc(BasicProcess::getCreateTime);
    }

    /**
     * 校验工序保存参数：基础必填、编码唯一、关联工段。
     */
    private void validateSave(BasicProcess param, boolean create) {
        if (param == null) {
            throw basicException(BasicPromptEnum.PROCESS_DATA_REQUIRED);
        }
        if (!create && param.getId() == null) {
            throw basicException(BasicPromptEnum.PROCESS_NOT_FOUND);
        }
        if (!StringUtils.hasText(param.getProcessCode())) {
            throw basicException(BasicPromptEnum.PROCESS_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(param.getProcessName())) {
            throw basicException(BasicPromptEnum.PROCESS_NAME_REQUIRED);
        }
        // 编码唯一性校验
        BasicProcess same = findByCode(normalizeCode(param.getProcessCode()));
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw basicException(BasicPromptEnum.PROCESS_CODE_EXISTS);
        }
        // 工段校验
        BasicWorkSection workSection = requireWorkSection(param.getWorkSectionId());
        if (!Integer.valueOf(1).equals(workSection.getStatus())) {
            throw basicException(BasicPromptEnum.PROCESS_WORK_SECTION_DISABLED);
        }
        // 字典值校验：仅做日志占位，实际校验逻辑由 DictService 统一调度
        if (StringUtils.hasText(param.getProcessType())) {
            log.info("validate processType={}", param.getProcessType());
        }
        if (StringUtils.hasText(param.getReportType())) {
            log.info("validate reportType={}", param.getReportType());
        }
        if (StringUtils.hasText(param.getQcTriggerPoint())) {
            log.info("validate qcTriggerPoint={}", param.getQcTriggerPoint());
        }
    }

    /**
     * 填充工段、产线、车间快照字段（含冗余字段）。
     * <p>
     * 工序所属产线和车间从工段冗余计算，避免前端漏传或工段改属后工序数据失真。
     * </p>
     */
    private void fillSnapshots(BasicProcess param) {
        if (param.getWorkSectionId() != null) {
            BasicWorkSection workSection = workSectionMapper.selectById(param.getWorkSectionId());
            if (workSection != null) {
                param.setWorkSectionCode(workSection.getWorkSectionCode());
                param.setWorkSectionName(workSection.getWorkSectionName());
                // 冗余产线与车间
                if (workSection.getProductionLineId() != null) {
                    BasicProductionLine productionLine = productionLineMapper.selectById(workSection.getProductionLineId());
                    if (productionLine != null) {
                        param.setProductionLineId(productionLine.getId());
                        param.setProductionLineCode(productionLine.getProductionLineCode());
                        param.setProductionLineName(productionLine.getProductionLineName());
                    }
                } else {
                    param.setProductionLineId(null);
                    param.setProductionLineCode(null);
                    param.setProductionLineName(null);
                }
                if (workSection.getWorkshopId() != null) {
                    BasicWorkshop workshop = workshopMapper.selectById(workSection.getWorkshopId());
                    if (workshop != null) {
                        param.setWorkshopId(workshop.getId());
                        param.setWorkshopCode(workshop.getWorkshopCode());
                        param.setWorkshopName(workshop.getWorkshopName());
                    }
                } else {
                    param.setWorkshopId(null);
                    param.setWorkshopCode(null);
                    param.setWorkshopName(null);
                }
            }
        }
    }

    /**
     * 规范化字段。
     */
    private void normalizeFields(BasicProcess param) {
        param.setProcessCode(normalizeCode(param.getProcessCode()));
        param.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        param.setSortOrder(param.getSortOrder() == null ? 0 : param.getSortOrder());
    }

    /**
     * 校验工艺路线 / BOM 是否引用了指定工序。
     * <p>
     * TODO 工艺路线 / BOM 模块上线后，注入对应 Mapper，命中时抛出 {@code PROCESS_REFERENCE_EXISTS}。
     * 当前暂无下游表，方法体保留空实现，不阻断删除流程。
     * </p>
     */
    private void assertNoRouteOrBomReference(List<Long> processIds) {
        if (CollectionUtils.isEmpty(processIds)) {
            return;
        }
        // TODO 工艺路线 / BOM 模块上线后接入引用校验
    }

    /**
     * 按工序编码查询未删除的工序。
     */
    private BasicProcess findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return processMapper.selectOne(new LambdaQueryWrapper<BasicProcess>()
                .eq(BasicProcess::getProcessCode, code)
                .eq(BasicProcess::getDeleted, false)
                .last("LIMIT 1"));
    }

    /**
     * 校验工序存在性。
     */
    private BasicProcess requireProcess(Long id) {
        if (id == null) {
            throw basicException(BasicPromptEnum.PROCESS_NOT_FOUND);
        }
        BasicProcess entity = processMapper.selectById(id);
        if (entity == null) {
            throw basicException(BasicPromptEnum.PROCESS_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验工段存在性。
     */
    private BasicWorkSection requireWorkSection(Long workSectionId) {
        if (workSectionId == null) {
            throw basicException(BasicPromptEnum.PROCESS_WORK_SECTION_NOT_FOUND);
        }
        BasicWorkSection workSection = workSectionMapper.selectById(workSectionId);
        if (workSection == null) {
            throw basicException(BasicPromptEnum.PROCESS_WORK_SECTION_NOT_FOUND);
        }
        return workSection;
    }

    /**
     * 将工序实体转换为 DTO，并补充工厂维度信息。
     */
    private ProcessDTO toDTO(BasicProcess entity) {
        if (entity == null) {
            return null;
        }
        ProcessDTO dto = new ProcessDTO();
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
