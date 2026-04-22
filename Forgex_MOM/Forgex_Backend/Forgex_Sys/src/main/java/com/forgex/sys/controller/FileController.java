package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysFileRecordQueryDTO;
import com.forgex.sys.domain.entity.SysFileRecord;
import com.forgex.sys.domain.vo.SysFileRecordVO;
import com.forgex.sys.service.FileService;
import com.forgex.sys.service.ISysFileRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * File controller.
 */
@RestController
@RequestMapping("/sys/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final ISysFileRecordService fileRecordService;

    @PostMapping("/upload")
    public R<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "moduleCode", required = false) String moduleCode,
            @RequestParam(value = "moduleName", required = false) String moduleName
    ) {
        if (file.isEmpty()) {
            return R.fail(CommonPrompt.FILE_EMPTY);
        }

        try {
            String fileUrl = fileService.upload(file, moduleCode, moduleName);
            return R.ok(CommonPrompt.UPLOAD_SUCCESS, fileUrl);
        } catch (IOException e) {
            return R.fail(CommonPrompt.FILE_UPLOAD_FAILED);
        }
    }

    @RequirePerm("sys:file:view")
    @PostMapping("/page")
    public R<IPage<SysFileRecordVO>> page(@RequestBody SysFileRecordQueryDTO query) {
        Page<SysFileRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(fileRecordService.pageRecords(page, query));
    }
}
