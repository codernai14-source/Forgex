package com.forgex.sys.controller;

import com.forgex.common.web.R;
import com.forgex.sys.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理Controller
 * <p>处理文件上传等操作，Controller层仅负责参数接收与结果返回，业务逻辑委托给{@link FileService}处理。</p>
 *
 * @author coder_nai@163.com
 * @date 2025-01-11
 * @see FileService
 */
@RestController
@RequestMapping("/sys/file")
public class FileController {

    /**
     * 文件服务接口
     */
    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }

        try {
            // 调用文件服务上传文件
            String fileUrl = fileService.upload(file);
            return R.ok("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
