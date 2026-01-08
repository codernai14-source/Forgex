package com.forgex.sys.controller;

import com.forgex.common.web.R;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件控制器。
 * <p>
 * 提供文件上传与读取接口：根据系统类型选择默认存储目录（Windows: D:/forgex/cache；Linux/Unix: $HOME/forgex/cache 或 /tmp/forgex/cache），
 * 上传成功后返回可访问的 URL；读取接口按文件名返回内容并设置适当的 Content-Type。
 * <p>
 * 使用：前端以 multipart/form-data 上传文件至 {@code /sys/file/upload}，后端返回 URL；
 * 通过 {@code /sys/file/{name}} 读取文件内容。
 * 可扩展性：后续可接入对象存储、权限控制、文件加密等。
 */
@RestController
@RequestMapping("/sys/file")
public class FileController {
    /**
     * 计算基础存储目录（按系统类型）。
     * @return 存储目录路径
     */
    private Path baseDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get("D:", "forgex", "cache");
        } else {
            String home = System.getProperty("user.home");
            return Paths.get(home == null ? "/tmp" : home, "forgex", "cache");
        }
    }
    /**
     * 上传文件并返回可访问 URL。
     * 文件名采用随机 UUID，保留原扩展名；目录不存在时自动创建。
     * @param file 上传文件
     * @return 可访问的文件 URL（相对路径）
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Path dir = baseDir();
        Files.createDirectories(dir);
        String original = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(name);
        Files.copy(file.getInputStream(), target);
        String url = "/api/sys/file/" + name;
        return R.ok(url);
    }

    /**
     * 读取文件内容。
     * 若文件不存在，返回 404；若无法探测类型，按八位字节流返回。
     * @param name 文件名
     * @return 文件资源响应
     */
    @GetMapping("/{name}")
    public ResponseEntity<Resource> get(@PathVariable("name") String name) throws IOException {
        Path dir = baseDir();
        Path p = dir.resolve(name);
        if (!Files.exists(p)) {
            return ResponseEntity.notFound().build();
        }
        String ct = Files.probeContentType(p);
        MediaType mt = ct == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(ct);
        InputStreamResource r = new InputStreamResource(Files.newInputStream(p));
        return ResponseEntity.ok().contentType(mt).body(r);
    }
}
