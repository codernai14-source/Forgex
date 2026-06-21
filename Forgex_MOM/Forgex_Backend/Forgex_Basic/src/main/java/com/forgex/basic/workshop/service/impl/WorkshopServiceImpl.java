package com.forgex.basic.workshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.mapper.BasicFactoryMapper;
import com.forgex.basic.team.domain.entity.BasicTeam;
import com.forgex.basic.team.mapper.BasicTeamMapper;
import com.forgex.basic.workshop.domain.dto.WorkshopDTO;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import com.forgex.basic.workshop.domain.param.WorkshopPageParam;
import com.forgex.basic.workshop.mapper.BasicWorkshopMapper;
import com.forgex.basic.workshop.service.IWorkshopService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 车间主数据服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl extends ServiceImpl<BasicWorkshopMapper, BasicWorkshop> implements IWorkshopService {

    private final BasicWorkshopMapper workshopMapper;
    private final BasicFactoryMapper factoryMapper;
    private final BasicTeamMapper teamMapper;

    @Override
    public Page<WorkshopDTO> page(WorkshopPageParam param) {
        WorkshopPageParam safeParam = param == null ? new WorkshopPageParam() : param;
        Page<BasicWorkshop> entityPage = workshopMapper.selectPage(
                new Page<>(safeParam.getPageNum(), safeParam.getPageSize()),
                buildWrapper(safeParam));
        Page<WorkshopDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    @Override
    public List<WorkshopDTO> list(WorkshopPageParam param) {
        return workshopMapper.selectList(buildWrapper(param == null ? new WorkshopPageParam() : param))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public WorkshopDTO detail(Long id) {
        return id == null ? null : toDTO(workshopMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BasicWorkshop param) {
        validateSave(param, true);
        param.setWorkshopCode(normalize(param.getWorkshopCode()));
        param.setStatus(param.getStatus() == null || param.getStatus());
        workshopMapper.insert(param);
        return param.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BasicWorkshop param) {
        validateSave(param, false);
        BasicWorkshop existing = requireWorkshop(param.getId());
        if (!Objects.equals(normalize(existing.getWorkshopCode()), normalize(param.getWorkshopCode()))) {
            throw badRequest("车间编码创建后不可修改");
        }
        param.setWorkshopCode(existing.getWorkshopCode());
        workshopMapper.updateById(param);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireWorkshop(id);
        assertNoTeamReference(Collections.singletonList(id));
        workshopMapper.deleteById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        assertNoTeamReference(ids);
        workshopMapper.deleteBatchIds(ids);
        return true;
    }

    /**
     * 根据工厂 ID 查询启用的车间列表（仅返回状态为启用的车间）。
     * <p>
     * 用于前端产线下拉按工厂过滤车间时调用，结果默认按 sortOrder 升序、createTime 倒序排列。
     * </p>
     *
     * @param factoryId 工厂 ID
     * @return 启用状态的车间列表
     */
    @Override
    public List<WorkshopDTO> listByFactory(Long factoryId) {
        if (factoryId == null) {
            return List.of();
        }
        return workshopMapper.selectList(new LambdaQueryWrapper<BasicWorkshop>()
                        .eq(BasicWorkshop::getFactoryId, factoryId)
                        .eq(BasicWorkshop::getStatus, true)
                        .eq(BasicWorkshop::getDeleted, false)
                        .orderByAsc(BasicWorkshop::getSortOrder)
                        .orderByDesc(BasicWorkshop::getCreateTime))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private LambdaQueryWrapper<BasicWorkshop> buildWrapper(WorkshopPageParam param) {
        return new LambdaQueryWrapper<BasicWorkshop>()
                .eq(BasicWorkshop::getDeleted, false)
                .like(StringUtils.hasText(param.getWorkshopCode()), BasicWorkshop::getWorkshopCode, param.getWorkshopCode())
                .like(StringUtils.hasText(param.getWorkshopName()), BasicWorkshop::getWorkshopName, param.getWorkshopName())
                .eq(param.getFactoryId() != null, BasicWorkshop::getFactoryId, param.getFactoryId())
                .eq(param.getStatus() != null, BasicWorkshop::getStatus, param.getStatus())
                .orderByDesc(BasicWorkshop::getCreateTime);
    }

    private void validateSave(BasicWorkshop param, boolean create) {
        if (param == null || !StringUtils.hasText(param.getWorkshopCode()) || !StringUtils.hasText(param.getWorkshopName())) {
            throw badRequest("车间编码和车间名称不能为空");
        }
        if (!create && param.getId() == null) {
            throw notFound();
        }
        BasicWorkshop same = workshopMapper.selectOne(new LambdaQueryWrapper<BasicWorkshop>()
                .eq(BasicWorkshop::getWorkshopCode, normalize(param.getWorkshopCode()))
                .eq(BasicWorkshop::getDeleted, false)
                .last("LIMIT 1"));
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw alreadyExists("车间编码已存在");
        }
    }

    private void assertNoTeamReference(List<Long> workshopIds) {
        Long count = teamMapper.selectCount(new LambdaQueryWrapper<BasicTeam>()
                .in(BasicTeam::getWorkshopId, workshopIds)
                .eq(BasicTeam::getDeleted, false));
        if (count != null && count > 0) {
            throw badRequest("车间已被班组引用，无法删除");
        }
    }

    private BasicWorkshop requireWorkshop(Long id) {
        BasicWorkshop workshop = id == null ? null : workshopMapper.selectById(id);
        if (workshop == null) {
            throw notFound();
        }
        return workshop;
    }

    private WorkshopDTO toDTO(BasicWorkshop workshop) {
        if (workshop == null) {
            return null;
        }
        WorkshopDTO dto = new WorkshopDTO();
        BeanUtils.copyProperties(workshop, dto);
        if (workshop.getFactoryId() != null) {
            BasicFactory factory = factoryMapper.selectById(workshop.getFactoryId());
            if (factory != null) {
                dto.setFactoryCode(factory.getFactoryCode());
                dto.setFactoryName(factory.getFactoryName());
            }
        }
        return dto;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private I18nBusinessException badRequest(String message) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, message);
    }

    private I18nBusinessException alreadyExists(String message) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.ALREADY_EXISTS, message);
    }

    private I18nBusinessException notFound() {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.NOT_FOUND);
    }
}
