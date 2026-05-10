package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysAndroidVersionDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionQueryDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadCompleteDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadInitDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadStatusDTO;
import com.forgex.sys.domain.entity.SysAndroidVersion;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.SysAndroidVersionUploadTaskVO;
import com.forgex.sys.domain.vo.SysAndroidVersionVO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.ISysAndroidVersionService;
import com.forgex.sys.service.ISysAndroidVersionUploadTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Android version management controller.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/android-version")
@RequiredArgsConstructor
public class SysAndroidVersionController {

    private final ISysAndroidVersionService androidVersionService;
    private final ISysAndroidVersionUploadTaskService uploadTaskService;

    @RequirePerm("sys:androidVersion:view")
    @PostMapping("/page")
    public R<IPage<SysAndroidVersionVO>> page(@RequestBody SysAndroidVersionQueryDTO query) {
        SysAndroidVersionQueryDTO condition = query == null ? new SysAndroidVersionQueryDTO() : query;
        Page<SysAndroidVersion> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(androidVersionService.pageVersions(page, condition));
    }

    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload")
    public R<SysAndroidVersionVO> upload(
            MultipartFile file,
            Integer versionCode,
            String versionName,
            String changelog
    ) {
        if (file == null || file.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        try {
            SysAndroidVersionDTO dto = new SysAndroidVersionDTO();
            dto.setVersionCode(versionCode);
            dto.setVersionName(versionName);
            dto.setChangelog(changelog);
            SysAndroidVersionVO vo = androidVersionService.uploadApk(file, dto);
            return R.ok(SysPromptEnum.CONFIG_CREATE_SUCCESS, vo);
        } catch (IOException e) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, e.getMessage());
        }
    }

    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload/init")
    public R<SysAndroidVersionUploadTaskVO> initUpload(@RequestBody SysAndroidVersionUploadInitDTO dto) throws IOException {
        return R.ok(uploadTaskService.initUpload(dto));
    }

    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload/chunk")
    public R<SysAndroidVersionUploadTaskVO> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunk") MultipartFile chunk
    ) throws IOException {
        return R.ok(uploadTaskService.uploadChunk(uploadId, chunkIndex, chunk));
    }

    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload/status")
    public R<SysAndroidVersionUploadTaskVO> uploadStatus(@RequestBody SysAndroidVersionUploadStatusDTO dto) {
        if (dto == null || dto.getUploadId() == null || dto.getUploadId().isBlank()) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        return R.ok(uploadTaskService.getUploadStatus(dto.getUploadId()));
    }

    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload/complete")
    public R<SysAndroidVersionVO> completeUpload(@RequestBody SysAndroidVersionUploadCompleteDTO dto) throws IOException {
        return R.ok(SysPromptEnum.CONFIG_CREATE_SUCCESS, uploadTaskService.completeUpload(dto));
    }

    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload/cancel")
    public R<Void> cancelUpload(@RequestBody SysAndroidVersionUploadStatusDTO dto) throws IOException {
        if (dto == null || dto.getUploadId() == null || dto.getUploadId().isBlank()) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        uploadTaskService.cancelUpload(dto.getUploadId());
        return R.ok(SysPromptEnum.CONFIG_DELETE_SUCCESS);
    }

    @RequirePerm("sys:androidVersion:edit")
    @PostMapping("/update")
    public R<SysAndroidVersionVO> update(@RequestBody SysAndroidVersionDTO dto) {
        return R.ok(SysPromptEnum.CONFIG_UPDATE_SUCCESS, androidVersionService.updateVersion(dto));
    }

    @RequirePerm("sys:androidVersion:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.ID_EMPTY);
        }
        androidVersionService.deleteVersion(param.getId());
        return R.ok(SysPromptEnum.CONFIG_DELETE_SUCCESS);
    }

    @PostMapping("/latest")
    public R<SysAndroidVersionVO> latest() {
        return R.ok(androidVersionService.getLatestVersion());
    }
}
