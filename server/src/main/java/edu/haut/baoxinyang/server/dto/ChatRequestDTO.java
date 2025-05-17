package edu.haut.baoxinyang.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 聊天请求DTO，用于向FastAPI发送请求
 */
@Data
public class ChatRequestDTO {
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 模型设置
     */
    private ModelSettings modelSettings;
    
    /**
     * 模板配置
     */
    private TemplateConfig templateConfig;
    
    /**
     * 向量搜索配置
     */
    private VectorSearchConfig vectorSearchConfig;
    
    /**
     * 精确查询配置
     */
    private PreciseSearchConfig preciseSearchConfig;
    
    /**
     * 聊天历史记录
     */
    private List<ChatMessage> history;
    
    /**
     * 模型设置
     */
    @Data
    public static class ModelSettings {
        /**
         * 模型名称
         */
        private String modelName;
        
        /**
         * API密钥
         */
        private String apiKey;
        
        /**
         * 模型API地址
         */
        private String baseUrl;
    }
    
    /**
     * 模板配置
     */
    @Data
    public static class TemplateConfig {
        /**
         * 模板ID
         */
        private String templateId;
        
        /**
         * 小模板ID列表
         */
        private List<String> subTemplateIds;
        
        /**
         * 模板参数
         */
        private Map<String, String> params;
    }
    
    /**
     * 向量搜索配置
     */
    @Data
    public static class VectorSearchConfig {
        /**
         * 命名空间列表
         */
        private List<String> namespaces;
        
        /**
         * 每个命名空间返回的最大结果数
         */
        private Integer nResults;
        
        /**
         * 元数据过滤条件
         */
        private Map<String, String> metadataFilter;
        
        /**
         * 历史消息查询数量
         */
        private Integer ragHistoryCount;
    }
    
    /**
     * 精确查询配置
     */
    @Data
    public static class PreciseSearchConfig {
        /**
         * 要搜索的大类ID列表
         */
        private List<String> categories;
        
        /**
         * 最大返回结果数
         */
        private Integer maxResults;
        
        /**
         * 搜索深度
         */
        private Integer searchDepth;
        
        /**
         * 历史消息查询数量
         */
        private Integer ragHistoryCount;
    }
    
    /**
     * 聊天消息
     */
    @Data
    public static class ChatMessage {
        /**
         * 角色（user、assistant）
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }
} 