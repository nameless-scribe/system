package com.system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${upload.img.path}")
    private String imgPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /img/** 到本地上传目录，便于前端直接访问图片
        if (imgPath != null) {
            String location = imgPath.replace("\\", "/");
            if (!location.endsWith("/")) {
                location = location + "/";
            }
            registry.addResourceHandler("/img/**")
                    .addResourceLocations("file:" + location);
        }
    }
}

