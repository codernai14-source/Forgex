package com.forgex.sys.config;

import com.forgex.common.config.ConfigService;
import com.forgex.sys.domain.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.access.prefix:/files}")
    private String accessPrefix;

    @jakarta.annotation.Resource
    private ConfigService configService;

    private String resolveUploadPath() {
        FileUploadConfig cfg = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, null);
        if (cfg != null && StringUtils.hasText(cfg.getLocalUploadPath())) {
            return cfg.getLocalUploadPath();
        }
        return uploadPath;
    }

    private String resolveAccessPrefix() {
        FileUploadConfig cfg = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, null);
        if (cfg != null && StringUtils.hasText(cfg.getAccessPrefix())) {
            return cfg.getAccessPrefix();
        }
        return accessPrefix;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = resolveAccessPrefix();
        String path = resolveUploadPath();

        if (!StringUtils.hasText(prefix)) {
            prefix = "/files";
        }
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (!StringUtils.hasText(path)) {
            path = "./uploads";
        }

        registry.addResourceHandler(prefix + "/**")
                .addResourceLocations("file:" + path + "/");
    }
}
