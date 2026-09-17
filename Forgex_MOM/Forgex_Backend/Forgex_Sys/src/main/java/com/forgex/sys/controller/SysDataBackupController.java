package com.forgex.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysDataBackupRecord;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.IDataBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据备份管理接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/backup")
@RequiredArgsConstructor
public class SysDataBackupController {

    private final IDataBackupService dataBackupService;

    /**
     * 分页查询备份记录。
     *
     * @param body current/size
     * @return 分页结果
     */
    @RequirePerm("sys:backup:view")
    @PostMapping("/page")
    public R<Page<SysDataBackupRecord>> page(@RequestBody Map<String, Object> body) {
        long current = body == null || body.get("current") == null ? 1L : Long.parseLong(String.valueOf(body.get("current")));
        long size = body == null || body.get("size") == null ? 20L : Long.parseLong(String.valueOf(body.get("size")));
        return R.ok(dataBackupService.page(current, size));
    }

    /**
     * 手动触发全量备份。
     *
     * @return 备份记录
     */
    @OperationLog(module = "sys", menuPath = "/system/backup", operationType = OperationType.GENERATE, detailTemplateCode = "BACKUP_RUN")
    @RequirePerm("sys:backup:run")
    @PostMapping("/run")
    public R<SysDataBackupRecord> run() {
        return R.ok(SysPromptEnum.BACKUP_TRIGGERED, dataBackupService.triggerFullBackup(CurrentUserUtils.getAccount()));
    }
}
