package com.forgex.basic.platform;

import com.forgex.common.api.dto.SysDictValueRequest;
import com.forgex.common.api.dto.SysEmployeeReferenceDTO;
import com.forgex.common.api.dto.SysEmployeeReferenceRequest;
import com.forgex.common.api.dto.SysEmployeeUserRequest;
import com.forgex.common.api.dto.SysEmployeeUserResult;
import com.forgex.common.api.dto.SysModuleSummaryDTO;
import com.forgex.common.api.feign.SysBasicSupportFeignClient;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基础资料的平台访问门面，统一校验远程结果，防止失败响应被当作有效业务数据。
 */
@Service
@RequiredArgsConstructor
public class SysPlatformSupport {

    private final SysBasicSupportFeignClient client;

    /**
     * 同步一个人员的用户与租户绑定。
     * @param request 人员信息
     * @return 已提交的用户结果
     */
    public SysEmployeeUserResult syncEmployeeUser(SysEmployeeUserRequest request) {
        return requireData(client.syncEmployeeUser(request));
    }

    /**
     * 批量读取人员引用信息。
     * @param requests 查询项
     * @return 平台引用信息
     */
    public List<SysEmployeeReferenceDTO> employeeReferences(List<SysEmployeeReferenceRequest> requests) {
        return requests.isEmpty() ? List.of() : requireData(client.employeeReferences(requests));
    }

    /**
     * 校验字典项。
     * @param request 字典条件
     * @return 是否允许使用
     */
    public boolean dictValueValid(SysDictValueRequest request) {
        return requireData(client.dictValueValid(request));
    }

    /**
     * 获取平台模块。
     * @param moduleId 模块 ID
     * @return 模块摘要，不存在时为 null
     */
    public SysModuleSummaryDTO getModuleById(Long moduleId) {
        return moduleId == null ? null : requireSuccess(client.module(moduleId)).getData();
    }

    private <T> T requireData(R<T> response) {
        T data = requireSuccess(response).getData();
        if (data == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
        }
        return data;
    }

    private <T> R<T> requireSuccess(R<T> response) {
        if (response == null || !Integer.valueOf(StatusCode.SUCCESS).equals(response.getCode())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
        }
        return response;
    }
}
