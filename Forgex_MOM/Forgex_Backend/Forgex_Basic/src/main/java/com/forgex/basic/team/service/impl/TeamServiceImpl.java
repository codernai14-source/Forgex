package com.forgex.basic.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.employee.domain.entity.BasicEmployee;
import com.forgex.basic.employee.mapper.BasicEmployeeMapper;
import com.forgex.basic.shift.domain.entity.BasicShift;
import com.forgex.basic.shift.mapper.BasicShiftMapper;
import com.forgex.basic.team.domain.dto.TeamDTO;
import com.forgex.basic.team.domain.dto.TeamEmployeeDTO;
import com.forgex.basic.team.domain.entity.BasicTeam;
import com.forgex.basic.team.domain.entity.BasicTeamEmployee;
import com.forgex.basic.team.domain.param.TeamPageParam;
import com.forgex.basic.team.mapper.BasicTeamEmployeeMapper;
import com.forgex.basic.team.mapper.BasicTeamMapper;
import com.forgex.basic.team.service.ITeamService;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import com.forgex.basic.workshop.mapper.BasicWorkshopMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 班组主数据服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Service
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<BasicTeamMapper, BasicTeam> implements ITeamService {

    private final BasicTeamMapper teamMapper;
    private final BasicTeamEmployeeMapper teamEmployeeMapper;
    private final BasicEmployeeMapper employeeMapper;
    private final BasicShiftMapper shiftMapper;
    private final BasicWorkshopMapper workshopMapper;

    @Override
    public Page<TeamDTO> page(TeamPageParam param) {
        TeamPageParam safeParam = param == null ? new TeamPageParam() : param;
        Page<BasicTeam> entityPage = teamMapper.selectPage(
                new Page<>(safeParam.getPageNum(), safeParam.getPageSize()),
                buildWrapper(safeParam));
        Page<TeamDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(item -> toDTO(item, false)).toList());
        return dtoPage;
    }

    @Override
    public List<TeamDTO> list(TeamPageParam param) {
        return teamMapper.selectList(buildWrapper(param == null ? new TeamPageParam() : param))
                .stream()
                .map(item -> toDTO(item, false))
                .toList();
    }

    @Override
    public TeamDTO detail(Long id) {
        return id == null ? null : toDTO(teamMapper.selectById(id), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TeamDTO param) {
        validateSave(param, true);
        fillBeforeSave(param);
        BasicTeam team = new BasicTeam();
        BeanUtils.copyProperties(param, team);
        teamMapper.insert(team);
        saveEmployees(team.getId(), param.getEmployeeList());
        return team.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TeamDTO param) {
        validateSave(param, false);
        BasicTeam existing = requireTeam(param.getId());
        if (!Objects.equals(normalize(existing.getTeamCode()), normalize(param.getTeamCode()))) {
            throw badRequest("班组编码创建后不可修改");
        }
        fillBeforeSave(param);
        BasicTeam team = new BasicTeam();
        BeanUtils.copyProperties(param, team);
        team.setTeamCode(existing.getTeamCode());
        teamMapper.updateById(team);
        teamEmployeeMapper.delete(new LambdaQueryWrapper<BasicTeamEmployee>().eq(BasicTeamEmployee::getTeamId, team.getId()));
        saveEmployees(team.getId(), param.getEmployeeList());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireTeam(id);
        teamMapper.deleteById(id);
        teamEmployeeMapper.delete(new LambdaQueryWrapper<BasicTeamEmployee>().eq(BasicTeamEmployee::getTeamId, id));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        teamMapper.deleteBatchIds(ids);
        teamEmployeeMapper.delete(new LambdaQueryWrapper<BasicTeamEmployee>().in(BasicTeamEmployee::getTeamId, ids));
        return true;
    }

    private LambdaQueryWrapper<BasicTeam> buildWrapper(TeamPageParam param) {
        return new LambdaQueryWrapper<BasicTeam>()
                .eq(BasicTeam::getDeleted, false)
                .like(StringUtils.hasText(param.getTeamCode()), BasicTeam::getTeamCode, param.getTeamCode())
                .like(StringUtils.hasText(param.getTeamName()), BasicTeam::getTeamName, param.getTeamName())
                .eq(param.getLeaderEmployeeId() != null, BasicTeam::getLeaderEmployeeId, param.getLeaderEmployeeId())
                .eq(param.getCurrentShiftId() != null, BasicTeam::getCurrentShiftId, param.getCurrentShiftId())
                .eq(param.getWorkshopId() != null, BasicTeam::getWorkshopId, param.getWorkshopId())
                .eq(param.getStatus() != null, BasicTeam::getStatus, param.getStatus())
                .orderByDesc(BasicTeam::getCreateTime);
    }

    private void validateSave(TeamDTO param, boolean create) {
        if (param == null || !StringUtils.hasText(param.getTeamCode()) || !StringUtils.hasText(param.getTeamName())) {
            throw badRequest("班组编码和班组名称不能为空");
        }
        if (!create && param.getId() == null) {
            throw notFound();
        }
        BasicTeam same = findByCode(param.getTeamCode());
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw alreadyExists("班组编码已存在");
        }
        if (param.getLeaderEmployeeId() != null && employeeMapper.selectById(param.getLeaderEmployeeId()) == null) {
            throw badRequest("负责人不存在");
        }
        if (param.getCurrentShiftId() != null && shiftMapper.selectById(param.getCurrentShiftId()) == null) {
            throw badRequest("当前班次不存在");
        }
        if (param.getWorkshopId() != null && workshopMapper.selectById(param.getWorkshopId()) == null) {
            throw badRequest("所属车间不存在");
        }
        validateEmployees(param.getEmployeeList());
    }

    private void validateEmployees(List<TeamEmployeeDTO> employees) {
        if (CollectionUtils.isEmpty(employees)) {
            return;
        }
        Set<Long> employeeIds = new LinkedHashSet<>();
        for (TeamEmployeeDTO item : employees) {
            if (item == null || item.getEmployeeId() == null) {
                throw badRequest("班组人员不能为空");
            }
            if (!employeeIds.add(item.getEmployeeId())) {
                throw badRequest("班组人员不可重复");
            }
            if (employeeMapper.selectById(item.getEmployeeId()) == null) {
                throw badRequest("班组人员不存在");
            }
        }
    }

    private void fillBeforeSave(TeamDTO param) {
        param.setTeamCode(normalize(param.getTeamCode()));
        param.setTeamName(normalize(param.getTeamName()));
        param.setStatus(param.getStatus() == null || param.getStatus());
    }

    private void saveEmployees(Long teamId, List<TeamEmployeeDTO> employees) {
        if (CollectionUtils.isEmpty(employees)) {
            return;
        }
        Set<Long> employeeIds = new LinkedHashSet<>();
        for (TeamEmployeeDTO item : employees) {
            if (!employeeIds.add(item.getEmployeeId())) {
                continue;
            }
            BasicTeamEmployee entity = new BasicTeamEmployee();
            entity.setTeamId(teamId);
            entity.setEmployeeId(item.getEmployeeId());
            teamEmployeeMapper.insert(entity);
        }
    }

    private TeamDTO toDTO(BasicTeam team, boolean withEmployees) {
        if (team == null) {
            return null;
        }
        TeamDTO dto = new TeamDTO();
        BeanUtils.copyProperties(team, dto);
        fillNames(dto);
        if (withEmployees) {
            dto.setEmployeeList(queryEmployees(team.getId()));
        }
        return dto;
    }

    private void fillNames(TeamDTO dto) {
        BasicEmployee leader = dto.getLeaderEmployeeId() == null ? null : employeeMapper.selectById(dto.getLeaderEmployeeId());
        if (leader != null) {
            dto.setLeaderEmployeeNo(leader.getEmployeeNo());
            dto.setLeaderEmployeeName(leader.getEmployeeName());
        }
        BasicShift shift = dto.getCurrentShiftId() == null ? null : shiftMapper.selectById(dto.getCurrentShiftId());
        if (shift != null) {
            dto.setCurrentShiftName(shift.getShiftName());
        }
        BasicWorkshop workshop = dto.getWorkshopId() == null ? null : workshopMapper.selectById(dto.getWorkshopId());
        if (workshop != null) {
            dto.setWorkshopCode(workshop.getWorkshopCode());
            dto.setWorkshopName(workshop.getWorkshopName());
        }
    }

    private List<TeamEmployeeDTO> queryEmployees(Long teamId) {
        return teamEmployeeMapper.selectList(new LambdaQueryWrapper<BasicTeamEmployee>()
                        .eq(BasicTeamEmployee::getTeamId, teamId)
                        .eq(BasicTeamEmployee::getDeleted, false)
                        .orderByAsc(BasicTeamEmployee::getCreateTime))
                .stream()
                .map(this::toEmployeeDTO)
                .toList();
    }

    private TeamEmployeeDTO toEmployeeDTO(BasicTeamEmployee entity) {
        TeamEmployeeDTO dto = new TeamEmployeeDTO();
        BeanUtils.copyProperties(entity, dto);
        BasicEmployee employee = employeeMapper.selectById(entity.getEmployeeId());
        if (employee != null) {
            dto.setEmployeeNo(employee.getEmployeeNo());
            dto.setEmployeeName(employee.getEmployeeName());
        }
        return dto;
    }

    private BasicTeam requireTeam(Long id) {
        BasicTeam team = id == null ? null : teamMapper.selectById(id);
        if (team == null) {
            throw notFound();
        }
        return team;
    }

    private BasicTeam findByCode(String teamCode) {
        return teamMapper.selectOne(new LambdaQueryWrapper<BasicTeam>()
                .eq(BasicTeam::getTeamCode, normalize(teamCode))
                .eq(BasicTeam::getDeleted, false)
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
