/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO文件存储服务实现类
 * <p>提供基于MinIO的文件存储功能。</p>
 * 
 * <p>配置JSON格式：</p>
 * <pre>
 * {
 *   "endpoint": "http://localhost:9000",
 *   "accessKey": "minioadmin",
 *   "secretKey": "minioadmin",
 *   "bucketName": "forgex",
 *   "domain": "http://localhost:9000"
 * }
 * </pre>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
public class MinioStorageService implements FileStorageService {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
    private final String domain;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数
     * 
     * @param configJson MinIO配置JSON字符串
     * @throws IOException 配置解析异常
     */
    public MinioStorageService(String configJson) throws IOException {
        if (!StringUtils.hasText(configJson)) {
            throw new IllegalArgumentException("MinIO配置不能为空");
        }
        
        JsonNode config = objectMapper.readTree(configJson);
        this.endpoint = config.get("endpoint").asText();
        this.accessKey = config.get("accessKey").asText();
        this.secretKey = config.get("secretKey").asText();
        this.bucketName = config.get("bucketName").asText();
        this.domain = config.has("domain") ? config.get("domain").asText() : null;
        
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(accessKey) 
                || !StringUtils.hasText(secretKey) || !StringUtils.hasText(bucketName)) {
            throw new IllegalArgumentException("MinIO配置不完整，缺少必要参数");
        }
    }

    /**
     * 创建MinIO客户端
     * 
     * @return MinIO客户端实例
     */
    private MinioClient createMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 确保Bucket存在
     * 
     * @param minioClient MinIO客户端
     * @throws Exception 异常
     */
    private void ensureBucketExists(MinioClient minioClient) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            log.info("创建Bucket成功: {}", bucketName);
        }
    }

    /**
     * 生成文件存储路径
     * <p>格式：yyyy/MM/dd/uuid.ext</p>
     * 
     * @param originalFilename 原始文件名
     * @return 文件存储路径
     */
    private String generateFilePath(String originalFilename) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        return date + "/" + fileName;
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        String filePath = generateFilePath(file.getOriginalFilename());
        MinioClient minioClient = createMinioClient();
        
        try {
            // 确保Bucket存在
            ensureBucketExists(minioClient);
            
            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            log.info("文件上传成功: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("MinIO文件上传失败: {}", e.getMessage(), e);
            throw new IOException("MinIO文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void download(String filePath, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(filePath)) {
            throw new IOException("文件路径为空");
        }
        
        MinioClient minioClient = createMinioClient();
        InputStream inputStream = null;
        
        try {
            // 获取文件
            inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            
            // 获取文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            
            // 写入响应流
            try (OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            
            log.info("文件下载成功: {}", filePath);
        } catch (Exception e) {
            log.error("MinIO文件下载失败: {}", e.getMessage(), e);
            throw new IOException("MinIO文件下载失败: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("关闭输入流失败", e);
                }
            }
        }
    }

    @Override
    public boolean delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        
        MinioClient minioClient = createMinioClient();
        
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            
            log.info("文件删除成功: {}", filePath);
            return true;
        } catch (Exception e) {
            log.error("MinIO文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        
        // 如果配置了自定义域名，使用自定义域名
        if (StringUtils.hasText(domain)) {
            String url = domain;
            if (!url.endsWith("/")) {
                url += "/";
            }
            return url + bucketName + "/" + filePath;
        }
        
        // 否则使用默认的MinIO域名
        return endpoint + "/" + bucketName + "/" + filePath;
    }
}

