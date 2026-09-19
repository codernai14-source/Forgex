/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.sys.controller;

import com.forgex.sys.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 本地公开文件读取入口。
 * <p>
 * 服务内映射 {@code GET /files/**}，经网关对外为 {@code GET /api/sys/files/**}。
 * 登录页 Logo、背景和头像都走这条路径，因此必须匿名可读，并返回可被浏览器直接渲染的 Content-Type。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FileService#getFile(String)
 */
@Controller
@RequiredArgsConstructor
public class PublicFileController {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String FILE_PATTERN = "/files/**";

    private final FileService fileService;

    /**
     * 按相对路径读取本地文件并以 inline 方式返回。
     *
     * @param request 当前请求，用于提取 {@code /files/**} 之后的相对路径
     * @return 文件响应；路径非法或不存在时返回 404
     * @throws IOException 读取文件失败时抛出
     */
    @GetMapping(FILE_PATTERN)
    public ResponseEntity<Resource> getFile(HttpServletRequest request) throws IOException {
        String filePath = extractFilePath(request);
        if (!StringUtils.hasText(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileService.getFile(filePath);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = resolveContentType(filePath, resource);
        String filename = resource.getFilename();
        if (!StringUtils.hasText(filename)) {
            filename = filePath;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(contentType)
                .body(resource);
    }

    /**
     * 按文件名优先推断媒体类型，避免 nosniff 把图片/视频当成二进制下载。
     *
     * @param filePath 相对文件路径
     * @param resource 已定位的文件资源
     * @return 可用于浏览器直接渲染的媒体类型
     * @throws IOException 探测磁盘类型失败时抛出
     */
    private MediaType resolveContentType(String filePath, Resource resource) throws IOException {
        return MediaTypeFactory.getMediaType(filePath)
                .or(() -> MediaTypeFactory.getMediaType(resource))
                .orElseGet(() -> {
                    try {
                        String mediaType = fileService.getMediaType(filePath);
                        if (StringUtils.hasText(mediaType) && !"application/octet-stream".equals(mediaType)) {
                            return MediaType.parseMediaType(mediaType);
                        }
                    } catch (IOException ignored) {
                        // 探测失败时继续走扩展名兜底。
                    }
                    return fallbackMediaType(filePath);
                });
    }

    /**
     * 按扩展名给出浏览器可渲染的兜底类型。
     *
     * @param filePath 相对文件路径
     * @return 已知图片、视频、办公文档扩展名对应的类型，否则为二进制流
     */
    private MediaType fallbackMediaType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        }
        if (lower.endsWith(".mp4")) {
            return MediaType.parseMediaType("video/mp4");
        }
        if (lower.endsWith(".webm")) {
            return MediaType.parseMediaType("video/webm");
        }
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (lower.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        if (lower.endsWith(".doc")) {
            return MediaType.parseMediaType("application/msword");
        }
        if (lower.endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        if (lower.endsWith(".xls")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        }
        if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * 从 HandlerMapping 属性中提取 {@code /files/**} 后的相对路径。
     *
     * @param request 当前请求
     * @return 相对路径，无法解析时返回空串
     */
    private String extractFilePath(HttpServletRequest request) {
        String pathWithinMapping = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (!StringUtils.hasText(pathWithinMapping) || !StringUtils.hasText(bestMatchingPattern)) {
            return "";
        }
        return PATH_MATCHER.extractPathWithinPattern(bestMatchingPattern, pathWithinMapping);
    }
}
