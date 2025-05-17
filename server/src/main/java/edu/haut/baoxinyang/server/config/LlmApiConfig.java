package edu.haut.baoxinyang.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM API配置，包含聊天和管理功能的配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "llm.api")
public class LlmApiConfig {
    
    /**
     * API基础URL
     */
    private String baseUrl = "http://localhost:8000";
    
    /**
     * API版本路径
     */
    private String apiVersion = "/api/v1";
    
    /**
     * 非流式请求路径
     */
    private String askPath = "/api/v1/ask";
    
    /**
     * 流式请求路径
     */
    private String streamPath = "/api/v1/stream";
    
    /**
     * 向量数据库API路径
     */
    private String vectorPath = "/vectors";
    
    /**
     * 精确查询数据库API路径
     */
    private String precisePath = "/precise";
    
    /**
     * 提示词模板API路径
     */
    private String templatePath = "/templates";
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 30000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 60000;
    
    /**
     * 写入超时时间（毫秒）
     */
    private int writeTimeout = 30000;
    
    /**
     * 获取完整的基础API URL
     * @return 完整的基础API URL
     */
    public String getBaseApiUrl() {
        return baseUrl + apiVersion;
    }
    
    /**
     * 获取向量数据库API完整URL
     * @return 向量数据库API完整URL
     */
    public String getVectorUrl() {
        return getBaseApiUrl() + vectorPath;
    }
    
    /**
     * 获取精确查询数据库API完整URL
     * @return 精确查询数据库API完整URL
     */
    public String getPreciseUrl() {
        return getBaseApiUrl() + precisePath;
    }
    
    /**
     * 获取提示词模板API完整URL
     * @return 提示词模板API完整URL
     */
    public String getTemplateUrl() {
        return getBaseApiUrl() + templatePath;
    }
    
    /**
     * 获取聊天API完整URL
     * @return 聊天API完整URL
     */
    public String getAskUrl() {
        return baseUrl + askPath;
    }
    
    /**
     * 获取流式聊天API完整URL
     * @return 流式聊天API完整URL
     */
    public String getStreamUrl() {
        return baseUrl + streamPath;
    }
} 