package com.forgex.sys.service.storage;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 本地文件存储服务实现类
 * <p>提供基于本地文件系统的文件存储功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
public class LocalStorageService implements FileStorageService {

    /**
     * 文件上传路径
     */
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 文件访问前缀
     */
    @Value("${file.access.prefix:/files}")
    private String accessPrefix;

    /**
     * 上传文件
     * <p>将文件保存到本地存储目录，并返回相对路径。</p>
     * 
     * @param file 上传的文件
     * @return 文件相对路径，上传失败返回null
     * @throws IOException 文件处理异常
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        String relativePath = fileName;

        File targetDir = new File(uploadPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File targetFile = new File(targetDir, fileName);
        file.transferTo(targetFile);

        return relativePath;
    }

    @Override
    public void download(String filePath, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(filePath)) {
            throw new IOException("文件路径为空");
        }
        File file = new File(uploadPath, filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    @Override
    public boolean delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        File file = new File(uploadPath, filePath);
        return file.exists() && file.delete();
    }

    /**
     * 获取文件访问URL
     * 
     * @param filePath 文件相对路径
     * @return 文件访问URL，路径为空时返回null
     */
    @Override
    public String getUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        String prefix = StringUtils.hasText(accessPrefix) ? accessPrefix : "/files";
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix + "/" + filePath;
    }
}
