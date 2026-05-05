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
package com.forgex.sys.controller;

import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.vo.CodegenPreviewVO;
import com.forgex.sys.service.CodeGenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 代码生成控制器
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/codegen")
@RequiredArgsConstructor
public class CodeGenController {

    private final CodeGenService codeGenService;

    /**
     * 预览代码生成结果。
     *
     * @param req 生成请求
     * @return 预览结果
     */
    @PostMapping("/preview")
    public R<CodegenPreviewVO> preview(@RequestBody CodeGenRequestDTO req) {
        if (req != null && req.getConfigId() != null) {
            return R.ok(codeGenService.previewByConfigId(req.getConfigId()));
        }
        return R.ok(codeGenService.preview(req));
    }

    /**
     * 下载文件。
     *
     * @param req 生成请求
     * @param response 响应对象
     */
    @PostMapping("/download")
    public void download(@RequestBody CodeGenRequestDTO req, HttpServletResponse response) {
        try {
            CodegenPreviewVO preview;
            byte[] zip;
            if (req != null && req.getConfigId() != null) {
                preview = codeGenService.previewByConfigId(req.getConfigId());
                zip = codeGenService.generateZipByConfigId(req.getConfigId());
            } else {
                preview = codeGenService.preview(req);
                zip = codeGenService.generateZip(req);
            }
            String fileName = preview.getZipFileName();
            response.setContentType("application/zip");
            response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20") + "\""
            );
            response.setContentLengthLong(zip.length);
            try (OutputStream os = response.getOutputStream()) {
                os.write(zip);
                os.flush();
            }
        } catch (Exception ex) {
            try {
                response.setStatus(500);
                response.setContentType("text/plain;charset=UTF-8");
                response.getOutputStream().write("codegen download failed".getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }
    }
}
