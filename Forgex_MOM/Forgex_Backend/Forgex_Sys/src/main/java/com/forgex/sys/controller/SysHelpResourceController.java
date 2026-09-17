package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysHelpPageResolveDTO;
import com.forgex.sys.domain.dto.SysHelpResourceDTO;
import com.forgex.sys.domain.dto.SysHelpUploadResultDTO;
import com.forgex.sys.domain.entity.SysHelpResource;
import com.forgex.sys.domain.param.BatchIdsParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.param.SysHelpResourceListParam;
import com.forgex.sys.domain.param.SysHelpResourcePageParam;
import com.forgex.sys.domain.param.SysHelpResourceSaveParam;
import com.forgex.sys.domain.param.SysHelpResourceStatusParam;
import com.forgex.sys.service.ISysHelpResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 帮助资源控制器。
 * <p>
 * 管理端接口需要 {@code sys:help:*} 权限；运行时列表仅要求登录。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see ISysHelpResourceService
 */
@RestController
@RequestMapping("/help-resource")
@RequiredArgsConstructor
public class SysHelpResourceController {

    private final ISysHelpResourceService helpResourceService;

    /**
     * 分页查询帮助资源。
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @RequirePerm("sys:help:view")
    @PostMapping("/page")
    public R<IPage<SysHelpResourceDTO>> page(@RequestBody(required = false) SysHelpResourcePageParam param) {
        SysHelpResourcePageParam condition = param == null ? new SysHelpResourcePageParam() : param;
        Page<SysHelpResource> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(helpResourceService.pageResources(page, condition));
    }

    /**
     * 新增帮助资源。
     *
     * @param param 保存参数
     * @return 新记录 ID
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.ADD, detailTemplateCode = "HELP_RESOURCE_CREATE")
    @RequirePerm("sys:help:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody SysHelpResourceSaveParam param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, helpResourceService.createResource(param));
    }

    /**
     * 更新帮助资源。
     *
     * @param param 保存参数
     * @return 处理结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.UPDATE, detailTemplateCode = "HELP_RESOURCE_UPDATE")
    @RequirePerm("sys:help:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody SysHelpResourceSaveParam param) {
        helpResourceService.updateResource(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 删除帮助资源。
     *
     * @param param 主键参数
     * @return 处理结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.DELETE, detailTemplateCode = "HELP_RESOURCE_DELETE")
    @RequirePerm("sys:help:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        helpResourceService.deleteResource(param == null ? null : param.getId());
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 批量删除帮助资源。
     *
     * @param param 主键列表
     * @return 处理结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.DELETE, detailTemplateCode = "HELP_RESOURCE_BATCH_DELETE")
    @RequirePerm("sys:help:delete")
    @PostMapping("/batch-delete")
    public R<Void> batchDelete(@RequestBody BatchIdsParam param) {
        helpResourceService.deleteResources(param == null ? null : param.getIds());
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 启停帮助资源。
     *
     * @param param 状态参数
     * @return 处理结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.UPDATE, detailTemplateCode = "HELP_RESOURCE_STATUS")
    @RequirePerm("sys:help:edit")
    @PostMapping("/change-status")
    public R<Void> changeStatus(@RequestBody SysHelpResourceStatusParam param) {
        helpResourceService.changeStatus(param == null ? null : param.getId(), param == null ? null : param.getStatus());
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 上传帮助文件。
     *
     * @param file 文件
     * @param docType 文档类型或联系模块编码
     * @return 上传结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.ADD, detailTemplateCode = "HELP_RESOURCE_UPLOAD")
    @RequirePerm("sys:help:add")
    @PostMapping("/upload")
    public R<SysHelpUploadResultDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docType", required = false) String docType
    ) {
        return R.ok(helpResourceService.upload(file, docType));
    }

    /**
     * 按当前页解析启用中的手册或视频。
     *
     * @param param 解析参数
     * @return 覆盖解析结果
     */
    @PostMapping("/list-for-page")
    public R<SysHelpPageResolveDTO> listForPage(@RequestBody SysHelpResourceListParam param) {
        return R.ok(helpResourceService.listForPage(param));
    }
}
