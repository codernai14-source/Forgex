package com.forgex.basic.employee.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.employee.domain.dto.EmployeeDTO;
import com.forgex.basic.employee.domain.dto.EmployeeSyncUserResultDTO;
import com.forgex.basic.employee.domain.entity.BasicEmployee;
import com.forgex.basic.employee.domain.param.EmployeePageParam;
import com.forgex.common.api.dto.EmployeeThirdPartyInvokeDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncResultDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncDTO;

import java.util.List;

/**
 * 人员主数据服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
public interface IEmployeeService extends IService<BasicEmployee> {

    Page<EmployeeDTO> page(EmployeePageParam param);

    List<EmployeeDTO> list(EmployeePageParam param);

    EmployeeDTO detail(Long id);

    Long create(BasicEmployee param);

    Boolean update(BasicEmployee param);

    Boolean delete(Long id);

    Boolean batchDelete(List<Long> ids);

    EmployeeSyncUserResultDTO syncUser(Long id);

    EmployeeSyncUserResultDTO batchSyncUser(List<Long> ids);

    EmployeeThirdPartySyncResultDTO syncThirdPartyEmployees(EmployeeThirdPartySyncRequestDTO request);

    List<EmployeeThirdPartySyncDTO> exportThirdPartyEmployees(EmployeeThirdPartySyncRequestDTO request);

    EmployeeThirdPartySyncResultDTO syncToThirdParty(EmployeeThirdPartyInvokeDTO request);

    EmployeeThirdPartySyncResultDTO pullFromThirdParty(EmployeeThirdPartyInvokeDTO request);
}
