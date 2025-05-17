package edu.haut.baoxinyang.server.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.Collections;

/**
 * 资源处理配置类
 * 用于确保API请求不被当作静态资源处理
 */
@Configuration
public class ResourceHandlingConfig {

    /**
     * 配置静态资源处理器映射
     * 设置优先级低于Controller的处理器
     */
    @Bean
    public SimpleUrlHandlerMapping customResourceHandlerMapping(
            WebProperties webProperties,
            WebMvcProperties mvcProperties) {
        
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        // 设置优先级为最低，确保Controller优先处理
        mapping.setOrder(Integer.MAX_VALUE - 1);
        
        // 只处理/static/**路径的静态资源，避免处理API请求
        mapping.setUrlMap(Collections.singletonMap("/static/**", 
                new ResourceHttpRequestHandler()));
        
        return mapping;
    }
} 