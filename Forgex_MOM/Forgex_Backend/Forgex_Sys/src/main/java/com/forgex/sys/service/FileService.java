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
package com.forgex.sys.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 文件服务接口
 * 
 * 提供文件上传、下载等功能，支持不同操作系统的存储目录管理
 * 
 * @author coder_nai@163.com
 * @date 2026-01-08
 */
public interface FileService {
    
    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @return 可访问的文件URL
     * @throws IOException 文件处理异常
     */
    String upload(MultipartFile file) throws IOException;
    
    /**
     * 获取文件资源
     * 
     * @param filename 文件名
     * @return 文件资源
     * @throws IOException 文件处理异常
     */
    Resource getFile(String filename) throws IOException;
    
    /**
     * 获取基础存储目录
     * 
     * @return 存储目录路径
     */
    Path getBaseDir();
    
    /**
     * 获取文件的Media Type
     * 
     * @param filename 文件名
     * @return Media Type字符串
     * @throws IOException 文件处理异常
     */
    String getMediaType(String filename) throws IOException;
}