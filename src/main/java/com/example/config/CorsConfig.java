package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 *
 * @author WanAn
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有的路径
                .allowedOriginPatterns("*") // 允许所有来源
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*") // 允许所有请求头
                .exposedHeaders("*") // 允许所有响应头暴露给客户端
                .allowCredentials(true); // 允许发送凭据(Cookies)
    }
}
