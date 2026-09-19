package com.forgex.sys.service.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.api.dto.SysDictValueRequest;
import com.forgex.common.api.dto.SysEmployeeReferenceDTO;
import com.forgex.common.api.dto.SysEmployeeReferenceRequest;
import com.forgex.common.api.dto.SysModuleSummaryDTO;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysDict;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysDictMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础资料需要的平台只读查询。显式限定租户，防止全局用户表泄露其他租户关联。
 */
@Service
@RequiredArgsConstructor
public class BasicPlatformReferenceService {

    private final SysDepartmentMapper departmentMapper;
    private final SysPositionMapper positionMapper;
    private final SysUserMapper userMapper;
    private final SysDictMapper dictMapper;
    private final SysModuleMapper moduleMapper;
    private final BasicPlatformRequestGuard requestGuard;

    /**
     * 批量查询人员引用，数据库查询次数不随人员行数增加。
     * @param requests 人员查询项
     * @return 关联信息
     */
    public List<SysEmployeeReferenceDTO> employeeReferences(List<SysEmployeeReferenceRequest> requests) {
        Long tenantId = requestGuard.requireTenant();
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        if (requests.stream().anyMatch(item -> item == null || item.employeeId() == null)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        List<Long> departmentIds = requests.stream().map(SysEmployeeReferenceRequest::departmentId)
                .filter(Objects::nonNull).distinct().toList();
        List<Long> positionIds = requests.stream().map(SysEmployeeReferenceRequest::positionId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, SysDepartment> departments = departmentIds.isEmpty() ? Map.of()
                : departmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                    .in(SysDepartment::getId, departmentIds).eq(SysDepartment::getTenantId, tenantId)).stream()
                    .collect(Collectors.toMap(SysDepartment::getId, Function.identity()));
        Map<Long, SysPosition> positions = positionIds.isEmpty() ? Map.of()
                : positionMapper.selectList(new LambdaQueryWrapper<SysPosition>()
                    .in(SysPosition::getId, positionIds).eq(SysPosition::getTenantId, tenantId)).stream()
                    .collect(Collectors.toMap(SysPosition::getId, Function.identity()));
        Map<Long, Long> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getEmployeeId)
                .in(SysUser::getEmployeeId, requests.stream().map(SysEmployeeReferenceRequest::employeeId).toList())
                .eq(SysUser::getTenantId, tenantId)).stream()
                .collect(Collectors.toMap(SysUser::getEmployeeId, SysUser::getId, (first, ignored) -> first));
        return requests.stream().map(item -> new SysEmployeeReferenceDTO(item.employeeId(),
                item.departmentId() != null && departments.containsKey(item.departmentId())
                        ? departments.get(item.departmentId()).getDeptName() : null,
                item.positionId() != null && positions.containsKey(item.positionId())
                        ? positions.get(item.positionId()).getPositionName() : null,
                users.get(item.employeeId()))).toList();
    }

    /**
     * 校验包装等业务所使用的字典值，保留未配置字典时允许保存的行为。
     * @param request 字典条件
     * @return 是否有效
     */
    public boolean dictValueValid(SysDictValueRequest request) {
        Long tenantId = requestGuard.requireTenant();
        if (request == null || !Objects.equals(request.tenantId(), tenantId)
                || !StringUtils.hasText(request.dictCode()) || !StringUtils.hasText(request.dictValue())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        SysDict root = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, request.dictCode()).eq(SysDict::getDeleted, false)
                .in(SysDict::getTenantId, 0L, tenantId).last("LIMIT 1"));
        if (root == null) {
            return true;
        }
        return dictMapper.selectCount(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, root.getId()).eq(SysDict::getDictValue, request.dictValue())
                .eq(SysDict::getStatus, 1).eq(SysDict::getDeleted, false)
                .in(SysDict::getTenantId, root.getTenantId() == null ? 0L : root.getTenantId(), tenantId)) > 0;
    }

    /**
     * 查询当前租户的模块摘要。
     * @param moduleId 模块 ID
     * @return 摘要，不存在时返回 null
     */
    public SysModuleSummaryDTO module(Long moduleId) {
        Long tenantId = requestGuard.requireTenant();
        SysModule module = moduleId == null ? null : moduleMapper.selectById(moduleId);
        return module == null || !Objects.equals(module.getTenantId(), tenantId) || Boolean.TRUE.equals(module.getDeleted())
                ? null : new SysModuleSummaryDTO(module.getId(), module.getName());
    }
}
