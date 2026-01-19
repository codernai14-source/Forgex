package com.forgex.sys.controller;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.service.CodeGenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 代码生成控制器
 * <p>提供代码生成的预览和下载功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/codegen")
@RequiredArgsConstructor
public class CodeGenController {

    /**
     * 代码生成服务
     */
    private final CodeGenService codeGenService;

    /**
     * 预览生成的代码
     * <p>根据请求参数生成代码，返回文件名和内容的映射。</p>
     * 
     * @param req 代码生成请求参数
     * @return 代码文件映射
     */
    @PostMapping("/preview")
    public R<Map<String, String>> preview(@RequestBody CodeGenRequestDTO req) {
        // 参数校验
        if (req == null || !StringUtils.hasText(req.getTableName())) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        return R.ok(codeGenService.preview(req));
    }

    /**
     * 下载生成的代码压缩包
     * <p>根据请求参数生成代码，打包成ZIP文件并下载。</p>
     * 
     * @param req 代码生成请求参数
     * @param response HTTP响应对象
     */
    @PostMapping("/download")
    public void download(@RequestBody CodeGenRequestDTO req, HttpServletResponse response) {
        try {
            // 生成代码压缩包
            byte[] zip = codeGenService.generateZip(req);
            
            // 设置响应头
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"codegen.zip\"");
            response.setContentLengthLong(zip == null ? 0 : zip.length);
            
            // 写入响应流
            try (OutputStream os = response.getOutputStream()) {
                if (zip != null) {
                    os.write(zip);
                }

                os.flush();
            }
        } catch (Exception e) {
            // 异常处理：返回错误信息
            try {
                response.setContentType("text/plain;charset=UTF-8");
                response.setStatus(500);
                response.getOutputStream().write("codegen failed".getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }
    }
}

