package com.example.conifg;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 拦截所有路径的请求
                .allowedOriginPatterns("http://localhost:3000") // 明确指定允许的前端地址<span data-allow-html class='source-item source-aggregated' data-group-key='source-group-6' data-url='https://blog.csdn.net/weixin_44953227/article/details/119281830' data-id='turn0search5'><span data-allow-html class='source-item-num' data-group-key='source-group-6' data-id='turn0search5' data-url='https://blog.csdn.net/weixin_44953227/article/details/119281830'><span class='source-item-num-name' data-allow-html>csdn.net</span><span data-allow-html class='source-item-num-count'></span></span></span>
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法，必须包含OPTIONS<span data-allow-html class='source-item source-aggregated' data-group-key='source-group-7' data-url='https://oneuptime.com/blog/post/2025-12-22-configure-cors-spring-boot/view' data-id='turn0search8'><span data-allow-html class='source-item-num' data-group-key='source-group-7' data-id='turn0search8' data-url='https://oneuptime.com/blog/post/2025-12-22-configure-cors-spring-boot/view'><span class='source-item-num-name' data-allow-html>oneuptime.com</span><span data-allow-html class='source-item-num-count'></span></span></span>
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true); // 允许携带Cookie等凭证
    }

}
