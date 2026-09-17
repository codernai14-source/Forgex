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
 * 本地文件上传与公开读取服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface FileService {

    /**
     * 上传文件并返回可访问 URL。
     *
     * @param file 上传文件
     * @param moduleCode 模块编码
     * @param moduleName 模块名称
     * @return 访问 URL
     * @throws IOException 存储失败时抛出
     */
    String upload(MultipartFile file, String moduleCode, String moduleName) throws IOException;

    /**
     * 按指定体积上限上传文件并写入文件记录。
     *
     * @param file 上传文件
     * @param moduleCode 模块编码
     * @param moduleName 模块名称
     * @param maxSizeMbOverride 覆盖体积上限（MB）；为空时使用全局配置
     * @return 访问 URL
     * @throws IOException 存储失败时抛出
     */
    String upload(MultipartFile file, String moduleCode, String moduleName, Long maxSizeMbOverride) throws IOException;

    /**
     * 按相对路径读取本地文件资源。
     *
     * @param filename 相对文件名
     * @return 文件资源；不存在时返回 {@code null}
     * @throws IOException 路径非法时抛出
     */
    Resource getFile(String filename) throws IOException;

    /**
     * 解析当前存储根目录。
     *
     * @return 上传根路径
     */
    Path getBaseDir();

    /**
     * 解析文件媒体类型。
     *
     * @param filename 相对文件名
     * @return MIME 类型
     * @throws IOException 路径非法时抛出
     */
    String getMediaType(String filename) throws IOException;
}
