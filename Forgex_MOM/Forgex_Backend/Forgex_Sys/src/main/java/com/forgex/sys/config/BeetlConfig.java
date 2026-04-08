package com.forgex.sys.config;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.springframework.context.annotation.Bean;

/**
 * Beetl 模板引擎配置类
 * <p>
 * 用于代码生成器的模板渲染。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@org.springframework.context.annotation.Configuration
public class BeetlConfig {

    /**
     * 创建 Beetl GroupTemplate Bean
     * <p>
     * 配置模板文件的加载路径和渲染规则。
     * </p>
     * 
     * @return GroupTemplate 实例
     * @throws Exception 配置异常
     */
    @Bean
    public GroupTemplate groupTemplate() throws Exception {
        // 创建类路径资源加载器，指定模板文件根目录
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader("templates/codegen/");
        
        // 创建 Beetl 配置对象
        Configuration cfg = Configuration.defaultConfiguration();
        
        // 创建 GroupTemplate 实例
        GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
        
        // 注册自定义函数（如果需要）
        // gt.registerFunction("myFunction", new MyFunction());
        
        return gt;
    }
}



