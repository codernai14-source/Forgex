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
package com.forgex.sys.service.impl;

import com.forgex.sys.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件服务实现类
 * <p>提供文件上传、下载、获取媒体类型等功能，根据操作系统自动选择存储目录。</p>
 * <p><strong>存储目录选择规则：</strong></p>
 * <ul>
 *   <li>Windows系统：{@code D:\forgex\cache}</li>
 *   <li>其他系统：{@code ${user.home}/forgex/cache}，若用户目录为空则使用 {@code /tmp}</li>
 * </ul>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>获取基础存储目录</li>
 *   <li>上传文件并返回可访问URL</li>
 *   <li>根据文件名获取文件资源</li>
 *   <li>获取文件的Media Type</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-08
 */
@Service
public class FileServiceImpl implements FileService {

    /**
     * 获取基础存储目录（按系统类型）
     * 
     * @return 存储目录路径
     */
    @Override
    public Path getBaseDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get("D:", "forgex", "cache");
        } else {
            String home = System.getProperty("user.home");
            return Paths.get(home == null ? "/tmp" : home, "forgex", "cache");
        }
    }

    /**
     * 上传文件并返回可访问 URL
     * 
     * @param file 上传文件
     * @return 可访问的文件 URL（相对路径）
     * @throws IOException 文件处理异常
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        Path dir = getBaseDir();
        Files.createDirectories(dir);
        
        String original = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(name);
        Files.copy(file.getInputStream(), target);
        
        return "/api/sys/file/" + name;
    }

    /**
     * 获取文件资源
     * 
     * @param filename 文件名
     * @return 文件资源
     * @throws IOException 文件处理异常
     */
    @Override
    public Resource getFile(String filename) throws IOException {
        Path dir = getBaseDir();
        Path filePath = dir.resolve(filename);
        
        if (!Files.exists(filePath)) {
            return null;
        }
        
        return new InputStreamResource(Files.newInputStream(filePath));
    }

    /**
     * 获取文件的Media Type
     * 
     * @param filename 文件名
     * @return Media Type字符串
     * @throws IOException 文件处理异常
     */
    @Override
    public String getMediaType(String filename) throws IOException {
        Path dir = getBaseDir();
        Path filePath = dir.resolve(filename);
        
        if (!Files.exists(filePath)) {
            return "application/octet-stream";
        }
        
        String mediaType = Files.probeContentType(filePath);
        return mediaType == null ? "application/octet-stream" : mediaType;
    }
}