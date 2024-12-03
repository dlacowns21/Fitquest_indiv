package com.web.fitquest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                "http://localhost:5173",     // Vue 개발 서버
                "http://localhost:8097",     // Spring 서버
                "http://70.12.50.63:8097",    // IP 주소로 접근
                "http://70.12.50.62:8097"    // IP 주소로 접근
                // "http://192.168.10.10:8097",    //  성훈 집 ip로 바꾸기
                // "http://70.12.50.63:8097"    // 채준 집 ip로 바꾸기
            )
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);

        // fitquest 전용 엔드포인트에 대한 설정도 동일하게 수정
        registry
            .addMapping("/fitquest/api/**")
            .allowedOrigins(
                "http://localhost:5173",
                "http://localhost:8097",
                "http://70.12.50.62:8097"    // IP 주소로 접근
                // "http://192.168.10.10:8097",    //  성훈 집 ip로 바꾸기
                // "http://70.12.50.63:8097"    // 채준 집 ip로 바꾸기
            )
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}