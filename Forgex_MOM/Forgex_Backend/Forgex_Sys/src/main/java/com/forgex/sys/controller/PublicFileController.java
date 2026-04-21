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
 * Public file access controller.
 */
@Controller
@RequiredArgsConstructor
public class PublicFileController {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String FILE_PATTERN = "/files/**";

    private final FileService fileService;

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

        String mediaType = fileService.getMediaType(filePath);
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElseGet(() -> StringUtils.hasText(mediaType)
                        ? MediaType.parseMediaType(mediaType)
                        : MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(contentType)
                .body(resource);
    }

    private String extractFilePath(HttpServletRequest request) {
        String pathWithinMapping = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (!StringUtils.hasText(pathWithinMapping) || !StringUtils.hasText(bestMatchingPattern)) {
            return "";
        }
        return PATH_MATCHER.extractPathWithinPattern(bestMatchingPattern, pathWithinMapping);
    }
}

