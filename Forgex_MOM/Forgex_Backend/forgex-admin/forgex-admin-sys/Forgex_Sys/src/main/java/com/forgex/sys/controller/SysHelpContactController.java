package com.forgex.sys.controller;

import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysHelpContactDTO;
import com.forgex.sys.domain.dto.SysHelpUploadResultDTO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.ISysHelpResourceService;
import com.forgex.sys.service.help.HelpResourceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 帮助中心联系方式控制器。
 * <p>
 * 读取仅要求登录；写入需要 {@code sys:help:contact:edit}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see ISysHelpResourceService
 */
@RestController
@RequestMapping("/help-contact")
@RequiredArgsConstructor
public class SysHelpContactController {

    private final ISysHelpResourceService helpResourceService;

    /**
     * 读取联系方式。
     *
     * @return 联系配置
     */
    @PostMapping("/get")
    public R<SysHelpContactDTO> get() {
        return R.ok(helpResourceService.getContact());
    }

    /**
     * 保存联系方式。
     *
     * @param contact 联系配置
     * @return 处理结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.UPDATE, detailTemplateCode = "HELP_CONTACT_SAVE")
    @RequirePerm("sys:help:contact:edit")
    @PostMapping("/save")
    public R<Void> save(@RequestBody SysHelpContactDTO contact) {
        helpResourceService.saveContact(contact);
        return R.ok(SysPromptEnum.HELP_CONTACT_SAVE_SUCCESS);
    }

    /**
     * 上传联系二维码。
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @OperationLog(module = "sys", menuPath = "/system/helpResource", operationType = OperationType.UPDATE, detailTemplateCode = "HELP_CONTACT_QR_UPLOAD")
    @RequirePerm("sys:help:contact:edit")
    @PostMapping("/upload-qr")
    public R<SysHelpUploadResultDTO> uploadQr(@RequestParam("file") MultipartFile file) {
        return R.ok(helpResourceService.upload(file, HelpResourceSupport.MODULE_CONTACT));
    }
}
