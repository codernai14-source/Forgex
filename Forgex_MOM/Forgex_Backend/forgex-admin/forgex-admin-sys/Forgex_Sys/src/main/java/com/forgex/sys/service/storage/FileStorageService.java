package com.forgex.sys.service.storage;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口
 * <p>定义文件存储的标准操作接口。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface FileStorageService {
    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @return 文件相对路径
     * @throws IOException 文件处理异常
     */
    String upload(MultipartFile file) throws IOException;

    /**
     * 按指定体积上限上传文件。
     * <p>
     * 帮助中心视频等场景需要高于全局 {@code file.upload.settings.maxSizeMb} 的上限。
     * 默认实现忽略覆盖值，仍走 {@link #upload(MultipartFile)}。
     * </p>
     *
     * @param file 上传文件
     * @param maxSizeMbOverride 覆盖体积上限（MB）；为空时使用存储实现默认配置
     * @return 文件相对路径
     * @throws IOException 文件处理异常
     */
    default String upload(MultipartFile file, Long maxSizeMbOverride) throws IOException {
        return upload(file);
    }

    /**
     * 下载文件
     * 
     * @param filePath 文件相对路径
     * @param response HTTP响应对象
     * @throws IOException 文件处理异常
     */
    void download(String filePath, HttpServletResponse response) throws IOException;

    /**
     * 删除文件
     * 
     * @param filePath 文件相对路径
     * @return true表示删除成功，false表示删除失败
     */
    boolean delete(String filePath);

    /**
     * 获取文件访问URL
     * 
     * @param filePath 文件相对路径
     * @return 文件访问URL
     */
    String getUrl(String filePath);
}

