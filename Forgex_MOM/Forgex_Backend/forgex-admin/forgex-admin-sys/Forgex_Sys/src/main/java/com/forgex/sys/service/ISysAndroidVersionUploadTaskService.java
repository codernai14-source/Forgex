package com.forgex.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadCompleteDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionUploadInitDTO;
import com.forgex.sys.domain.entity.SysAndroidVersionUploadTask;
import com.forgex.sys.domain.vo.SysAndroidVersionUploadTaskVO;
import com.forgex.sys.domain.vo.SysAndroidVersionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Android APK chunk upload task service.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
public interface ISysAndroidVersionUploadTaskService extends IService<SysAndroidVersionUploadTask> {

    SysAndroidVersionUploadTaskVO initUpload(SysAndroidVersionUploadInitDTO dto) throws IOException;

    SysAndroidVersionUploadTaskVO uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) throws IOException;

    SysAndroidVersionUploadTaskVO getUploadStatus(String uploadId);

    SysAndroidVersionVO completeUpload(SysAndroidVersionUploadCompleteDTO dto) throws IOException;

    void cancelUpload(String uploadId) throws IOException;

    void cleanupExpiredTasks();
}
