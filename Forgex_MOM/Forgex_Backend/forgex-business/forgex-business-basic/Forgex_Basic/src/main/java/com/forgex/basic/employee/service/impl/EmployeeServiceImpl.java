package com.forgex.basic.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.employee.domain.dto.EmployeeDTO;
import com.forgex.basic.employee.domain.dto.EmployeeSyncUserResultDTO;
import com.forgex.basic.employee.domain.entity.BasicEmployee;
import com.forgex.basic.employee.domain.param.EmployeePageParam;
import com.forgex.basic.employee.mapper.BasicEmployeeMapper;
import com.forgex.basic.employee.service.IEmployeeService;
import com.forgex.basic.team.domain.entity.BasicTeamEmployee;
import com.forgex.basic.team.mapper.BasicTeamEmployeeMapper;
import com.forgex.basic.platform.SysPlatformSupport;
import com.forgex.common.api.dto.SysEmployeeUserRequest;
import com.forgex.common.api.dto.SysEmployeeUserResult;
import com.forgex.common.api.dto.SysEmployeeReferenceRequest;
import com.forgex.common.api.dto.SysEmployeeReferenceDTO;
import com.forgex.common.api.dto.EmployeeThirdPartyInvokeDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncResultDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncDTO;
import com.forgex.common.api.feign.IntegrationInternalEmployeeFeignClient;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 人员主数据服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<BasicEmployeeMapper, BasicEmployee> implements IEmployeeService {

    private static final String DEFAULT_EMPLOYEE_SYNC_API_CODE = "basic_employee_sync";
    private static final String DEFAULT_EMPLOYEE_PULL_API_CODE = "basic_employee_pull";

    private final BasicEmployeeMapper employeeMapper;
    private final BasicTeamEmployeeMapper teamEmployeeMapper;
    private final SysPlatformSupport platformSupport;
    private final IntegrationInternalEmployeeFeignClient integrationInternalEmployeeFeignClient;

    @Override
    public Page<EmployeeDTO> page(EmployeePageParam param) {
        EmployeePageParam safeParam = param == null ? new EmployeePageParam() : param;
        Page<BasicEmployee> entityPage = employeeMapper.selectPage(
                new Page<>(safeParam.getPageNum(), safeParam.getPageSize()),
                buildWrapper(safeParam));
        Page<EmployeeDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(toDTOs(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public List<EmployeeDTO> list(EmployeePageParam param) {
        return toDTOs(employeeMapper.selectList(buildWrapper(param == null ? new EmployeePageParam() : param)));
    }

    @Override
    public EmployeeDTO detail(Long id) {
        return id == null ? null : toDTO(employeeMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BasicEmployee param) {
        validateSave(param, true);
        fillBeforeSave(param);
        employeeMapper.insert(param);
        return param.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BasicEmployee param) {
        validateSave(param, false);
        BasicEmployee existing = requireEmployee(param.getId());
        if (!Objects.equals(normalize(existing.getEmployeeNo()), normalize(param.getEmployeeNo()))) {
            throw badRequest("工号创建后不可修改");
        }
        fillBeforeSave(param);
        param.setEmployeeNo(existing.getEmployeeNo());
        employeeMapper.updateById(param);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireEmployee(id);
        assertNoTeamReference(Collections.singletonList(id));
        employeeMapper.deleteById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        assertNoTeamReference(ids);
        employeeMapper.deleteBatchIds(ids);
        return true;
    }

    @Override
    public EmployeeSyncUserResultDTO syncUser(Long id) {
        return batchSyncUser(Collections.singletonList(id));
    }

    @Override
    public EmployeeSyncUserResultDTO batchSyncUser(List<Long> ids) {
        EmployeeSyncUserResultDTO result = new EmployeeSyncUserResultDTO();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        List<BasicEmployee> employees = employeeMapper.selectBatchIds(ids);
        result.setTotalCount(employees.size());
        for (BasicEmployee employee : employees) {
            try {
                upsertUser(employee, result);
            } catch (Exception ex) {
                log.error("同步人员到用户失败，employeeNo={}", employee == null ? null : employee.getEmployeeNo(), ex);
                result.getFailedEmployeeNos().add(employee == null ? "UNKNOWN" : employee.getEmployeeNo());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeThirdPartySyncResultDTO syncThirdPartyEmployees(EmployeeThirdPartySyncRequestDTO request) {
        EmployeeThirdPartySyncResultDTO result = new EmployeeThirdPartySyncResultDTO();
        List<EmployeeThirdPartySyncDTO> employees = request == null ? Collections.emptyList() : request.getEmployees();
        result.setTotalCount(employees == null ? 0 : employees.size());
        if (CollectionUtils.isEmpty(employees)) {
            return result;
        }
        for (EmployeeThirdPartySyncDTO item : employees) {
            String employeeNo = item == null ? null : normalize(item.getEmployeeNo());
            try {
                if (item == null || !StringUtils.hasText(employeeNo) || !StringUtils.hasText(item.getEmployeeName())) {
                    throw badRequest("工号和姓名不能为空");
                }
                BasicEmployee existing = findByNo(employeeNo);
                BasicEmployee employee = new BasicEmployee();
                BeanUtils.copyProperties(item, employee);
                fillBeforeSave(employee);
                if (existing == null) {
                    employeeMapper.insert(employee);
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    employee.setId(existing.getId());
                    employeeMapper.updateById(employee);
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                log.error("同步第三方人员失败，employeeNo={}", employeeNo, ex);
                result.setFailedCount(result.getFailedCount() + 1);
                result.getFailedEmployeeNos().add(StringUtils.hasText(employeeNo) ? employeeNo : "UNKNOWN");
            }
        }
        return result;
    }

    @Override
    public List<EmployeeThirdPartySyncDTO> exportThirdPartyEmployees(EmployeeThirdPartySyncRequestDTO request) {
        return employeeMapper.selectList(new LambdaQueryWrapper<BasicEmployee>()
                        .eq(BasicEmployee::getDeleted, false)
                        .orderByDesc(BasicEmployee::getCreateTime))
                .stream()
                .map(this::toThirdPartyDTO)
                .toList();
    }

    @Override
    public EmployeeThirdPartySyncResultDTO syncToThirdParty(EmployeeThirdPartyInvokeDTO request) {
        EmployeeThirdPartyInvokeDTO safeRequest = request == null ? new EmployeeThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_EMPLOYEE_SYNC_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(Optional.ofNullable(TenantContext.get()).orElse(0L));
        }
        R<EmployeeThirdPartySyncResultDTO> response = integrationInternalEmployeeFeignClient.syncEmployees(safeRequest);
        if (response == null || response.getData() == null) {
            throw badRequest("人员第三方同步失败");
        }
        return response.getData();
    }

    @Override
    public EmployeeThirdPartySyncResultDTO pullFromThirdParty(EmployeeThirdPartyInvokeDTO request) {
        EmployeeThirdPartyInvokeDTO safeRequest = request == null ? new EmployeeThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_EMPLOYEE_PULL_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(Optional.ofNullable(TenantContext.get()).orElse(0L));
        }
        R<EmployeeThirdPartySyncResultDTO> response = integrationInternalEmployeeFeignClient.pullEmployees(safeRequest);
        if (response == null || response.getData() == null) {
            throw badRequest("人员第三方拉取失败");
        }
        return response.getData();
    }

    private LambdaQueryWrapper<BasicEmployee> buildWrapper(EmployeePageParam param) {
        return new LambdaQueryWrapper<BasicEmployee>()
                .eq(BasicEmployee::getDeleted, false)
                .like(StringUtils.hasText(param.getEmployeeNo()), BasicEmployee::getEmployeeNo, param.getEmployeeNo())
                .like(StringUtils.hasText(param.getEmployeeName()), BasicEmployee::getEmployeeName, param.getEmployeeName())
                .like(StringUtils.hasText(param.getPhone()), BasicEmployee::getPhone, param.getPhone())
                .eq(param.getDepartmentId() != null, BasicEmployee::getDepartmentId, param.getDepartmentId())
                .eq(param.getPositionId() != null, BasicEmployee::getPositionId, param.getPositionId())
                .eq(param.getStatus() != null, BasicEmployee::getStatus, param.getStatus())
                .orderByDesc(BasicEmployee::getCreateTime);
    }

    private void validateSave(BasicEmployee param, boolean create) {
        if (param == null || !StringUtils.hasText(param.getEmployeeNo()) || !StringUtils.hasText(param.getEmployeeName())) {
            throw badRequest("工号和姓名不能为空");
        }
        if (!create && param.getId() == null) {
            throw notFound();
        }
        BasicEmployee same = findByNo(param.getEmployeeNo());
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw alreadyExists("工号已存在");
        }
    }

    private void fillBeforeSave(BasicEmployee param) {
        param.setEmployeeNo(normalize(param.getEmployeeNo()));
        param.setEmployeeName(normalize(param.getEmployeeName()));
        param.setStatus(param.getStatus() == null || param.getStatus());
    }

    private void upsertUser(BasicEmployee employee, EmployeeSyncUserResultDTO result) {
        if (employee == null || !StringUtils.hasText(employee.getEmployeeNo())) {
            throw badRequest("工号不能为空");
        }
        // 用户与租户绑定在 Sys 的同一事务中写入。Basic 此步骤不写本地表，超时可按工号和人员 ID 重试。
        SysEmployeeUserResult synced = platformSupport.syncEmployeeUser(new SysEmployeeUserRequest(
                employee.getId(), resolveTenantId(employee), employee.getEmployeeNo(), employee.getEmployeeName(),
                employee.getPhone(), employee.getEmail(), employee.getGender(), employee.getAvatar(),
                employee.getEntryDate(), employee.getDepartmentId(), employee.getPositionId(), employee.getStatus()));
        if (synced.created()) {
            result.setCreatedCount(result.getCreatedCount() + 1);
        } else {
            result.setUpdatedCount(result.getUpdatedCount() + 1);
        }
    }

    private EmployeeDTO toDTO(BasicEmployee employee) {
        return employee == null ? null : toDTOs(List.of(employee)).get(0);
    }

    private List<EmployeeDTO> toDTOs(List<BasicEmployee> employees) {
        if (employees.isEmpty()) {
            return List.of();
        }
        // 一次批量查询，避免列表中每位人员分别发出远程请求。
        java.util.Map<Long, SysEmployeeReferenceDTO> references = platformSupport.employeeReferences(
                employees.stream().map(employee -> new SysEmployeeReferenceRequest(employee.getId(),
                        employee.getDepartmentId(), employee.getPositionId())).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(SysEmployeeReferenceDTO::employeeId,
                        java.util.function.Function.identity(), (first, ignored) -> first));
        return employees.stream().map(employee -> {
            EmployeeDTO dto = new EmployeeDTO();
            BeanUtils.copyProperties(employee, dto);
            SysEmployeeReferenceDTO reference = references.get(employee.getId());
            if (reference != null) {
                dto.setDepartmentName(reference.departmentName());
                dto.setPositionName(reference.positionName());
                dto.setUserId(reference.userId());
            }
            return dto;
        }).toList();
    }

    private EmployeeThirdPartySyncDTO toThirdPartyDTO(BasicEmployee employee) {
        EmployeeThirdPartySyncDTO dto = new EmployeeThirdPartySyncDTO();
        BeanUtils.copyProperties(employee, dto);
        return dto;
    }

    private void assertNoTeamReference(List<Long> employeeIds) {
        Long count = teamEmployeeMapper.selectCount(new LambdaQueryWrapper<BasicTeamEmployee>()
                .in(BasicTeamEmployee::getEmployeeId, employeeIds)
                .eq(BasicTeamEmployee::getDeleted, false));
        if (count != null && count > 0) {
            throw badRequest("人员已被班组引用，无法删除");
        }
    }

    private BasicEmployee requireEmployee(Long id) {
        BasicEmployee employee = id == null ? null : employeeMapper.selectById(id);
        if (employee == null) {
            throw notFound();
        }
        return employee;
    }

    private BasicEmployee findByNo(String employeeNo) {
        return employeeMapper.selectOne(new LambdaQueryWrapper<BasicEmployee>()
                .eq(BasicEmployee::getEmployeeNo, normalize(employeeNo))
                .eq(BasicEmployee::getDeleted, false)
                .last("LIMIT 1"));
    }

    private Long resolveTenantId(BasicEmployee employee) {
        return Optional.ofNullable(employee.getTenantId()).orElse(Optional.ofNullable(TenantContext.get()).orElse(0L));
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
