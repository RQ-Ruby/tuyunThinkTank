package com.RQ.tuyunthinktank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description 全局跨域配置
 * @author RQ
 * @date 2025/5/25 下午4:19
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
/**
 * @return: void
 * @author RQ
 * @date: 2025/5/25 下午4:28
 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 全局跨域配置，覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie 和认证信息
                .allowCredentials(true)
                // 使用 allowedOriginPatterns 以支持通配符域名与 allowCredentials 共存
                .allowedOriginPatterns("*")
                // 支持常见的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头
                .allowedHeaders("*")
                // 暴露所有响应头给前端
                .exposedHeaders("*");
    }
}
