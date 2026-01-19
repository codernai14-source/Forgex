package com.forgex.sys.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * <p>配置静态资源映射等。</p>
 *
 * @author coder_nai@163.com
 * @date 2025-01-11
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.access.prefix:/files}")
    private String accessPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射文件访问路径到本地上传目录
        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
