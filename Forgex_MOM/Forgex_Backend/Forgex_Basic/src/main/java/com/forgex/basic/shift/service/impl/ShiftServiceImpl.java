package com.forgex.basic.shift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.shift.domain.dto.ShiftDTO;
import com.forgex.basic.shift.domain.entity.BasicShift;
import com.forgex.basic.shift.domain.entity.BasicShiftPeriod;
import com.forgex.basic.shift.domain.param.ShiftPageParam;
import com.forgex.basic.shift.mapper.BasicShiftMapper;
import com.forgex.basic.shift.mapper.BasicShiftPeriodMapper;
import com.forgex.basic.shift.service.IShiftService;
import com.forgex.basic.team.domain.entity.BasicTeam;
import com.forgex.basic.team.mapper.BasicTeamMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 班次主数据服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl extends ServiceImpl<BasicShiftMapper, BasicShift> implements IShiftService {

    private static final Set<String> TIME_TYPES = Set.of("WORK", "REST");

    private final BasicShiftMapper shiftMapper;
    private final BasicShiftPeriodMapper periodMapper;
    private final BasicTeamMapper teamMapper;

    @Override
    public Page<ShiftDTO> page(ShiftPageParam param) {
        ShiftPageParam safeParam = param == null ? new ShiftPageParam() : param;
        Page<BasicShift> entityPage = shiftMapper.selectPage(
                new Page<>(safeParam.getPageNum(), safeParam.getPageSize()),
                buildWrapper(safeParam));
        Page<ShiftDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(item -> toDTO(item, false)).toList());
        return dtoPage;
    }

    @Override
    public List<ShiftDTO> list(ShiftPageParam param) {
        return shiftMapper.selectList(buildWrapper(param == null ? new ShiftPageParam() : param))
                .stream()
                .map(item -> toDTO(item, false))
                .toList();
    }

    @Override
    public ShiftDTO detail(Long id) {
        return id == null ? null : toDTO(shiftMapper.selectById(id), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ShiftDTO param) {
        validateSave(param, true);
        fillBeforeSave(param);
        BasicShift shift = new BasicShift();
        BeanUtils.copyProperties(param, shift);
        shiftMapper.insert(shift);
        savePeriods(shift.getId(), param.getPeriodList());
        return shift.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ShiftDTO param) {
        validateSave(param, false);
        BasicShift existing = requireShift(param.getId());
        if (!Objects.equals(normalize(existing.getShiftCode()), normalize(param.getShiftCode()))) {
            throw badRequest("班次编码创建后不可修改");
        }
        fillBeforeSave(param);
        BasicShift shift = new BasicShift();
        BeanUtils.copyProperties(param, shift);
        shift.setShiftCode(existing.getShiftCode());
        shiftMapper.updateById(shift);
        periodMapper.delete(new LambdaQueryWrapper<BasicShiftPeriod>().eq(BasicShiftPeriod::getShiftId, shift.getId()));
        savePeriods(shift.getId(), param.getPeriodList());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireShift(id);
        assertNoTeamReference(Collections.singletonList(id));
        shiftMapper.deleteById(id);
        periodMapper.delete(new LambdaQueryWrapper<BasicShiftPeriod>().eq(BasicShiftPeriod::getShiftId, id));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        assertNoTeamReference(ids);
        shiftMapper.deleteBatchIds(ids);
        periodMapper.delete(new LambdaQueryWrapper<BasicShiftPeriod>().in(BasicShiftPeriod::getShiftId, ids));
        return true;
    }

    private LambdaQueryWrapper<BasicShift> buildWrapper(ShiftPageParam param) {
        return new LambdaQueryWrapper<BasicShift>()
                .eq(BasicShift::getDeleted, false)
                .like(StringUtils.hasText(param.getShiftName()), BasicShift::getShiftName, param.getShiftName())
                .like(StringUtils.hasText(param.getShiftCode()), BasicShift::getShiftCode, param.getShiftCode())
                .eq(param.getStatus() != null, BasicShift::getStatus, param.getStatus())
                .orderByDesc(BasicShift::getCreateTime);
    }

    private void validateSave(ShiftDTO param, boolean create) {
        if (param == null || !StringUtils.hasText(param.getShiftCode()) || !StringUtils.hasText(param.getShiftName())) {
            throw badRequest("班次编码和班次名称不能为空");
        }
        if (!create && param.getId() == null) {
            throw notFound();
        }
        BasicShift same = findByCode(param.getShiftCode());
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw alreadyExists("班次编码已存在");
        }
        if (!CollectionUtils.isEmpty(param.getPeriodList())) {
            for (BasicShiftPeriod period : param.getPeriodList()) {
                if (period == null || !TIME_TYPES.contains(period.getTimeType()) || period.getStartTime() == null || period.getEndTime() == null) {
                    throw badRequest("班次时段类型、开始时间和结束时间不能为空");
                }
            }
        }
    }

    private void fillBeforeSave(ShiftDTO param) {
        param.setShiftCode(normalize(param.getShiftCode()));
        param.setShiftName(normalize(param.getShiftName()));
        param.setStatus(param.getStatus() == null || param.getStatus());
    }

    private void savePeriods(Long shiftId, List<BasicShiftPeriod> periods) {
        if (CollectionUtils.isEmpty(periods)) {
            return;
        }
        int index = 1;
        for (BasicShiftPeriod period : periods) {
            period.setId(null);
            period.setShiftId(shiftId);
            period.setSortOrder(period.getSortOrder() == null ? index : period.getSortOrder());
            periodMapper.insert(period);
            index++;
        }
    }

    private ShiftDTO toDTO(BasicShift shift, boolean withPeriods) {
        if (shift == null) {
            return null;
        }
        ShiftDTO dto = new ShiftDTO();
        BeanUtils.copyProperties(shift, dto);
        if (withPeriods) {
            dto.setPeriodList(periodMapper.selectList(new LambdaQueryWrapper<BasicShiftPeriod>()
                    .eq(BasicShiftPeriod::getShiftId, shift.getId())
                    .eq(BasicShiftPeriod::getDeleted, false)
                    .orderByAsc(BasicShiftPeriod::getSortOrder)
                    .orderByAsc(BasicShiftPeriod::getStartTime)));
        }
        return dto;
    }

    private void assertNoTeamReference(List<Long> shiftIds) {
        Long count = teamMapper.selectCount(new LambdaQueryWrapper<BasicTeam>()
                .in(BasicTeam::getCurrentShiftId, shiftIds)
                .eq(BasicTeam::getDeleted, false));
        if (count != null && count > 0) {
            throw badRequest("班次已被班组引用，无法删除");
        }
    }

    private BasicShift requireShift(Long id) {
        BasicShift shift = id == null ? null : shiftMapper.selectById(id);
        if (shift == null) {
            throw notFound();
        }
        return shift;
    }

    private BasicShift findByCode(String shiftCode) {
        return shiftMapper.selectOne(new LambdaQueryWrapper<BasicShift>()
                .eq(BasicShift::getShiftCode, normalize(shiftCode))
                .eq(BasicShift::getDeleted, false)
                .last("LIMIT 1"));
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
